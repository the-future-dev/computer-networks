// PutFileClient.java

import java.net.*;
import java.rmi.RemoteException;
import java.io.*;

public class Client {

	public static void main(String[] args) throws IOException {
   
		InetAddress addr = null;
		int port = -1;
		
		try{
			if(args.length == 2){
				addr = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
			} else{
				System.out.println("Usage: java PutFileClient serverAddr serverPort");
				System.exit(1);
			}
		} catch(Exception e){
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out.println("Usage: java PutFileClient serverAddr serverPort");
			System.exit(2);
		}
		
		Socket socket = null;
		DataInputStream inSock = null;
		DataOutputStream outSock = null;

		try{
			socket = new Socket(addr, port);
			socket.setSoTimeout(30000);
			System.out.println("Creata la socket: " + socket);
		}
		catch(Exception e){
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			return;
		}

		try{
			inSock = new DataInputStream(socket.getInputStream());
			outSock = new DataOutputStream(socket.getOutputStream());
		} catch(IOException e){
			System.out.println("Problemi nella creazione degli stream su socket: ");
			e.printStackTrace();
			System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
			socket.close();
			return;
		}
		
		try {
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Noleggia Sci Started.\n\n^D(Unix)/^Z(Win)+invio per uscire, scegli il servizio: ");
			
			String service;
			boolean exit = false;
			
			while (!exit && (service = stdIn.readLine()) != null) {
        		// #1 comunicazione del servizio;
        		
        		switch (service) {
                    case "L":
                    	
                        
                        break;
                    case "N":
                        
                    	break;
                    case "E":
                    	exit = true;
                    	
                        break;
                    	
                }
        		System.out.println("");
                System.out.print("Servizio (L=Lista, N=Numero, E=Exit): ");
            } // while
        	
        } catch (IOException ex) {
        	System.out.println("IOException occurred.");
        }
		inSock.close();
    	outSock.close();
    	socket.close();
    	System.exit(2);
	} // main
} // PutFileClient
