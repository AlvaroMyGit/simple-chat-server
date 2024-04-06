package main.java.io.codeforall.bootcamp.javatars;

public class Main {

    public static void main(String[] args) {
        int port = 9060; // Default port number

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Using default port 9090.");
            }
        }
        Server myServer = new Server(port);
    }
}
