// DiscoveryServer.java

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;



public class DiscoveryServer {

	private static int portaRichiesteClient = -1;
	private static int portaRegistrazioneRS = -1;

	public static void main(String[] args) {

		/* Controllo argomenti */
		// We need precisely 2 arguments to initialize DiscoveryServer:
		// - portaRichiesteClient: int
		// - portaRegistrazione: int
		if (args.length != 2) {
			System.out.println("Usage: java DiscoveryServer portaDiscoveryServer file1 port1 ... fileN portN"); 
			System.exit(1);
		}
		portaRichiesteClient = Integer.parseInt(args[0]);
		portaRegistrazioneRS = Integer.parseInt(args[0]);
		if (portaRichiesteClient <= 1024 || portaRichiesteClient > 65535) {
			System.out.println("The discovery server port is not valid: " + args[0]);
			System.exit(2);
		}
		if (portaRegistrazioneRS <= 1024 || portaRegistrazioneRS > 65535) {
			System.out.println("The discovery server port is not valid: " + args[0]);
			System.exit(2);
		}

		//Inizializzazione e apertura Socket
		DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[256];

		try {
			socket = new DatagramSocket(PORT);
			packet = new DatagramPacket(buf, buf.length);
			System.out.println("DiscoveryServer avviato con socket port: " + socket.getLocalPort()); 
		} catch (SocketException e) {
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.exit(1);
		}

		try {
			String richiesta = null;
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			String risposta = null;
			byte[] data = null;

			while (true) {
				System.out.println("\nIn attesa di richieste...");

				// ricezione del datagramma
				try {
					packet.setData(buf);
					socket.receive(packet);
				} catch (IOException e) {
					System.err.println("Problemi nella ricezione del datagramma: "+ e.getMessage());
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}

				try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					richiesta = diStream.readUTF();
					System.out.println("Richiesto server per nome file: " + richiesta);
				} catch (Exception e) {
					System.err.println("Problemi nella lettura della richiesta: ");
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}

				/* Generazione della risposta */
				risposta = null;
				for (int i = 1; i <= validArgs; i+=2) {
					if (args[i].equals(richiesta)) {
						risposta = "" + args[i+1];
						break;
					}
				}

				// problemi lato server, i.e. non abbiamo trovato il file
				if (risposta == null) risposta = "File con nome: " + richiesta + " non trovato";

				System.out.println("Risposta: " + risposta);

				// invio della risposta
				try {
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(risposta);
					data = boStream.toByteArray();
					packet.setData(data, 0, data.length);
					socket.send(packet);
				} catch (IOException e) {
					System.err.println("Problemi nell'invio della risposta: "+ e.getMessage());
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}
			} // while
		}
		// qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il server termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("SwapCountServer: termino...");
		socket.close();
	}
}