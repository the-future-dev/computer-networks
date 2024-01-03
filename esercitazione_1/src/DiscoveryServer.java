import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

// TODO (escludendo i to-do) Forse sono necessari meno commenti?

public class DiscoveryServer 
{
    // Assuming only one DiscoveryServer is istantiated at once.
	private static List<ServerInfo> serverList = 
        Collections.synchronizedList(new ArrayList<>());
    
    private static int DISCOVERY_PORT = -1;
    private static int REGISTRATION_PORT = -1;
	
	public static void main(String[] args) 
    {
        // Socket objects are declared here (and not at the top level of the 
        // class) because they are "internal" objects only used in the main 
        // method, while, for example, DISCOVERY PORT is global and final
        // because it is a global feature of the DiscoveryServer object.
        ServerSocket discoverySocket = null;
        ServerSocket registrationSocket = null;

        // Check number of arguments
        if (args.length != 2) {
            log("Usage: java <DISCOVERY_PORT: int> <REGISTRATION_PORT: int>"); 
            System.exit(1);
        }

        // Check arguments are integers
        try {
            DISCOVERY_PORT = Integer.parseInt(args[0]);
            REGISTRATION_PORT = Integer.parseInt(args[1]);
        } 
        catch (Exception e) {
            log("Usage: java <portaRichiesteClient: int> <portaRegistrazioneRS: int>"); 
            System.exit(1);
        }
        
        // Check arguments are valid port numbers
        if (DISCOVERY_PORT <= 1024 || DISCOVERY_PORT > 65535) {
            log("The discovery server port is not valid: " + args[0]);
            System.exit(2);
        }
        if (REGISTRATION_PORT <= 1024 || REGISTRATION_PORT > 65535) {
            log("The discovery server port is not valid: " + args[1]);
            System.exit(2);
        }
		
        // Initialize and open the sockets
        try {
            discoverySocket = new ServerSocket(DISCOVERY_PORT);
            registrationSocket = new ServerSocket(REGISTRATION_PORT);
        } 
        // TODO Verificare se la gestione di SecurityException è necessaria
        catch(IOException | SecurityException e) {
            log("Error initializing server sockets: " + e.getMessage());
            System.exit(3);
        }
        
        // Create and start:
        // - One thread to handle (de)registration requests
        // - One thread to handle discovery requests
        try {
            // Discovery Thread
            Thread discoveryThread = new Thread(() -> {
                log("lookup service running");
                while (true) {
                    Socket clientSocket = null;
                    try {
                        clientSocket = discoverySocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    }
                    // Error catching the request.
                    catch (IOException e) {
                        log("Error handling client request: " + e.getMessage());
                    }

                    String response, requestedFile = null;
                        try {
                            log("Discovery request from address " + clientSocket.getInetAddress().getHostAddress());
                            
                            // Sending the list of servers available to the client.
                            out.println(serverList.toString());
                            
                            // Reading the name of the file requested by the client 
                            requestedFile = in.readLine();
                            response = "404 File not found";
                            for (ServerInfo server : serverList) {
                                if (server.file.equals(requestedFile)) {
                                    response = server.IP.getHostAddress() + "," + server.porta;
                                    break;
                                }
                            }
                        }
                        // Error during communication: il messaggio espone errori interni, non è lo standard di sicurezza... Ok per gli obiettivi del corso.
                        catch (Exception e){
                            response = "Error: " + e.getMessage();
                        }

                        // Sending the response to client
                        out.println(response);
                        log("Request -> response: (" + requestedFile + ") -> (" + response + ")");

                }
            }, "Lookup ");

            // Registration Thread
            Thread registrationThread = new Thread(() -> {
                log("registration service running");
                try {
                    while(true){
                        Socket serverSocket = null;
                        try {
                            serverSocket = registrationSocket.accept();
                            BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                            PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
                        } catch (Exception e) {
                            log("Error handling server registration: " + e.getMessage());
                        }
                        log("Registration request from address " + serverSocket.getInetAddress().getHostAddress());
                            String registration = in.readLine(), parts[] = registration.split(" "), response;
                            try{
                                if (parts.length != 4) throw new IllegalArgumentException("Invalid input format");
                                
                                boolean registering = Boolean.parseBoolean(parts[0]);
                                InetAddress addr = InetAddress.getByName(parts[1]);
                                int port = Integer.parseInt(parts[2]);
                                String fileName = parts[3];
                                ServerInfo serverInfo = new ServerInfo(addr, port, fileName);
                                
                                if (registering) {
                                    if (!serverList.contains(serverInfo)) {
                                        serverList.add(serverInfo);
                                        response = "200 Successful Registration";
                                    } else {
                                        response = "200 Server Information Already Present";
                                    }
                                } else {
                                    if (serverList.remove(serverInfo)){
                                        response = "200 Successful De-Registration";
                                    } else {
                                        response = "404 Server Information Not Found";
                                    }
                                }
                            } catch(UnknownHostException | IllegalArgumentException e) {
                                // As before: security problem, ok for the purpose of the code.
                                response = "Error: " + e.getMessage();
                            }

                            out.println(response);
                            log(response);
                    } // end while
                } catch(Exception e) {
                    log("Error handling server registration: " + e.getMessage());
                }
            }, "Registration");
            registrationThread.start();
            discoveryThread.start();
        }
        catch(Exception e) {
            log("Error during thread execution: " + e.getMessage());
            System.exit(3);
        }
        
        // Ciclo infinito quindi questo codice non viene eseguito
        // discoverySocket.close();
        // registrationSocket.close();
	}
	
    // Standardizzazione del logging a scapito dell'overhead per avere una funzione ad hoc.
    public static void log(String msg)
    {
        String thread = Thread.currentThread().getName();
        System.out.println("[DiscoveryServer | "+thread+"] > " + msg);
    }

}
