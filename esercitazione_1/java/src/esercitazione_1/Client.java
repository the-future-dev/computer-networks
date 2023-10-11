package esercitazione_1;

import java.io.*;
import java.net.*;

public class Client {
	public static void main(String args[]) throws Exception{
		DatagramSocket socket = new DatagramSocket();
		socket.setSoTimeout(30000); // Timeout of 30 seconds

		// User Input: [filename <String>, lineNumber <int>]
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String filename;
		Integer lineNumber;
        do {
	        System.out.println("Nome del file: ");
			filename = stdIn.readLine();
			System.out.println("Numero di linea: ");
			lineNumber = Integer.parseInt(stdIn.readLine());			
		} while (filename == null && lineNumber == null);
		
        String request = filename + " " + lineNumber;

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);
		dataStream.writeUTF(request);

        InetAddress serverAddress = InetAddress.getByName("localhost");
		DatagramPacket packet = new DatagramPacket(byteStream.toByteArray(), byteStream.size(), serverAddress, 9876);
		socket.send(packet);
		
		byte[] receiveBuffer = new byte[1024];
		DatagramPacket responsePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
		socket.receive(responsePacket);
		String responseString = new String(responsePacket.getData(), 0, responsePacket.getLength());
		System.out.println("CLIENT| "+ responseString);
		socket.close();
	}
}
