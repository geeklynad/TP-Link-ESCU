package javadecrypt;

import java.util.Arrays;
import java.util.HexFormat;

public class main {

    // int array to store post-KSA key
    private static int[] mS = new int[256];
    
    // checks if XTEA has already been performed
    private static boolean lC;

    public static void main(String[] args) {
        if (lC) {
            return;
        }

        // Send XTEA-encrypted key to be decrypted
        final String h = new of().h(new byte[] {REDACTED});
        
        // KSA phase
        final byte[] array = new byte[256];
        int n = 0;
        for (int i = 0; i < 256; ++i) {
            array[mS[i] = i] = (byte)h.charAt(i % h.length());
        }
        for (int j = 0; j < 256; ++j) {
            n = (n + mS[j] + array[j]) % 256;
            final int n2 = mS[j];
            mS[j] = mS[n];
            mS[n] = n2;
        }
        lC = true;

        // Print key post-XTEA
        System.out.println("Pre-KSA key plaintext: " + h);
        // Pre-KSA in bytes can be enabled in Key.of.h()
        // Print key post-KSA
        System.out.println("Post-KSA Key in bytes: " + Arrays.toString(mS));


        // Example packet captured from wireshark
        HexFormat hexFormat = HexFormat.of();
        byte[] arrayOfByte = hexFormat.parseHex("5d777eefcbb14f45bfc42eb9cd4d7e51422ba2f5d791aeed508f31d5c202909a591381c0464e465f27d942b5a44c2c4a3a03355a0b08fa1e541489f773e1");

        // Print UTF-8 and byte array of encrypted packet
        System.out.println("Encrypted: " + new String(arrayOfByte));
        for (int length = arrayOfByte.length, i = 0; i < length; ++i) {
            System.out.print(arrayOfByte[i] + " ");
        }
        System.out.println("\n");

        // PRGA phase
        This.Code(arrayOfByte, arrayOfByte.length);

        // TLV.g to build string with de-signed byte values
        System.out.println("Decrypted: " + TLV.g(arrayOfByte));

        // Print de-signed byte values
        for (int length = arrayOfByte.length, i = 0; i < length; ++i) {
            System.out.print((arrayOfByte[i] & 0xFF) + " ");
        }
        System.out.println("\n");
    }
}
