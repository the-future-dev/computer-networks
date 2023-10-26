import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.StringTokenizer;

public class SwapServer {

	private static int port = -1;
	private static int discoveryRegistrationPort = -1;
	private static String discoveryServerIP;

	/*
	 * Input:
	 * 	a string containg the two row indexes separated by -
	 * 	ex: 1-2
	 * 
	 * Output:
	 *  1: success
	 * 
	 *  0: unexpected behavior
	 * 
	 * Error:
	 * -1: failed to delete the original file
	 * -2: failed to rename the tem file
	 * -3: row index out of bound  
	 * -4: input problems
	 * 
	 */
	public static void main(String[] args) {
		// Input validation
		if (args.length != 4) {
			System.err.println("Usage: java SwapServer <DiscoveryServer_IP> <DiscoveryServer_Registration_Port> <SwapServer_Port> <file>");
			System.exit(1);
		}

		String nomeFile = null;
		File file;

		try{
			discoveryServerIP = args[0];
			discoveryRegistrationPort = Integer.parseInt(args[1]);
			InetAddress addr = InetAddress.getByName(discoveryServerIP); // addr is not used but "prevenire è meglio che curare"
			port = Integer.parseInt(args[2]);
			nomeFile = args[3];

			if (!((file = new File(nomeFile)).exists())) {
				throw new FileNotFoundException(nomeFile);
			}
		} catch (NumberFormatException e){
			System.err.println("Error parsing the port.");
			System.err.println("Usage: java SwapServer <DiscoveryServer_IP> <DiscoveryServer_Registration_Port> <SwapServer_Port> <file>");
			System.exit(1);
		} catch(UnknownHostException e){
			System.err.println("Error parsing the address.");
			System.err.println("Usage: java SwapServer <DiscoveryServer_IP> <DiscoveryServer_Registration_Port> <SwapServer_Port> <file>");
			System.exit(1);
		} catch(FileNotFoundException e){
			System.err.println("The file passed does not exist: "+nomeFile+"\n");
			System.exit(2);
		}

		/*
		 * Server registration to the Name Server.
		 */
		try (Socket registrationSocket = new Socket(discoveryServerIP, discoveryRegistrationPort);
			PrintWriter out = new PrintWriter(registrationSocket.getOutputStream(), true)) {
			String registrationMessage = InetAddress.getLocalHost().getHostAddress() + " " + port + " " + nomeFile;
			out.println(registrationMessage);
			System.out.println("Registration:\n\t"+registrationMessage);
		} catch (IOException e) {
			System.err.println("Error registering with DiscoveryServer: " + e.getMessage());
			System.exit(4);
		}

		// Socket Initialization
		DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[256];

		try {
			socket = new DatagramSocket(port);
			packet = new DatagramPacket(buf, buf.length);
			System.out.println("SwapServer per file " + nomeFile + " avviato con socket port: " + socket.getLocalPort()); 
		} catch (SocketException e) {
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.exit(1);
		}

		try {
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			byte[] data = null;
			int result = 0, firstRow = 0, secondRow = 0;

			while (true) {
				System.out.println("\nSwapServer in attesa di richieste...");
				biStream = null;
				diStream = null;
				boStream = null;
				doStream = null;
				data = null;
				result = 0; firstRow = 0; secondRow = 0;
				Arrays.fill(buf, (byte) 0);
				/*
				 * Ricezione della richiesta di swap dal client.
				 */
				try {
					packet.setData(buf, 0, buf.length);
					socket.receive(packet);
					System.out.println("\n\nRequest received from: "+packet.getAddress().getHostAddress());
					System.out.println("\nPacket: "+packet.getData().toString());
				} catch (IOException e) {
					System.err.println("Problemi nella ricezione del datagramma: "+ e.getMessage());
					e.printStackTrace();
					continue; // Server continue providing the service.
				}

				// Estrazione prima e seconda riga da scambiare dal datagramma
				try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);

					String firstAndSecondRows = diStream.readUTF();
					StringTokenizer st = new StringTokenizer(firstAndSecondRows, "-");
					
					firstRow = Integer.parseInt((String)st.nextElement());
					secondRow = Integer.parseInt((String)st.nextElement());			
					
					System.out.println("FirstRow: " + firstRow + " SecondRow: " + secondRow);
					
					biStream.close();
					diStream.close();
				}
				catch (Exception e) {
					System.out.println("Problemi nella lettura del datagramma in input: ");
					e.printStackTrace();
					result = -4;
				}

				BufferedReader br = null;
				BufferedWriter bw = null;
				String line;
				int count = 0;
				String firstLineCache = null;
				String secondLineCache = null;

				if (result >= 0){
					// swap e invio dell'esito
					try {
						System.out.println("Swapping rows...");
						
						br = new BufferedReader(new FileReader(nomeFile));
						bw = new BufferedWriter(new FileWriter("temp"));
						
						// Read the file once to get the lines
						while ((line = br.readLine())!=null && (firstLineCache==null || secondLineCache==null)) {
							count++;
							if (count == firstRow) {
								firstLineCache = line;
							} else if(count == secondRow){
								secondLineCache = line;
							}
						}
						br.close();

						if (firstLineCache != null && secondLineCache != null){
							// Read the file a second time to swap the lines
							br = new BufferedReader(new FileReader(nomeFile));
							count = 0;
							while ((line = br.readLine())!=null){
								count++;
								if (count == firstRow) {
									line = secondLineCache;
								} else if(count == secondRow){
									line = firstLineCache;
								}
								bw.write(line + "\n");
							}
							bw.close();
							br.close();
							
							// Rename the file using temp
							File fileorig = new File(nomeFile);
							if (fileorig.delete()){
								file = new File(nomeFile);
								File tempFile = new File("temp");

								if (tempFile.renameTo(file)){ // if rename succeeds tempFile no longer exists.
									System.out.println("Operation concluded correctly.");
									result = 1;
								} else {
									System.out.println("Failed to rename the temp file.");
									result = -2;
								}
							} else {
								System.out.println("Failed to delete the original file.");
								result = -1;
							}
						} else {
							// bw.close();
							System.out.println("Failed to read the rows: index out of bound");
							result = -3;
						}
					} catch (IOException e) {
						System.err.println("Problemi, i seguenti: "+ e.getMessage());
						e.printStackTrace();
						result = -6;
					}
				}

				if (result < 0){
					File tempFile = new File("temp");
					if (tempFile.exists())
						tempFile.delete();
				}

				/*
				 * Send the operation result to the client.
				 */
				try{
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeInt(result);
					data = boStream.toByteArray();
					packet.setData(data, 0, data.length);
					socket.send(packet);

					// boStream.close();
					// doStream.close();
				}catch (Exception e){
					System.err.println("Problemi, i seguenti: "+ e.getMessage());
					e.printStackTrace();
					continue; // Server continue providing the service.
				}
			} // while

		}
		// qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il server termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("SwapServer: termino...");
		socket.close();
		
	}

}
