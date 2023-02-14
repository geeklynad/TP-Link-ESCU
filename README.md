Static key encryption between TP Link's Easy Smart Configuration Utility v1.3.10 (and earlier) and Easy Smart Switch product line allows network attackers to decrypt captured packets to obtain administrator login credentials and subsequently gain control of network hardware to escape VLAN segmentation, perform denial of service, and access administrative capabilities of the listed devices.

### Affected hardware

The Easy Smart Configuration Utility is used to administratively interface with a range of devices in the Easy Smart product line. The following devices, per the compatibility list available at the [Easy Smart Configuration Utility download page](https://www.tp-link.com/us/support/download/tl-sg108e/), are susceptible to the attack as the utility performs the same static key encryption for all datastream transmissions.

* TL-SG1428PE(UN) V1/V1.2/V1.26/V2/V2.2
* TL-SG1218MPE(UN) V1/V2/V3.2/V3.26/V4/V4.2
* TL-SG1210MPE V2/V3
* TL-SG1024DE(UN) V1/V2/V3/V4/V4.2/V4.26
* TL-SG1016PE(UN) V1/V2/V3.2/V3.26/V4/V5
* TL-SG1016DE(UN) V1/V2/V3/V4/V4.2
* TL-SG116E(UN) V1/V1.2/V2/V2.6
* TL-SG105E(UN) V1/V2/V3/V4/V5
* TL-SG108E(UN) V1/V2/V3/V4/V5/V6
* TL-SG108PE(UN) V1/V2/V3/V4/V5
* TL-SG105PE(UN) V1/V2
* TL-RP108GE(UN) V1

### Attack conditions

* The attacker must have visibility to capture broadcast domain transmissions within LAN or VLAN.
* A user of the Easy Smart Configuration Utility must log into the device while the attacker is observing network traffic.
* The attacker requires no prior authorization to the hardware or utility in order to extract encryption key and read unencrypted packet data.

### Easy Smart Configuration Utility packet encryption

The utility uses RC4 encryption with a static key. The static key is stored internally in the utility, in the form of a byte array that is encrypted using TEA. When called upon, the key is decrypted with TEA, and RC4 is performed on the packet to be sent.

Obtaining the static key is possible due to the utility being readily decompiled. The encrypted key can be located and run through the TEA decryption process to obtain the unencrypted RC4 plaintext key. RC4 can then be performed on captured packets to obtain the login credentials' plaintext.

As the encryption key is statically stored in the utility which is readily available from the manufacturer, an attacker can freely extract the key by reproducing the TEA method to output the plaintext key. This can be performed by rebuilding the decompiled java classes to output the key string. Proof of concept also shows that reproducing the TEA decryption in Python is possible despite differences of data types and usage of bitwise operations, and only requires the encrypted byte array from the decompiled java classes. 

### VLAN escape and denial of service

VLAN compatibility is a main feature of the Easy Smart Switch product line. An attacker who is able to obtain administrative credentials will be able to log into the hardware normally. VLAN access port and trunk line designations can be altered at-will. An attacker can then use the switch to pivot to target a new device by altering the access ports to the VLAN segment the attacker has access to. Alternatively, if the attacker is being served by one of the access ports of the target switch, the VLAN access port can be altered to a different VLAN or multiple VLANs (as a trunk line), possibly including the native VLAN. 

Denial of service can be performed with administrator access simply by breaking key functionality. The attacker may also alter the login credentials to disable management login from legitimate administration. Resetting the switch would be limited to physical-based factory reset.

### Encryption use across devices

Decompiled utility data in `com.tplink.smb.easySmartUtility.transfer` classes `I.class` and `thing.class` show methods `I.ba` ( with `This.F`) and `thing.aT` (with `This.Code`) use the static key RC4 cipher on the data portion of the packets both sent and received. There are no known provisions to handle the encryption of the data portion of the packets any differently based off any other criteria, nor is there evidence of another cipher present in the codebase apart from the TEA cipher which is used solely for the encryption of the static key.

The obtained plaintext key can be used to decrypt all encrypted packets sent by the utility. It is for this reason that the list of compatible hardware is assumed to be vulnerable.

---

## Proof of concept

### Java rebuild

The Easy Smart Configuration Utility was decompiled using JD-gui and Procyon. The following dependencies were rebuilt from the decompiled code:

* `com.tplink.smb.easySmartUtility.transfer.of`
* `com.tplink.smb.easySmartUtility.transfer.This`
* `com.tplink.smb.easySmartUtility.transfer.TLV`

`Main.java` was created to break out methods to decrypt the key with TEA, then decrypt a given hexadecimal string obtained through Wireshark packet capture. The capture was performed on a secondary system on the same VLAN.

The Java proof of concept is available in this currently private github repository. Encrypted key byte array is redacted. 

### With Python

Proof of concept was ported to Python by emulating Java bitwise handling performed. Despite the two languages having different integers types and endianness, it was still possible to recreate the entire procedure in Python, including both TEA decryption and RC4 decryption.

The Python proof of concept is also available in this currently private github repository. Key is redacted. Included `dump.txt` file provides example packet capture including login credentials and device information.

---

## Compounding issues

### Broadcast domain communication of administrative interface

The utility and hardware with which it communicates transmit packets to the broadcast domain IPv4 destination address 255.255.255.255, and MAC desctination address ff:ff:ff:ff:ff:ff. These packets are readily and passively intercepted by any other device on the same VLAN segment. Use of a simple packet sniffer combined with decryption can passively log traffic to obtain credentials.

### Administrative interface responds regardless of VLAN

With the TL-SG105Ev5 on which these tests were performed, the device would respond to utility queries regardless of the VLAN it received the queries from. There are no options to limit management access to specific VLANs. As such, once the administrative credentials are obtained from the attack, the attacker would be capable of gaining control of the device regardless of VLAN.

Even in the case of manually setting a static IP on the switch outside of the VLAN of the attacking device, it still responds and allows connection. There are no options available on the device configuration to prohibit administrative communication to or from a given VLAN.

A malicious user within a non-management VLAN could therefore potentially create a brute force script to gain access to the switch even in the case of being unable to capture a login session to decrypt.

### Web interface alternative

As previously reported in [CVE-2017-8075](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2017-8075) for the TL-SG108E, the web interface for the TL-SG105E uses HTTP without encryption, and transmits the login credentials in plaintext. While the web interface does not utilize broadcast domain transmission, these packets are still obtainable by an attacker with visibility on the same VLAN segment.

---

## Previous reports

Previous reports have been submitted by [chmod750](https://chmod750.wordpress.com/2017/04/23/vulnerability-disclosure-tp-link/), [PenTestPartners](https://www.pentestpartners.com/security-blog/how-i-can-gain-control-of-your-tp-link-home-switch/), and [DrGough's TechZone](https://goughlui.com/2018/11/03/not-so-smart-tp-link-tl-sg105e-v3-0-5-port-gigabit-easy-smart-switch/) to TP-Link. CVEs were generated for an earlier iteration of this vulnerability on the TL-SG108E, providing further confirmation that the static key encryption is used across the Smart Switch product line.

* [CVE-2017-8074](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2017-8074) - On the TP-Link TL-SG108E 1.0, a remote attacker could retrieve credentials from "SEND data" log lines where passwords are encoded in hexadecimal. This affects the 1.1.2 Build 20141017 Rel.50749 firmware.
* [CVE-2017-8075](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2017-8075) - On the TP-Link TL-SG108E 1.0, a remote attacker could retrieve credentials from "Switch Info" log lines where passwords are in cleartext. This affects the 1.1.2 Build 20141017 Rel.50749 firmware.
* [CVE-2017-8076](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2017-8076) - On the TP-Link TL-SG108E 1.0, admin network communications are RC4 encoded, even though RC4 is deprecated. This affects the 1.1.2 Build 20141017 Rel.50749 firmware.
* [CVE-2017-8077](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2017-8077) - On the TP-Link TL-SG108E 1.0, there is a hard-coded ciphering key (a long string beginning with Ei2HNryt). This affects the 1.1.2 Build 20141017 Rel.50749 firmware.
* [CVE-2017-8078](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2017-8078) - On the TP-Link TL-SG108E 1.0, the upgrade process can be requested remotely without authentication (httpupg.cgi with a parameter called cmd). This affects the 1.1.2 Build 20141017 Rel.50749 firmware.

### Encryption patch

Since those reports, TP-Link has made an attempt to patch the encryption vulnerability by obfuscating the code of the utility and encrypting the key itself with TEA. While this patch increases the difficulty of performing the key extraction, it does not change the use of the static key. The key itself, in its TEA-decrypted plaintext form, has remained consistent per the description of CVE-2017-8077, "a long string beginning with Ei2HNryt."

### Amendment of scope

Previous reports did not account for the use case of VLAN network topology. However, since the Smart Switch line of devices prominently feature VLAN capability, VLAN segmentation escape should be a primary consideration of this vulnerability as it enables an attacker to potentially gain additional access beyond the device itself.

### Amendment of affected products

Previous reports were specific to single devices being analyzed. However, since the vulnerability lies within the administration interface software utilized by a range of devices, the scope of affected hardware should be broadened to include the utility-compatible devices listed by TP-Link.

## Mitigation notes

### For the end user
In the current state of the Easy Smart Switch product line and Easy Smart Configuration Utility v1.3.10, it should be advised to take the following precautions for logging into the device to perform configuration changes.

* Switch can be physically isolated from network prior to logging in by disconnecting all trunk lines and access ports and then only connecting to device being used to log in. This will completely eliminate the potential for login credentials to be remotely captured.

* It is unknown if session credential are retransmitted at any interval, so switch should remain isolated for the duration of the session.

* Alternatively, as the communication between utility and device appear to be limited to the VLAN from which an user is connecting to the device, it may be possible to limit scope of a session's broadcast transmissions to a management VLAN by manually assigning a static IP address for the switch within the management VLAN and only logging in through a device on the management VLAN. However, packet captures should be performed in a test environment to ensure that no broadcast transmissions are leaked to non-management VLANs.

As the switch cannot currently be configured to enforce limitations of response across different VLANs, the potential for brute force attacks against login credentials exists. A strong password that is resilient against brute force attacks should be configured on the switch.

The switch may be relegated to a "dumb" switch capacity by disabling the trunk line and only being served a single untagged VLAN. The attack potential will be limited to only switch access. DoS and other administrative functions through the compromised hardware can still be obtained, but will be limited in scope to the single VLAN.  

For a highly security sensitive use case, the device should be replaced until the devices and utility are patched to sufficiently address the cryptographic weakness.

### For the manufacturer

The encryption was patched [after](https://chmod750.wordpress.com/2017/04/23/vulnerability-disclosure-tp-link/) [previous](https://goughlui.com/2018/11/03/not-so-smart-tp-link-tl-sg105e-v3-0-5-port-gigabit-easy-smart-switch/) [reports](https://www.pentestpartners.com/security-blog/how-i-can-gain-control-of-your-tp-link-home-switch/), however it still uses a static key. Using a secondary encryption to store a static key in a different form is still a static key. Continued use of a static key, no matter how many times it's encrypted, will ultimately result in the same vulnerability across all devices that use it.

Use of a protocol that utilizes secure key exchanging, such as TLS, would eliminate the issue of static key storage (and the secondary encryption), as fresh keys could be generated per session and exchanged. 

Beyond initial discovery, it is inadvisable to persist communication using broadcast domain transmissions. Not only are these communications easily and passively intercepted, but the communications are much more difficult to restrict using firewall rules or similar mechnisms. 

Administrative access to the switch should be configurable to limit access and only respond within trusted VLANs.

HTTPS should be utilized for the web administration interface. Login credentials should be hashed or encrypted. Hashing would be limited to authentication, however, and could not be used for setting username and password. The temptation to use static key encryption for setting username and password should be dissuaded by the readability of the utility's code. Use of an established secure protocol such as HTTPS should therefore be the primary objective.