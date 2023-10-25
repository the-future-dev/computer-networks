package Server;

import java.io.*;
import java.net.*;

import shared.FileUtility;

public class Server {
	public static final int PORT = 54321; // porta default per server

	public static void main(String[] args) throws IOException {
		// Porta sulla quale ascolta il server
		int port = -1, minLengthInBytes = -1;

		/* controllo argomenti */
		try {
			if (args.length == 2) {
				minLengthInBytes = Integer.parseInt(args[0]);
				port = Integer.parseInt(args[1]);
				// controllo che la porta sia nel range consentito 1024-65535
				if (port < 1024 || port > 65535) {
					System.out.println("Usage: java PutFileServerSeq minLengthInBytes or java PutFileServerSeq minLengthInBytes port");
					System.exit(1);
				}
			} else if (args.length == 1) {
				minLengthInBytes = Integer.parseInt(args[0]);
				port = PORT;
			} else {
				System.out
					.println("Usage: java PutFileServerSeq minLengthInBytes or java PutFileServerSeq minLengthInBytes port");
				System.exit(1);
			}
		} //try
		catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out
				.println("Usage: java PutFileServerSeq minLengthInBytes or java PutFileServerSeq minLengthInBytes port");
			System.exit(1);
		}

		/* preparazione socket e in/out stream */
		ServerSocket serverSocket = null;
		try {
//			serverSocket = new ServerSocket(port,2);
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			System.out.println("PutFileServerSeq: avviato ");
			System.out.println("Creata la server socket: " + serverSocket);
		}
		catch (Exception e) {
			System.err.println("Problemi nella creazione della server socket: "
					+ e.getMessage());
			e.printStackTrace();
			System.exit(2);
		}
		try {
			//ciclo infinito server
			while (true) {
				Socket clientSocket = null;
				DataInputStream inSock = null;
				DataOutputStream outSock = null;
				
				System.out.println("\nIn attesa di richieste...");
				try {
					clientSocket = serverSocket.accept();
					clientSocket.setSoTimeout(30000); //timeout altrimenti server sequenziale si sospende
					System.out.println("Connessione accettata: " + clientSocket + "\n");
				}
				catch (SocketTimeoutException te) {
					System.err
						.println("Non ho ricevuto nulla dal client per 30 sec., interrompo "
								+ "la comunicazione e accetto nuove richieste.");
					// il server continua a fornire il servizio ricominciando dall'inizio
					continue;
				}
				catch (Exception e) {
					System.err.println("Problemi nella accettazione della connessione: "
							+ e.getMessage());
					e.printStackTrace();
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo, se ci sono stati problemi
					continue;
				}
				
				//stream I/O e ricezione number of files to be read
				int numberOfFiles;
				try {
					inSock = new DataInputStream(clientSocket.getInputStream());
					outSock = new DataOutputStream(clientSocket.getOutputStream());
					numberOfFiles = inSock.readInt();
					System.out.println("\tReceived the number of files: "+numberOfFiles);
		        }
				catch(SocketTimeoutException ste){
					System.out.println("Timeout scattato: ");
					ste.printStackTrace();
					clientSocket.close();
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					continue;          
				}
				catch (IOException e) {
		        	System.out
		        		.println("Problemi nella creazione degli stream di input/output "
		        			+ "su socket: ");
		        	e.printStackTrace();
		        	// il server continua l'esecuzione riprendendo dall'inizio del ciclo
		        	continue;
		        }

				String nomeFile;
				for (int i = 0; i<numberOfFiles; i++){
					nomeFile = inSock.readUTF();
					System.out.println("Ricevuto in input: "+nomeFile+".");
					
					//elaborazione e comunicazione esito
					FileOutputStream outFile = null;
					String esito;
					if (nomeFile == null) {
						System.out.println("Problemi nella ricezione del nome del file: ");
						continue;
					} else {
						File curFile = new File(nomeFile);
						// controllo su file
						if (curFile.exists()) {
							try {
								esito = "salta";
							}
							catch (Exception e) {
								System.out.println("Problemi nella notifica di file esistente: ");
								e.printStackTrace();
								continue;
							}
						} 
						else esito = "attiva";
					}

					try{
						outSock.writeUTF(esito);
						System.out.println("\tInviato l'esito di "+nomeFile);
					}
					catch(Exception e){
						System.out.println("Problemi nell'invio del nome di " + nomeFile
							+ ": ");
						e.printStackTrace();
						System.out
							.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
						continue;
					}
					
					if(esito.equalsIgnoreCase("salta")){
						System.out.println("File "+nomeFile+" already exists. Skip\n");
					} else {
						long lengthFile;
						try {
							lengthFile = inSock.readLong();
						}
						catch (Exception e) {
							System.out
								.println("Problemi nella creazione degli stream di input/output "
									+ "su socket: ");
							e.printStackTrace();
							// il server continua l'esecuzione riprendendo dall'inizio del ciclo
							continue;
						}

						boolean fileLongEnough = lengthFile >= minLengthInBytes;
						if (fileLongEnough){
							//ricezione file
							try {
								outFile = new FileOutputStream(nomeFile);

								System.out.println("Ricevo il file " + nomeFile + ": \n");
								/**NOTA: la funzione consuma l'EOF*/
								FileUtility.trasferisci_a_byte_file_binario_length(inSock,
										new DataOutputStream(outFile), lengthFile);
								System.out.println("\nRicezione del file " + nomeFile
										+ " terminata\n");
								outFile.close();				// chiusura file 
								// ritorno esito positivo al client
								outSock.writeUTF(esito + ", file salvato lato server");
							}
							catch(SocketTimeoutException ste){
								System.out.println("Timeout scattato: ");
								ste.printStackTrace();
								clientSocket.close();
								System.out
									.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
								continue;          
							}        
							catch (Exception e) {
								System.err
									.println("\nProblemi durante la ricezione e scrittura del file: "
											+ e.getMessage());
								e.printStackTrace();
								clientSocket.close();
								System.out.println("Terminata connessione con " + clientSocket);
								continue;
							}
						} // File shorter than the size given in input by the user: minLengthInBytes. 
						else {
							System.out.println("File Shorter than the minimum length in bytes for a file. Skipped. \n");
							FileUtility.consumaByteStream(inSock, lengthFile);
							// ritorno esito positivo al client
							outSock.writeUTF(esito + ", file non salvato lato server: file too short.");
						}
					}
				}
				clientSocket.shutdownInput();	//chiusura socket (downstream)
				clientSocket.shutdownOutput();	//chiusura socket (upstream)
				System.out.println("\nTerminata connessione con " + clientSocket);
				clientSocket.close();
				
			} // while (true)
		}
		// qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il server termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
			// chiusura di stream e socket
			System.out.println("Errore irreversibile, PutFileServerSeq: termino...");
			System.exit(3);
		} finally {
			try {
				if (serverSocket != null){
					serverSocket.close();
				}
			} catch (IOException e) {
				System.err.println("Error closing the server socket in finally");
			}
		}
	} // main
} // PutFileServerSeq
