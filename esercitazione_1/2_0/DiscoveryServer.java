import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DiscoveryServer {
	public static void main(String args[]) {
		/*
		 * Input parsing.
		 */
		if (args.length < 3 || (args.length % 2) != 1) {
			System.out.println("Usage: DiscoveryServer portaDs nomeFile_1 port_1 ... nomeFile_N port_N");
			System.exit(1);
		}
		int portaDs = -1;
		int porteRs [] = new int [(args.length - 1) / 2];
		String fileNameRs [] = new String [(args.length - 1) / 2];
		
		try {
			portaDs = Integer.parseInt(args[0]);
			for (int i = 1; i < args.length - 1; i += 2) {
				fileNameRs[i/2] = args[i];
				porteRs[i/2] = Integer.parseInt(args[i+1]);
				
				if (porteRs[i/2] < 1024 || porteRs[i/2] > 65535) {
					System.out.println("Port belongs to [1024, 65535]");
					System.exit(1);
				}
				if (porteRs[i/2] == portaDs) {
					System.out.println("Duplicate ports detected: DS port can't be same as RS port.");
					System.exit(1);
				}
				for (int j = 0; j < i/2; j++) {
					if(porteRs[j] == porteRs[i/2]) {
						System.out.println("Duplicate ports detected.");
						System.exit(1);
					}
				}
				if (!((new File(fileNameRs[i/2])).exists())) {
					System.out.println("A file inserted does not exist: "+ fileNameRs[i/2]);
					System.exit(1);
				}
			}
		} catch(Exception e) {
			System.out.println("Usage: DiscoveryServer portaDs:<int> nomeFile_1:<str> port_1:<int> ... nomeFile_N:<str> port_N:<int>");
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}
		
		
		/*
		 * DiscoveryServer's **datagram socket** initialization.
		 */
		ByteArrayInputStream biStream = null; 	DataInputStream diStream = null;		// in
		ByteArrayOutputStream boStream = null; 	DataOutputStream doStream = null;		// out
		DatagramSocket discoverySocket = null;	DatagramPacket discoveryPacket = null;	// socket
		byte[] buf = new byte[256];
		byte[] data = null;
		
		try {
			discoverySocket = new DatagramSocket(portaDs);
			discoveryPacket = new DatagramPacket(buf, buf.length);
			System.out.println("Creata la discovery socket: " + discoverySocket);
		}
		catch (IOException e) {
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.exit(1);
		}
		
		// launch of RowSwap Servers
		/*
		 * for (int i = 0; i < porteRs.length; i++) {
		 * }
		 */
		
		while (true) {
			/*
			 * Discovery Server as a name server: handling requests from clients.
			 * 	(fileName) => row swap server's port
			 */
			System.out.println("DS: Waiting for requests...");
			try {
				/*
				 * Datagram reception.
				 */
				discoveryPacket.setData(buf);
				discoverySocket.receive(discoveryPacket);
			} catch(IOException e) {
				System.err.println("Problemi nella ricezione del datagramma: " + e.getMessage());
				e.printStackTrace();
				continue;
			}
			
			int port = -1;
			try {
				/*
				 * Message interpretation.
				 */
				biStream = new ByteArrayInputStream(discoveryPacket.getData(), 0, discoveryPacket.getLength());
				diStream = new DataInputStream(biStream);
				String received = diStream.readUTF();
				for (int i = 0; i < fileNameRs.length; i++) {
					if (fileNameRs[i].equals(received)) {
						port = porteRs[i];
						break;
					}
				}
				if (port == -1) {
					System.out.println("File ["+received+"] not found: "+port);
				} else {
					System.out.println("File ["+received+"] found: "+port);
				}
			} catch(Exception e) {
				System.err.println("Problems in reading the request: " + e.getMessage());
				e.printStackTrace();
				continue;
			}
			
			try {
				/*
				 * Sending response.
				 */
				boStream = new ByteArrayOutputStream();
				doStream = new DataOutputStream(boStream);
				doStream.writeUTF(port + "");
				data = boStream.toByteArray();
				discoveryPacket.setData(data, 0, data.length);
				discoverySocket.send(discoveryPacket);
			} catch(Exception e) {
				System.err.println("Problems in sending the response: " + e.getMessage());
				e.printStackTrace();
			}
		} // while
		
		// discoverySocket.close();
	}
}
