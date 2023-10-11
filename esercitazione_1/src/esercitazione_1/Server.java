package esercitazione_1;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class Server {

	public static void main(String[] args) throws Exception {
		// Demon process: always active.
		DatagramSocket socket = new DatagramSocket(9876); // PORT 9876
		System.out.println("SERVER| started");

		while(true) {
			// Receive packet
			byte[] ibuf = new byte[1024];
			DatagramPacket requestPacket = new DatagramPacket(ibuf, ibuf.length);
			socket.receive(requestPacket);

			// Read request
			ByteArrayInputStream byteStream = new ByteArrayInputStream(requestPacket.getData());
			DataInputStream dataStream = new DataInputStream(byteStream);
			String request = dataStream.readUTF();

			String responseText = null;			
			StringTokenizer tokenizer = new StringTokenizer(request);
			if (tokenizer.countTokens() !=2) responseText = "Error 400: Bad request - not two tokens";
			String nomeFile = tokenizer.nextToken();
			if (!nomeFile.endsWith(".txt")) responseText = "Error 400: Bad request - token does not end with .txt";
			int numeroLinea = -1;
			try{
				numeroLinea = Integer.parseInt(tokenizer.nextToken());
			} catch(NumberFormatException e) {
				responseText = "Error 400: Bad request - second token not a number";
			};
			if (responseText == null) {
				try(BufferedReader reader = new BufferedReader(new FileReader(nomeFile))){
					String line;
					int lineNumber = 0;
					System.out.print("File reading: ");
					while ((line = reader.readLine()) != null) {
						lineNumber++;
						if (lineNumber == numeroLinea) {
							responseText = line;
							break;
						}
						System.out.print(lineNumber+ " ");
					}
					System.out.println("");
					if (responseText == null) {
						responseText = "Error 400: Bad Request - line not found";
					}
				} catch (IOException e) {
					responseText = "Error 404: File Not Found";
				}
			}
			byte[] sendBuffer = responseText.getBytes();
			DatagramPacket responsePacket = new DatagramPacket(sendBuffer, sendBuffer.length, requestPacket.getAddress(), requestPacket.getPort());
			socket.send(responsePacket);
			System.out.println("SERVER| Request ["+request+"], Response ["+responseText+"]");
		}
		// socket.close();
	}

}