package main.java.io.codeforall.bootcamp.javatars;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ServerHandler implements Runnable {

    private Socket clientSocket;
    private Server server;
    private PrintWriter handlerWriter;
    private BufferedReader handlerReader;
    private String username;
    private ArrayList<String> blockedUsers;
    private boolean userConnected;

    public ServerHandler(Server server, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.blockedUsers = new ArrayList<>();
        this.userConnected = true;
        setupSocketStream();
    }

    // Initialize the streams needed for the user
    public void setupSocketStream() {
        try {
            handlerReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            handlerWriter = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("Could not setup the socket streams.");
        }
    }

    // Asks the user to provide a username
    public void askUsername() throws IOException {
        handlerWriter.println("What is your username?");
        handlerWriter.flush();
        String desiredUsername = handlerReader.readLine();

        if (server.getHandlerList().size() == 1) {
            setUsername(desiredUsername);
        } else if (server.findHandlerByUsername(desiredUsername)) {
            System.out.println("else if");
            handlerWriter.println("Username already taken");
            handlerWriter.flush();
            askUsername();
        } else {
            System.out.println("else");
            setUsername(desiredUsername);
        }
    }


    // Greets the user and shows who is online
    public void serverGreeting() {
        handlerWriter.println("Online users:");
        for (ServerHandler handler : server.getHandlerList()) {
            handlerWriter.println("- " + handler.getUsername());
        }
        handlerWriter.println("Welcome to the server " + username + ". To see a list of available commands type: /commands");
        handlerWriter.flush();
    }

    // Direct message to the chosen user
    public void whisper(String input, ServerHandler sender) throws IOException {
        // Extract recipient username and message from the input
        String[] tokens = input.split("\\s+", 3);
        String recipientUsername = tokens[1];
        String message = tokens[2];
        System.out.println(tokens);
        System.out.println(recipientUsername + " recipientUsername");
        System.out.println(message + " message");

        // Find the recipient in the list of active users
        for (ServerHandler recipient : server.getHandlerList()) {
            if (recipient.getUsername().equals(recipientUsername)) {
                // Check if recipient is blocked by the sender
                if (sender.getBlockedUsers().contains(recipientUsername)) {
                    // Sender has blocked the recipient, do not send the whisper
                    sender.handlerWriter.println("You have blocked " + recipientUsername + ". You cannot send whispers to this user.");
                    handlerWriter.flush();
                    return;
                }
                // Send whisper message to the recipient
                recipient.handlerWriter.println("[Whisper from " + sender.getUsername() + "]: " + message);
                recipient.handlerWriter.flush();
                // Notify sender that whisper was sent successfully
                sender.handlerWriter.println("Whisper sent to " + recipientUsername + ": " + message);
                sender.handlerWriter.flush();
                return;
            }
        }
        // Recipient not found, notify sender
        sender.handlerWriter.println("User " + recipientUsername + " not found or is offline.");
        sender.handlerWriter.flush();
    }

    public void blockUser(String blockedUsername) {
        // Check if the user to be blocked exists in the list of online users
        boolean userExists = false;
        for (ServerHandler handler : server.getHandlerList()) {
            if (handler.getUsername().equals(blockedUsername)) {
                userExists = true;
                break;
            }
        }
        if (userExists) {
            // Add the blocked user to the list
            blockedUsers.add(blockedUsername);
            handlerWriter.println("User " + blockedUsername + " has been blocked.");
            handlerWriter.flush();
            System.out.println("User " + blockedUsername + " has been blocked by " + this.getUsername());
            System.out.println("Blocked Users List: " + blockedUsers);
        } else {
            handlerWriter.println("Invalid command format. Usage: /block <username>");
            handlerWriter.flush();
        }
    }

    public void unblockUser(String unblockedUsername) {
        // Check if the user to be unblocked exists in the list of blocked users
        if (blockedUsers.contains(unblockedUsername)) {
            // Remove the blocked user from the list
            blockedUsers.remove(unblockedUsername);
            handlerWriter.println(("User " + unblockedUsername + " has been unblocked."));
            handlerWriter.flush();
            System.out.println("User " + unblockedUsername + " has been unblocked by " + this.getUsername());
            System.out.println("Blocked Users List: " + blockedUsers);
        } else {
            handlerWriter.println("Invalid command format. Usage: /unblock <username>");
            handlerWriter.flush();
        }
    }

    public void showAvailableCommands() {
        handlerWriter.println("Available commands:");
        handlerWriter.println("/commands - Show all available commands");
        handlerWriter.println("/users - Show online users");
        handlerWriter.println("/exit - Disconnect from the server");
        handlerWriter.println("/whisper <username> - Send a private message to a specific user");
        handlerWriter.println("/block <username> - Block messages from a specific user");
        handlerWriter.println("/unblock <username> - Unblock messages from a specific user");
        handlerWriter.flush();
        // Add more commands as needed
    }

    public void parseCommand(String input) throws IOException {
        if (input.equals("/commands")) {
            showAvailableCommands();
        }
        if (input.startsWith("/block")) {
            // Extract the username from the input
            String[] target = input.split("\\s+");
            if (target.length == 2) {
                String usernameToBlock = target[1];
                blockUser(usernameToBlock);
            }
        }
        if (input.startsWith("/unblock")) {
            // Extract the username from the input
            String[] target = input.split("\\s+");
            if (target.length == 2) {
                String usernameToUnblock = target[1];
                unblockUser(usernameToUnblock);
            }
        }
        if (input.startsWith("/whisper")) {
            whisper(input, this);

        }
        if (input.startsWith("/exit")) {
            server.broadcast(username + " has left the chat.", this);
            server.getHandlerList().remove(this);
            userConnected = false;
            handlerReader.close();
        }
        if (input.startsWith("/users")) {
            server.showOnlineUsers(this);
        }
    }

    public boolean isCommand(String message) {
        return message.startsWith("/");
    }


    @Override
    public void run() {
        try {
            askUsername();
            server.broadcast(username + " has entered the chat.", this);
            serverGreeting();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (userConnected) {
            try {
                String input = handlerReader.readLine();
                System.out.println(username + " " + input);
                if (isCommand(input)) {
                    parseCommand(input);
                } else {
                    server.broadcast(username + ": " + input, this);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    public ArrayList getBlockedUsers() {
        return blockedUsers;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

