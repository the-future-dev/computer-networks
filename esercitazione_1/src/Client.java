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

public class Client {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java DiscoveryClient <server-address> <server-port> <file-name>");
            System.exit(1);
        }

        String discoveryServerAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String fileName = args[2];
 
        Socket socket;
        byte[] data = null;

        PrintWriter out;
        BufferedReader in;
        String response = null, swapServerAddressStr = null;
        int swapServerPort = -1;
        InetAddress swapServerAddress = null;
        /*
         * Comunicazione con DiscoveryServer:
         *  Input: nome di un file di testo
         *  Output:
         *   - IP + porta afferenti al server corrispondente.
         */
        try {
            socket = new Socket(discoveryServerAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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

        BufferedReader stdIn = null;
        DataOutputStream doStream = null;
        ByteArrayOutputStream boStream = null;
        ByteArrayInputStream biStream = null;
		DataInputStream diStream = null;

        try{
            datagramPacket = null;
            buf = new byte[256];
            datagramPacket = new DatagramPacket(buf, buf.length, swapServerAddress, swapServerPort);
            boStream = new ByteArrayOutputStream();
            doStream = new DataOutputStream(boStream);
            stdIn = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.exit(1);
		}

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
                        doStream.writeUTF(firstAndSecondRows);
                        data = boStream.toByteArray();
                        datagramPacket.setData(data);
                        datagramSocket.send(datagramPacket);
                        System.out.println("Richiesta inviata a " + swapServerAddress + ", " + swapServerPort);
                        System.out.println("\t>"+ firstAndSecondRows);
                    } catch (IOException e) {
                        System.out.println("Problemi nell'invio della richiesta: ");
                        e.printStackTrace();
                        System.out.println("Inserisci seconda riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
                        continue;
                    }
    
                    //set buffer and receive answer
                    try {
                        datagramPacket.setData(buf);
                        datagramSocket.receive(datagramPacket);
                        // sospensiva solo per i millisecondi indicati, dopo solleva una SocketException
                    } catch (IOException e) {
                        System.out.println("Problemi nella ricezione del datagramma: ");
                        e.printStackTrace();
                        System.out.println("Inserisci seconda riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
                        continue;
                        // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                    }
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
                        // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                    }
                    // tutto ok, pronto per nuova richiesta
                    System.out.println("Inserisci prima riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
                } // while
            }
            // qui catturo le eccezioni non catturate all'interno del while
            // in seguito alle quali il client termina l'esecuzione
            catch (Exception e) {
                e.printStackTrace();
            }
    }
}
