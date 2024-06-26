# simple-chat-server

The Simple Chat Server is a Java-based server application that allows multiple clients to connect and communicate with each other in a chat room environment.


<h2>Features</h2>

Supports multiple simultaneous client connections

Basic chat functionality (sending and receiving messages)

Customizable port number, if none is set the default port number is 9060

Available commands:

```
/commands - Show all available commands
        
/users - Show online users
 
/exit - Disconnect from the server
        
/whisper <username> - Send a private message to a specific user
        
/block <username> - Block messages from a specific user
        
/unblock <username> - Unblock messages from a specific user
```        


<h2>Getting Started</h2>

Prerequisites

<a href="https://www.oracle.com/java/technologies/downloads"> Java Development Kit (JDK) </a> installed on your system

<a href="https://ant.apache.org"> Apache Ant </a> installed on your system

<h3>Installation</h3>

1. Clone this repository to your local machine:
```
git clone https://github.com/AlvaroMyGit/simple-chat-server.git
```
2. Compile the source code to generate the JAR file:
Navigate to the project root directory which has the build.xml file
```
ant compile
```

<h3>Usage</h3>

1. Navigate to the directory containing the JAR file (SimpleChatServer.jar).

2. Run the server with the following command, specifying the desired port number (default is 9060)

Example:
```
java -jar SimpleChatServer.jar 10010
```

3. Clients can connect to the server using Netcat, Telnet or other similar application

Example with Telnet: 
```
telnet localhost 9060
```
Example with Netcat: 
```
nc localhost 9060
```
Substitute 'localhost' with the IP address of the machine where the server is running
