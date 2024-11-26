package connections;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.HashMap;


public class Server {
	
    // sets and maps to store IDs, coord ID and IP addresses and port numbers
    private static Set<String> names = new HashSet<>();
    private static Set<String> coord = new HashSet<>();
    private static HashMap<String, String> clientIPs = new HashMap<String, String>();

     // The set of all the print writers for all the clients, used for broadcast.
    private static Set<PrintWriter> writers = new HashSet<>();

    ///listens for clients and accepts to the server 
    public static void main(String[] args) throws Exception {
        System.out.println("server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        }
    }

    /// client handler module
    private static class Handler implements Runnable {
        private String name;
        private String details;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;

        
        public Handler(Socket socket) {
            this.socket = socket;
        }

        
        public void run() {
        	
        	///defines inputs from clients and outputs from server 
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                // Keep requesting a name until we get a unique one.
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.nextLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                    	///stores name if unique
                        if (!name.isEmpty() && !names.contains(name)) {
                            names.add(name);
                    
                    ///requests clients IP address and stores it with their IDs
                    out.println("IP");
                    details = in.nextLine();
                    clientIPs.put(name, details);
                     
                            break;
                        }
                    }
                }

                ///sends signal to user to say name has been accepted and for chat room to be initated
                out.println("NAMEACCEPTED " + name);   
                
                /// sets coordinator 
                synchronized (coord) {
                	if(names.size() == 1) {coord.add(name);}
                    }
                ///tells first person they are coordinator
                if(coord.contains(name)){out.println("MESSAGE" + " You are the coordinator");}
                ///tells people when they join the coordinator
                else{out.println("MESSAGE" + " " + coord + " is the coordinator ");}
                ///tells clients someone has joined
                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + " has joined");
                    
                    
              
                    
                    
                }
                writers.add(out);

                // Accept messages from this client and broadcast them.
                while (true) {
                	String input = in.nextLine();
                	///removes client if they quit 
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }
                    /// sends signal to clients to get response 
                    else if (input.startsWith("active")) {
                    	for (PrintWriter writer : writers) {
                            writer.println("IS_ACTIVE");}}
                    ///gets clients IP and port when requested
                    else if(input.startsWith("request")){
                    	for (String clients : clientIPs.keySet()) {
                    		if (input.contains(clients)) {
                    			out.println("MESSAGE" + " " + clients + " IP and port number is " + clientIPs.get(clients));
                    		}
                    	}
                    }
                    ///responds with everyone is active if recieves a signal back from the clients
                    else if (input.startsWith("ACTIVE!") ) {out.println("MESSAGE" + " the members " + names + " are active on the server");}
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + ": " + input);
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
            	///removes clients who are leaving 
                if (out != null) {
                    writers.remove(out);
                }
                if (name != null) {
                    System.out.println(name + " is leaving");
                  ///removes ID from list and informs clients
                    names.remove(name);
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + " has left");
                    }
                  /// removes coordinators name if they are leaving and assigns new coordinator
                    if(coord.contains(name) && !names.isEmpty()) {coord.clear(); coord.add(names.iterator().next());
                    for (PrintWriter writer : writers) {
                    	writer.println("MESSAGE" + "" + coord + " is the new coordinator");}
                    }
                }
                try { socket.close(); } catch (IOException e) {}
            }
        }
    }
}