## Decompiled code
Easy Smart Configuration Utility beta 14.0.0.162 was decompiled using Procyon and jd-gui. Previous encryption features (RC4 and TEA) are still present, but appear to have been supplanted by other encryption methods. New encryption features have replaced them, which appear to be a form of RSA (although further analysis would be required to determine the precise specifications). The most important addition is the use of pseudo random number generation (PRNG) to produce encryption keys. This is a substantial improvement. 
### PRNG strength
One factor to take into account in further analysis is whether the PRNG algorithm used can be considered "secure". Use of a weak PRNG algorithm can lead to a exploitable condition even within a strong encrpytion algorithm. Verification of the strength of the PRNG algorithm should be taken into account but it should also be noted that regardless of PRNG strength, this new encyrption scheme appears to be a vast improvement over the original static key RC4.
### Code cleanup
In current Utility beta, `transfer.This` method `main` contains static key used for RC4 in plaintext. While this key no longers appears to be used for sensitive data transmission, it would be advisable to remove the reference. Other sections of altered code should be examined to ensure that excess information is not easily revealed through decompilation.
## Patch integrity
Installation of the Easy Smart Confuguration Utility patch 14.0.0.162 executed without error.
Backwards compatibility between new Utility and existing switch firmware, as well as between existing Utility and new firmware, established connection without error.
Firmware update of TL-SG105Ev5 to 1.0.0 Build 20221201 Rel.29985(Beta) succeeded without error.
Original settings were retained.
## Packet captures
Packet captures were performed using Wireshark and examined in raw hexadecimal. Output was run through Proof of Concept decoder.
### Utility communication
Sequential logins captured in wireshark show different encryption used between different established sessions. Previous RC4/TEA encryption is still used for some packets, as the Proof of Concept does still decrypt portions of the communications. However, the login credentials are not decrypted by Proof of Concept.
### Web interface communication
Web interface remains entirely unencrpyted. Given that encryption of login credentials is in a much more secure state when using the Utility, customers should be informed that the Utility is preferable for secure communications until (and if) HTTPS communication with the web interface becomes available.
### Broadcast connections
As broadcast packets are received by all hosts within a network segment, it remains advisable to limit the scope of extended communications. Broadcast should be limited to discovery, while established connections should utilize protocols for more direct communication. Overuse of broadcast is mainly a security concern when using unecrypted or poorly encrypted communications, so with the improvement to encryption strength it is less of a security concern. However, consideration should be given to altering the connection model in future development, as it can be an inherently problematic design to communicate broadly with an entire network segment.
### Management interface reponsiveness in VLANs
Management interface discovery can still occur regardless of VLAN, in spite of inter-VLAN routing and firewalling. It is strongly recommended that future development of VLAN-capable equipment adopt stricter handling of access management, and provide the user with the means of limiting management access to designated VLANs.