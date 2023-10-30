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

public class DiscoveryServer {

	private static int portaRichiesteClient = -1;
	private static int portaRegistrazioneRS = -1;
	static List<ServerInfo> serverList = null;
	
	public static void main(String[] args) {
		/*
		 * Argument checks.
		 */
		if (args.length != 2) {
			System.out.println("Usage: java <portaRichiesteClient: int> <portaRegistrazioneRS: int>"); 
			System.exit(1);
		}

        try{
            portaRichiesteClient = Integer.parseInt(args[0]);
            portaRegistrazioneRS = Integer.parseInt(args[1]);
        } catch(NumberFormatException e){
			System.out.println("Usage: java <portaRichiesteClient: int> <portaRegistrazioneRS: int>"); 
			System.exit(1);
        }

		if (portaRichiesteClient <= 1024 || portaRichiesteClient > 65535) {
			System.out.println("The discovery server port is not valid: " + args[0]);
			System.exit(2);
		}
		if (portaRegistrazioneRS <= 1024 || portaRegistrazioneRS > 65535) {
			System.out.println("The discovery server port is not valid: " + args[1]);
			System.exit(2);
		}
		
		/*
		 *  Initialization and socket opening.
		 */
		try {
			ServerSocket discoverySocket = new ServerSocket(portaRichiesteClient);
			ServerSocket registrationSocket = new ServerSocket(portaRegistrazioneRS);
			
            serverList = Collections.synchronizedList(new ArrayList<>());

			Thread discoveryThread = new Thread(new DiscoveryHandler(discoverySocket, serverList));
	        Thread registrationThread = new Thread(new RegistrationHandler(registrationSocket, serverList));

            registrationThread.start();
            discoveryThread.start();
		} catch(IOException e) {
			System.err.println("Error initializing server sockets: " + e.getMessage());
	        System.exit(3);
		}
	}

}

class DiscoveryHandler extends Thread {
    private ServerSocket socket;
    private Socket clientSocket;

    BufferedReader in;
    PrintWriter out;
    
    String requestedFile, response;

    private final List<ServerInfo> servers;

    public DiscoveryHandler(ServerSocket socket, List<ServerInfo> servers) {
        this.socket = socket;
        this.servers = servers;
    }

    @Override
    public void run() {
        System.out.println("DiscoveryHandler running.\n");
        while (true) {
            try {
                // Thread waits until a request arrives.
                clientSocket = socket.accept();
                
                // Initialization of the resources needed.
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                System.out.println("Discovery request initialized from "+clientSocket.getInetAddress().getHostAddress());

                try {
                    out.println(servers.toString());

                    // Reading the input: the name of a file. 
                    requestedFile = in.readLine();

                    // file name validation:
                    if (requestedFile.trim().length() != requestedFile.length() || requestedFile.length() < 1 || requestedFile.length() > 255){
                        response = "Invalid file name";
                    } // correct file name
                    else {
                        // Does it exists a server that enables you to modify that file? 
                        response = "Error 404: File not found"; // Handling the file not existing. N.B: check client side before proceeding
                        synchronized (servers) {
                            for (ServerInfo server : servers) {
                                if (server.file.equals(requestedFile)) {
                                    response = server.IP.getHostAddress() + "," + server.porta;
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    response = "Error: Invalid request: \n input=fileName \n output=<IP,port> or Error";
                }

                // Sending the output
                out.println(response);

                System.out.println("\tResponse: ("+requestedFile+") > "+"("+response+")\n");

                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error handling client request: " + e.getMessage());
            }
        }
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
    public void run() {
        System.out.println("RegistrationHandler running.\n");
        while (true) {
            try {
                // Thread waits until a request arrives.
                serverSocket = socket.accept();
                System.out.println("Registration initialized.");

                // Initialization of the resources needed.
                in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                out = new PrintWriter(serverSocket.getOutputStream(), true);

                response = "Unsuccessful operation";

                try{
                    // Reading the input: registration/de-registration - IP - port - filename.txt 
                    registration = in.readLine();
                    parts = registration.split(" ");  // Assuming space-separated values
                    if (parts.length != 4){
                        throw new IllegalArgumentException("Invalid input format");
                    }
                    boolean registering = Boolean.parseBoolean(parts[0]);
                    addr = InetAddress.getByName(parts[1]);
                    serverInfo = new ServerInfo(addr, Integer.parseInt(parts[2]), parts[3]);

                    // checking if this server informations are already present in the system.
                    int serverInfoIndex = -1;
                    synchronized (servers) {
                        for (int i = 0; i < servers.size(); i++){
                            if (servers.get(i).equals(serverInfo)){
                                serverInfoIndex = i;
                                break;
                            }
                        }                        
                    }

                    if (registering){
                        // registering if needed
                        if (serverInfoIndex == -1){ // server not present in the array
                            servers.add(serverInfo);
                            System.out.println("Server registrato con: IP "+serverInfo.IP.getHostAddress()+
                            " porta "+ serverInfo.porta+" nomefile: "+serverInfo.file+"\n");
                            response = "Successful Registration";
                        } else {
                            System.out.println("Server Information already inside the array.");
                            response = "Sucessful operation, but server information already present.";
                        }
                    } else {
                        // de-registeringminazione e la de-registrazione dinamica
degli SR presso il DiscoveryServe
                        if (serverInfoIndex != -1){ // server present in the array.
                            serverInfo = servers.get(serverInfoIndex);
                            servers.remove(serverInfo);
                            response = "Successful De-Registration";
                            System.out.println("Server de-registrato con: IP "+serverInfo.IP.getHostAddress()+
                            " porta "+ serverInfo.porta+" nomefile: "+serverInfo.file+"\n");
                        } else {
                            System.out.println("Client requested de registering of server info not present.");
                            response = "Successful operation, but server information wasn't present already.";
                        }
                    }
                } // In case of malformed input or potential unexpected behaviour using server.
                catch (NumberFormatException e ){
                    response = "Error: invalid port format. Send: ip, port, filename. Each separated by a blank space";
                }
                catch(IllegalArgumentException e){
                    response = "Error: invalid input format. Send: ip, port, filename. Each separated by a blank space";
                }
                catch (UnknownHostException e){
                    response = "Error: validating the server address.";
                }
                catch (Exception e){
                    response = "Error: unexpected behaviour. Send: ip, port, filename. Each separated by a blank space.";
                }

                out.println(response);

                // Resource closure
                serverSocket.close();
                in.close();
            } catch (IOException e) {
                System.err.println("Error handling server registration: " + e.getMessage());
            }
        }
    }
}


