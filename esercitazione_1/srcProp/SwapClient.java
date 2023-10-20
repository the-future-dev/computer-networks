import java.io.*;
import java.net.*;

public class SwapClient 
{	
	public static void main(String[] args) 
	{	
		final int SO_TIMEOUT = 30000;
		
		// I valori dummy di inizializzazione servono a evitare che il parser dice che potrebbero non essere inizializzati 
		// (di fatto se si verifica un errore per cui i valori non possono essere inizializzati il programma termina).
		// Lo stesso vale per le variabili più in avanti
		InetAddress discoveryServerAddesss = null;
		int discoveryServerPort = -1;
		String fileName = "";

		// Controllo argomenti
		try {
			if (args.length == 3) {
				discoveryServerAddesss = InetAddress.getByName(args[0]);
				discoveryServerPort = Integer.parseInt(args[1]);
				fileName = args[2];
				System.out.println("Interrogo il discovery server:");
				System.out.println("Indirizzo: " + args[0]);
				System.out.println("Porta: " + args[1]);
				System.out.println("File: " + args[2]);
			} else {
				System.err.println("Usage: java SwapClient <ipDiscoveryServer> <portDiscoveryServer> <fileName>");
				System.exit(1);
			}
		}
		catch (Exception e) {
			System.err.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.exit(1);
		}

		// Creazione e inizializzazione socket e pacchetto
		DatagramSocket datagramSocket = null;
		DatagramPacket datagramPacket = null;
		byte[] packetBuffer = new byte[256];
		try {
			datagramSocket = new DatagramSocket();
			datagramSocket.setSoTimeout(SO_TIMEOUT);
			datagramPacket = new DatagramPacket(packetBuffer, packetBuffer.length, discoveryServerAddesss, discoveryServerPort);
			System.out.println("\nSwapClient: avviato");
			System.out.println("Creata la socket: " + datagramSocket);
		}
		catch (SocketException e) {
			System.err.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.out.println("SwapClient: interrompo...");
			System.exit(1);
		}

		int swapServerPort = -1;
		ByteArrayOutputStream boStream = null;
		DataOutputStream doStream = null;
		String risposta = null;
		ByteArrayInputStream biStream = null;
		DataInputStream diStream = null;
		byte[] data = null;
		try {
			
			// Invio pacchetto a DiscoveryServer per ricevere la porta a del mio server
			try {
				boStream = new ByteArrayOutputStream();
				doStream = new DataOutputStream(boStream);
				doStream.writeUTF(fileName);
				data = boStream.toByteArray();
				datagramPacket.setData(data);
				datagramSocket.send(datagramPacket);
				System.out.println("Richiesta inviata a " + discoveryServerAddesss + ", " + discoveryServerPort);
			}
			catch (IOException e) {
				System.out.println("Problemi nell'invio della richiesta: ");
				e.printStackTrace();
			}

			// Attesa e ricezione della risposta (bloccante solo per SO_TIMEOUT millisecondi)
			try {
				datagramPacket.setData(packetBuffer);
				datagramSocket.receive(datagramPacket);
			}
			catch (IOException e) {
				System.out.println("Problemi nella ricezione del datagramma: ");
				e.printStackTrace();
			}

			// Estrazione risposta (porta o errore)
			try {
				// TODO Modificare in modo che possa essere letto anche l'indirizzo del nuovo server
				biStream = new ByteArrayInputStream(datagramPacket.getData(), 0, datagramPacket.getLength());
				diStream = new DataInputStream(biStream);
				risposta = diStream.readUTF();
				System.out.println("Risposta(porta): " + risposta);
			}
			catch (IOException e) {
				System.out.println("Problemi nella lettura della risposta: ");
				e.printStackTrace();
			}
			
			// Parsing delle risposta
			if ( risposta.equals("File non trovato") ) {
				System.out.println("Errore, il seguente: ");
				System.out.println(risposta + "\nEsco...");
				datagramSocket.close();
				System.exit(4);
			} else {
				swapServerPort = Integer.parseInt(risposta);
			}
		}
		catch (Exception e) {
			System.out.println("Problemi nella ricezione dal Discovery Server: esco...");
			e.printStackTrace();
			System.exit(5);
		}
		
		// Inizializzo le variabili per la comunicazione
		datagramPacket = new DatagramPacket(packetBuffer, packetBuffer.length, discoveryServerAddesss, swapServerPort);
		boStream = new ByteArrayOutputStream();
		doStream = new DataOutputStream(boStream);
		
		// Leggo le righe da scambiare
		BufferedReader standardInputReader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Inserisci prima riga da scambiare contenuta nel file " + fileName +
			" oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
		try {
			String firstRow = null;
			String secondRow = null;
			while ((firstRow = standardInputReader.readLine()) != null) {
				System.out.println("Inserisci seconda riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
				secondRow = standardInputReader.readLine();
				String firstAndSecondRows = firstRow + "-" + secondRow;

				//Send request
				try {					
					doStream.writeUTF(firstAndSecondRows);
					data = boStream.toByteArray();
					datagramPacket.setData(data);
					datagramSocket.send(datagramPacket);
					System.out.println("Richiesta inviata a " + discoveryServerAddesss + ", " + swapServerPort);
				} catch (IOException e) {
					System.out.println("Problemi nell'invio della richiesta: ");
					e.printStackTrace();
					System.out.println("Inserisci seconda riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
					continue;
				}

				//set buffer and receive answer
				try {
					datagramPacket.setData(packetBuffer);
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
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("\nSwapClient: termino...");
			datagramSocket.close();
		}
	}
}