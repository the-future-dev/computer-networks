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
    // Here, we are assuming only one DiscoveryServer is istantiated at once.
	private static List<ServerInfo> serverList = 
        Collections.synchronizedList(new ArrayList<>());
    // TODO Controllare che usare due OptionalInt invece che due int non 
    // costituisca un overhead eccessivo rispetto al vantaggio di non dover
    // inizializzare a -1.
    private static OptionalInt DISCOVERY_PORT = OptionalInt.empty();
    private static OptionalInt REGISTRATION_PORT = OptionalInt.empty();
	
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
            DISCOVERY_PORT = OptionalInt.of(Integer.parseInt(args[0]));
            REGISTRATION_PORT = OptionalInt.of(Integer.parseInt(args[1]));
        } 
        catch (NumberFormatException e) {
            log("Usage: java <portaRichiesteClient: int> <portaRegistrazioneRS: int>"); 
            System.exit(1);
        }
        
        // Check arguments are valid port numbers
        if (DISCOVERY_PORT.getAsInt() <= 1024 || DISCOVERY_PORT.getAsInt() > 65535) {
            log("The discovery server port is not valid: " + args[0]);
            System.exit(2);
        }
        if (REGISTRATION_PORT.getAsInt() <= 1024 || REGISTRATION_PORT.getAsInt() > 65535) {
            log("The discovery server port is not valid: " + args[1]);
            System.exit(2);
        }
		
        // Initialize and open the sockets
        try {
            discoverySocket = new ServerSocket(DISCOVERY_PORT.getAsInt());
            registrationSocket = new ServerSocket(REGISTRATION_PORT.getAsInt());
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
            Thread discoveryThread = new Thread(new DiscoveryHandler(discoverySocket, serverList));
            Thread registrationThread = new Thread(new RegistrationHandler(registrationSocket, serverList));
            registrationThread.start();
            discoveryThread.start();
        }
        catch(Exception e) {
            log("Error during thread execution: " + e.getMessage());
            System.exit(3);
        }
        
        // Sockets are not closed since they are closed later by the child threads
	}
	
    // TODO Verificare che chiamare ogni volta questa funzione non costituisca
    // un overhead eccessivo
    public static void log(String msg)
    {
        System.out.println("[DiscoveryServer] " + msg);
    }

}

class DiscoveryHandler extends Thread 
{
	// TODO forse nome più specifico
    private static ServerSocket socket;
    private static Socket clientSocket;

    private static BufferedReader in;
    private static PrintWriter out;
    
    private String requestedFile, response;
    
    private static List<ServerInfo> servers;

    public DiscoveryHandler(ServerSocket socket, List<ServerInfo> servers) 
    {
        this.socket = socket;
        this.servers = servers;
    }

    @Override
    public void run() 
    {
        log("Discovery server running");
        while (true) {
            try {
                // Thread waits until a request arrives.
                clientSocket = socket.accept();
                log("Discovery request from address " + clientSocket.getInetAddress().getHostAddress());
                
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                		
                // Sending available files to the client.
                try {
                    out.println(servers.toString());
                    
                    // Reading the name of the file requested by the client 
                    requestedFile = in.readLine();
                    	
                    	// TODO Controllare che sia effettivamente necessaria quest'ulteriore validazione lato server
                    // Sending a response to the client: a file name or an error message
                    if (requestedFile.trim().length() != requestedFile.length() || requestedFile.length() < 1 || requestedFile.length() > 255){
                        response = "Invalid file name";
                    } 
                    else {
                        // Default message 
                        response = "Error 404: File not found";
                        // TODO Dal momento che non stiamo modificando la  lista dei server, non dovrebbe servire il blocco 
                        // synchronized, ma verificare.
                        for (ServerInfo server : servers) {
                            if (server.file.equals(requestedFile)) {
                                response = server.IP.getHostAddress() + "," + server.porta;
                                break;
                            }
                        }
                    }
                } 
                	// TODO Controllare che sia effettivamente il messaggio di errore più appropriato da mandare
                catch (IOException e) {
                    response = "Error: Invalid request: \n input=fileName \n output=<IP,port> or Error";
                }
                out.println(response);
                
                log("Request -> response: ("+requestedFile+") -> "+"("+response+")");

                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                log("Error handling client request: " + e.getMessage());
            }
        }
    }

    public static void log(String msg)
    {
    	DiscoveryServer.log(msg);
    }
}

class RegistrationHandler extends Thread {
    private final List<ServerInfo> servers;

    private ServerSocket socket;
    private Socket serverSocket;
    private BufferedReader in;
    private PrintWriter out;    
 
    private String registration, response;
    private String[] parts;

    private InetAddress addr;
    private ServerInfo serverInfo;

    public RegistrationHandler(ServerSocket socket, List<ServerInfo> servers) {
        this.socket = socket;
        this.servers = servers;
    }

    @Override
    public void run() 
    {
        log("Registration server running");
        while (true) {
            try {
                serverSocket = socket.accept();
                log("Registration request from address " + serverSocket.getInetAddress().getHostAddress());

                in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                out = new PrintWriter(serverSocket.getOutputStream(), true);
                	
                // Default
                response = "Operation failed";
                try {
                    // Reading the input: (de)registration  - IP - port - filename.txt (space-separated)  
                    registration = in.readLine();
                    parts = registration.split(" "); 
                    if (parts.length != 4) {
                        throw new IllegalArgumentException("Invalid input format");
                    } 
                    boolean registering = Boolean.parseBoolean(parts[0]);
                    addr = InetAddress.getByName(parts[1]);
                    serverInfo = new ServerInfo(addr, Integer.parseInt(parts[2]), parts[3]);
                    
                    // checking if this server's information is already present in the system.
                    int serverInfoIndex = -1;
                    // TODO Anche qui, controllare che non sia effettivamente da sincronizzare
                    // TODO Controllare che non esista un metodo built-in di ArrayList che faccia questa 
                    // operazione di ricerca in modo ottimale
                    	boolean serverAlreadyExists = servers.contains(serverInfo); 
                    for (int i = 0; i < servers.size(); i++) {
                        if (servers.get(i).equals(serverInfo)){
                            serverInfoIndex = i;
                            break;
                        }
                    }                        
                    	
                    	// Registering
                    if (registering) {
                        if (serverAlreadyExists) { 
                            servers.add(serverInfo);
                            log("Server registrato con: IP "+serverInfo.IP.getHostAddress()+
                            " porta "+ serverInfo.porta+" nomefile: "+serverInfo.file+"\n");
                            response = "Successful Registration";
                        } 
                        else {
                            log("Server Information already inside the array.");
                            response = "Sucessful operation, but server information already present.";
                        }
                    } 
                    	// Deregistering
                    else {
                        if (serverAlreadyExists) { 
                            serverInfo = servers.get(serverInfoIndex);
                            servers.remove(serverInfo);
                            response = "Successful De-Registration";
                            log("Server de-registrato con: IP "+serverInfo.IP.getHostAddress()+
                            " porta "+ serverInfo.porta+" nomefile: "+serverInfo.file+"");
                        } 
                        else {
                            log("Client requested de registering of server info not present.");
                            response = "Successful operation, but server information wasn't present already.";
                        }
                    }
                } 
                // In case of malformed input or potential unexpected behaviour using server.
                catch (NumberFormatException e ) {
                    response = "Error: invalid port format. Send: ip, port, filename. Each separated by a blank space";
                }
                catch(IllegalArgumentException e) {
                    response = "Error: invalid input format. Send: ip, port, filename. Each separated by a blank space";
                }
                catch (UnknownHostException e) {
                    response = "Error: validating the server address.";
                }
                catch (Exception e) {
                    response = "Error: unexpected behaviour. Send: ip, port, filename. Each separated by a blank space.";
                }
                out.println(response);

                serverSocket.close();
                in.close();
            } 
            catch (IOException e) {
                System.err.println("Error handling server registration: " + e.getMessage());
            }
        }
    }

    public static void log(String msg)
    {
    	DiscoveryServer.log(msg);
    }
}


