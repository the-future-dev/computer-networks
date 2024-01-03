import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class RowSwapClient {

	public static void main(String[] args) {
		InetAddress serverAddress = null;
		int portDiscoveryServer = -1, portRowSwapServer = -1;
		String fileName = null;
		/*
		 * Input parsing.
		 */
		try {
			if (args.length != 3) {
				System.out.println("Usage: java RowSwapClient serverIp serverPort fileName");
				System.exit(1);
			}
			serverAddress = InetAddress.getByName(args[0]);
			portDiscoveryServer = Integer.parseInt(args[1]);
			fileName = args[2];
		} catch (UnknownHostException e){
			System.out.println("Problemi nella determinazione dell'endpoint del server : "+e.getLocalizedMessage());
			System.exit(2);
		}
		
		DatagramSocket socket = null; DatagramPacket packet = null;					// socket
		ByteArrayInputStream biStream = null; DataInputStream diStream = null;		// input
		ByteArrayOutputStream boStream = null;	DataOutputStream doStream = null;	// output
		byte[] buff = new byte[256];
		/*
		 * Row Sawp Client's **datagram socket** initialization.
		 */
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(3000);
			packet = new DatagramPacket(buff, buff.length, serverAddress, portDiscoveryServer);
			System.out.println("Row Swap Client avviato con socket: "+socket);
		} catch(SocketException e) {
			System.out.println("Problemi nell'inizializzazione della socket: "+e.getLocalizedMessage());
			System.exit(3);
		}
		
		/*
		 * DisoveryServer.
		 * 	(fileName) => portRowSwapServer
		 */
		try {
			// request
			boStream = new ByteArrayOutputStream();
			doStream = new DataOutputStream(boStream);
			doStream.writeUTF(fileName);
			buff = boStream.toByteArray();
			packet.setData(buff);
			socket.send(packet);
		}catch (IOException e) {
			System.out.println("Problemi nell'invio della richiesta da DS.");
			System.exit(4);
		}
		try {
			// response
			biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
			diStream = new DataInputStream(biStream);
			socket.receive(packet);
			String received = diStream.readUTF();
			portRowSwapServer = Integer.parseInt(received.trim());
			if (portRowSwapServer <= 0) {
				System.out.println("Port not found inside DiscoveryServer");
				System.exit(4);
			}
			System.out.println("Received port for file " + fileName + ": " + portRowSwapServer);
		} catch(IOException e) {
			System.out.println("Problemi nella ricezione della risposta da DS: "+e.getLocalizedMessage());
			System.exit(4);
		} catch(NumberFormatException e) {
			System.out.println("Problemi nella conversione della porta: "+e.getLocalizedMessage());
			System.exit(4);
		}
		
		socket.close();
	}

}
