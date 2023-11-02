import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;

public class SwapClient 
{
    private static Socket socket;
    private static byte[] data = null;

    private static PrintWriter out;
    private static BufferedReader in;
    private static String response = null, swapServerAddressStr = null;
    private static int swapServerPort = -1;
    private static InetAddress swapServerAddress = null;

    private static BufferedReader stdIn = null;
    private static DataOutputStream doStream = null;
    private static ByteArrayOutputStream boStream = null;
    private static ByteArrayInputStream biStream = null;
	private static DataInputStream diStream = null;

    private static String discoveryServerAddress = null;
    private static int serverPort = -1;
    private static String fileName = null;

    public static void main(String[] args) 
    {
        if (args.length != 2) {
            System.out.println("Usage: java DiscoveryClient <server-address> <server-port>");
            System.exit(1);
        }

        discoveryServerAddress = args[0];
        serverPort = Integer.parseInt(args[1]);
        // fileName is read from input // fileName = args[2]
 
        try{
            stdIn = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.exit(1);
		}

        /*
         * Comunicazione con DiscoveryServer:
         *  Output: lista dei files disponibili
         *  Input: nome di un file di testo
         *  Output: IP + porta afferenti al server corrispondente.
         */
        try {
            socket = new Socket(discoveryServerAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            response = in.readLine();
            System.out.println("Files available: "+ response);
            System.out.println("Insert the filename file:");
            fileName = stdIn.readLine();
            
            out.println(fileName);
            response = in.readLine();
            if (response.equalsIgnoreCase("Error 404: File not found")){
                System.err.println("File not found on the Discovery Server. SwapServer hasn't registered: is it running?");
                System.exit(2);
            }
            StringTokenizer st = new StringTokenizer(response, ",");
            System.out.println("ST: "+st.countTokens());
            swapServerAddressStr = st.nextToken();
            swapServerPort = Integer.parseInt(st.nextToken());
            System.out.println(response);
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            response = null;
        } catch (Error e){
            System.err.println("Error communicating witht the server.\n\n"+e.getStackTrace());
        }

        if (response != null) {
            System.out.println("Server response: " + response);
        }
        try {
            System.out.println(">>"+swapServerAddressStr+"\n");
            swapServerAddress = InetAddress.getByName(swapServerAddressStr);
        } catch (Exception e) {
            System.err.println("Error interpreting the address: "+e.getStackTrace());
            System.exit(5);
        }

        if( swapServerPort == -1 || swapServerAddress == null) {
            System.err.println("Error reading swapserver port and address.");
            System.exit(4);
        }

        DatagramSocket datagramSocket = null;
		DatagramPacket datagramPacket = null;
        byte[] buf = new byte[256];
        try {
			datagramSocket = new DatagramSocket();
			datagramSocket.setSoTimeout(30000);
			datagramPacket = new DatagramPacket(buf, buf.length, swapServerAddress, swapServerPort);
			System.out.println("\nSwapClient: avviato");
			System.out.println("Creata la socket: " + datagramSocket);
		}
		catch (SocketException e) {
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.out.println("SwapClient: interrompo...");
			System.exit(1);
		}


        buf = new byte[256];
        System.out.println("Inserisci prima riga da scambiare contenuta nel file " + fileName +
			" oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
		
            try {
                String firstRow = null;
                String secondRow = null;
                while ((firstRow = stdIn.readLine()) != null) {
                    System.out.println("Inserisci seconda riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
                    secondRow = stdIn.readLine();
                    String firstAndSecondRows = firstRow + "-" + secondRow;
                    // String firstAndSecondRows = firstRow + "/" + secondRow; // a malformatted input should return -4

                    //Send request
                    try {
                        // Create new outputstream to send the rows to be swapped
                        boStream = new ByteArrayOutputStream();
                        doStream = new DataOutputStream(boStream);
                        datagramPacket = new DatagramPacket(buf, buf.length, swapServerAddress, swapServerPort);

                        // Send rows
                        doStream.writeUTF(firstAndSecondRows);
                        data = boStream.toByteArray();
                        datagramPacket.setData(data);
                        datagramSocket.send(datagramPacket);
                        System.out.println("Richiesta inviata a " + swapServerAddress + ", " + swapServerPort);
                    } catch (IOException e) {
                        System.out.println("Problemi nell'invio della richiesta: ");
                        e.printStackTrace();
                        System.out.println("Inserisci seconda riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
                        continue;
                    }
    
                    //Receive SwapServer answer
                    try {
                        datagramPacket.setData(buf);
                        datagramSocket.receive(datagramPacket);
                    } catch (IOException e) {
                        // SocketException includes timer out.
                        System.out.println("Problemi nella ricezione del datagramma: ");
                        e.printStackTrace();
                        System.out.println("Inserisci seconda riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
                        continue;
                    }

                    //Swap righe
                    try {
                        biStream = new ByteArrayInputStream(datagramPacket.getData(), 0, datagramPacket.getLength());
                        diStream = new DataInputStream(biStream);
                        int esitoScambioRighe = diStream.readInt();
                        System.out.println("Esito scambio righe: " + esitoScambioRighe);
                    } catch (IOException e) {
                        System.out.println("Problemi nella lettura della risposta: ");
                        e.printStackTrace();
                        System.out.println("Inserisci seconda riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
                        continue;
                    }

                    // inizio nuova richiesta allo stesso SwapServer
                    System.out.println("Inserisci prima riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
                } // while
            }
 
            catch (Exception e) {
                e.printStackTrace();
            }
    }
}
