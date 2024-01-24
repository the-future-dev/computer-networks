import java.io.*;
import java.net.*;

class PutFileServerThread extends Thread {
    private Socket clientSocket = null;
    // Opzionalmente, anche questo potrebbe diventare un parametro (opzionale)!
    private int buffer_size = 4096;

    public PutFileServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        System.out.println("Attivazione figlio: " + Thread.currentThread().getName());

        DataInputStream inSock;
        DataOutputStream outSock;

        byte[] buffer = new byte[buffer_size];
        int cont = 0;
        int read_bytes = 0;
        DataOutputStream dest_stream = null;

        try {
            inSock = new DataInputStream(clientSocket.getInputStream());
            outSock = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException ioe) {
            System.out.println("Problemi nella creazione degli stream di input/output su socket: ");
            ioe.printStackTrace();
            return;
        }

        
        
    }

}// thread

public class Server {	
	
	final int tableSize = 30;
	public Object[][] table = new Object[tableSize][4]; //Targa, Patente, Tipo, Folder
	
	public Server() {
		for (int i = 0; i < tableSize; i++) {
			table[i][0] = "L";
			table[i][1] = "0";
			table[i][2] = "L";
			table[i][3] = "L";
		}
		
		/*
		 * Inizializzazione struttura dati.
		 */
		table[0][0] = "AN745NL";
		table[0][1] = "00003";
		table[0][2] = "auto";
		table[0][3] = "AN745NL_img/";
		
		table[1][0] = "FE457GF";
		table[1][1] = "00003";
		table[1][2] = "camper";
		table[1][3] = "FE457GF_img/";
		
		table[4][0] = "NU547P";
		table[4][1] = "40063";
		table[4][2] = "auto";
		table[4][3] = "NU547PL_img/";
		
		table[10][0] = "LR897AH";
		table[10][1] = "56832";
		table[10][2] = "camper";
		table[10][3] = "LR897AH_img/";

		table[20][0] = "MD506DW";
		table[20][1] = "00100";
		table[20][2] = "camper";
		table[20][3] = "MD506DW_img/";
	}
	
    public static void main(String[] args) throws IOException {
        int port = -1;

        try {
            if (args.length == 1) {
                port = Integer.parseInt(args[0]);
                // controllo che la porta sia nel range consentito 1024-65535
                if (port < 1024 || port > 65535) {
                    System.out.println("Usage: java Server [serverPort>1024]");
                    System.exit(1);
                }
            } else {
                System.out.println("Usage: java Server port");
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Problemi, i seguenti: ");
            e.printStackTrace();
            System.out.println("Usage: java Server port");
            System.exit(1);
        }

        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            System.out.println("Server: avviato ");
            System.out.println("Server: creata la server socket: " + serverSocket);
        } catch (Exception e) {
            System.err.println("Server: problemi nella creazione della server socket: " + e.getMessage());
            e.printStackTrace();
            serverSocket.close();
            System.exit(1);
        }
        
        try {
            while (true) {
                System.out.println("Server: in attesa di richieste...\n");

                try {
                    clientSocket = serverSocket.accept(); // bloccante!!!
                    System.out.println("Server: connessione accettata: " + clientSocket);
                } catch (Exception e) {
                    System.err.println("Server: problemi nella accettazione della connessione: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }

                try {
                    new PutFileServerThread(clientSocket).start();
                } catch (Exception e) {
                    System.err.println("Server: problemi nel server thread: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }
            } // while true
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Server: termino...");
            System.exit(2);
        }
    }
    
    private synchronized int elimina(String targa) {
    	if (targa == null) return -1;
    	for (int i = 0; i<targa.length(); i++) {
    		if (targa.charAt(i) < '0' || targa.charAt(i) > '9') {
    			return -1;
    		}
    	}
    	
    	boolean found = false;
    	for (int i = 0; (i < tableSize && !found); i++) {
    		if (table[i][0].equals(targa)) {
    			found = true;
    			table[i][0] = "L";
    			table[i][1] = "0";
    			table[i][2] = "L";
    			table[i][3] = "L";
    		}
    	}
    	
    	if (!found) {
    		return -2;
    	} else {
    		return 1;
    	}
	}
}
