yubinotes
=========

Open source secure notes app for Android that supports using either a password or the YubiKey NEO to encrypt/decrypt notes. Encryption/Decryption is based on a simple lock system that stores and wipes necessary keys for decryption. 

Who's it for?
======
Anyone who value their privacy and wish to store sensitive notes and snippets on their device. 

Who's it not for?
=====
The app will not stop ninjas with physical access to your device and your yubikey or password from extracting your notes. Also, if you're being hunted by security agencies, dont blame me if they break the crypto and steal your notes.

Modes
====

Three modes of operation:

* yubikey (offline)
* yubikey (online, feature not ready yet)
* passcode (offline)

Security
===

Notes are encrypted using AES/CBC with PKCS5 Padding. 

The encryption and decryption process is based around 4 security keys. The first two are generated the first time the app is started and stored there. The second two are the result of the hashed password or yubikey input string. 

As long as the device is locked only the unique device id keys are stored on the device. The other two keys are wiped when the note store is locked. A one-way hash of the password is also stored on the device when password mode is used.

When decrypting the notes, the following will happen in Password Mode:

The password provided from the user is hashed and checked against the stored hash value. If it matches, the hashed password is separated into two hash strings which are then XOR'ed with the first and second security key. The resulting values are then used as IV and key and used for decrypting the notes.

When decrypting notes in YubiKey mode, the procedure is similar.

The string provided by the YubiKey is XOR'ed first with the first key, and then with the second. The resulting two values are used as IV and key and used for decrypting the notes.


Todo (In no particular order)
===
* OnResume polish
* Bug Fixes
* Widget
* Rewrite logic!
* Tablet support (Fragments) 
