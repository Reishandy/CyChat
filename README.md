# CyChat
## Secure Messaging Desktop App

This advanced programming final project is Java-based and incorporates Object-Oriented Programming principles, JCDB, Swing GUI, Socket, and Java's Security package. The project's core concept revolves around a Secure Messaging app for desktop, designed for Real-Time Communication and Serverless or Peer-to-Peer (P2P) interactions. Every real-time message operation is encrypted using the AES algorithm with a 128-bit key and features key exchange using the RSA algorithm. Additionally, the application will include a contact system and encrypted message history, which is stored locally on the user's machine.

## Key Features:
- Real-time and secure communication, similar to common messaging apps.
- Designed for LAN connections, with potential for internet connectivity.
- Users can discover and connect with others on the same LAN.
- Adding and removing contacts.
- Direct peer-to-peer communication is required for messaging.
- Consideration for transitioning from P2P to a server-based model, enabling message logic.storage and forwarding for inactive recipients when resources permit.

Please note that the transition to a server-based model may be explored if additional project time and resources become available.

## Known bugs (as of version 0.1):

The following list is a list of major and minor bugs

### Major bugs
- Unable to do peer discovery in an isolated network
- Unable to perform any tcp connection (Contact exchange, Chat handshake, Chat), reason unknown
- Cannot perform a reconnect to chat after exiting or disconnecting from chat, temporary solution: exit the app after disconnect

### Minor bugs
- All message will be identified and added to partner chat bubble (grey), if the user name is the same. possible solution: chaange message identification to user id
- The first chat bubble will not be aligned properly
