package main.java.io.codeforall.bootcamp.javatars;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {

    private Socket clientSocket;
    private ServerSocket serverSocket;
    private CopyOnWriteArrayList<ServerHandler> handlerList;
    private ExecutorService serverThreads;
    private PrintWriter out;
    private BufferedReader in;
    private ServerHandler serverHandler;
    private int port;


    public Server(int port) {
        this.port = port;
        serverSetup();

        System.out.println("Binding to port " + port);
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Server started: " + serverSocket);
        while (true) {
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            System.out.println("Waiting for a client connection");
            ServerHandler myHandler = new ServerHandler(this, clientSocket);
            handlerList.add(myHandler);
            serverThreads.submit(myHandler);
            ThreadPoolExecutor pool = (ThreadPoolExecutor) serverThreads;
            System.out.println(pool.getCorePoolSize());
            System.out.println(pool.getActiveCount());
            System.out.println("Client accepted: " + clientSocket);
            for (ServerHandler handler: handlerList) {
                System.out.println(handler.getUsername());
            }
        }
    }

    public void serverSetup() {
        handlerList = new CopyOnWriteArrayList<>();
        serverThreads = Executors.newCachedThreadPool();
    }

    public void close() {
        try {
            if (clientSocket != null) {
                System.out.println("Closing client connection");
                clientSocket.close();
            }
            if (serverSocket != null) {
                System.out.println("Closing server socket");
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    public void showOnlineUsers(ServerHandler serverHandler) {
        try {
            PrintWriter writer = new PrintWriter(serverHandler.getClientSocket().getOutputStream(), true);
            writer.println("Online users:");
            for (ServerHandler handler : getHandlerList()) {
                writer.println("- " + handler.getUsername());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CopyOnWriteArrayList<ServerHandler> getHandlerList() {
        return handlerList;
    }

    public void broadcast(String input, ServerHandler sender) throws IOException {
        if (input.startsWith("/")) {
            return;
        }

        // Iterate through all connected clients
        for (ServerHandler recipient : getHandlerList()) {
            // Skip sending messages to the sender
            if (recipient.equals(sender)) {
                continue;
            }

            // Skip sending messages to blocked users
            if (sender.getBlockedUsers().contains(recipient.getUsername())) {
                continue;
            }

            // Skip sending messages from blocked users to users who blocked them
            if (recipient.getBlockedUsers().contains(sender.getUsername())) {
                continue;
            }

            // Send the message to the recipient
            PrintWriter recipientWriter = new PrintWriter(recipient.getClientSocket().getOutputStream(), true);
            recipientWriter.println(input);
        }
    }
    public boolean findHandlerByUsername(String username) {
        for (int i = 0; i < handlerList.size()-1; i++) {
            if (handlerList.get(i).getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
