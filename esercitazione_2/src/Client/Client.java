package Client;
import java.net.*;
import java.io.*;
import shared.FileUtility;

public class Client {

	public static void main(String[] args) throws IOException {
   
		InetAddress addr = null;
		int port = -1;
		
		try{ //check args
			if(args.length == 2){
				addr = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
			} else{
				System.out.println("Usage: java PutFileClient serverAddr serverPort");
				System.exit(1);
			}
		} //try
		// Per esercizio si possono dividere le diverse eccezioni
		catch(Exception e){
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out.println("Usage: java PutFileClient serverAddr serverPort");
			System.exit(2);
		}
		
		// oggetti utilizzati dal client per la comunicazione e la lettura del file
		// locale
		Socket socket = null;
		FileInputStream inFile = null;
		DataInputStream inSock = null;
		DataOutputStream outSock = null;
		File directory = null;
		String nomeDirectory = null;
        String esito;

		// creazione stream di input da tastiera
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		System.out
		    .print("PutFileClient Started.\n\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome directory: ");

		try{
			while ( (nomeDirectory=stdIn.readLine()) != null){
				// se il directory esiste creo la socket
				if((directory = new File(nomeDirectory)).exists()){
					// creazione socket
					try{
						socket = new Socket(addr, port);
						socket.setSoTimeout(30000);
						System.out.println("Creata la socket: " + socket);
					}
					catch(Exception e){
						System.out.println("Problemi nella creazione della socket: ");
						e.printStackTrace();
						System.out
							.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome directory: ");
						continue;
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					}

					// creazione stream di input/output su socket
					try{
						inSock = new DataInputStream(socket.getInputStream());
						outSock = new DataOutputStream(socket.getOutputStream());
					}
					catch(IOException e){
						System.out
							.println("Problemi nella creazione degli stream su socket: ");
						e.printStackTrace();
						System.out
							.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome directory: ");
						continue;
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					}
				}
				// se la richiesta non � corretta non proseguo
				else{
					System.out.println("Directory non presente nel direttorio corrente");
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome Directory: ");
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					continue;
				}

				// Sending the number of files to read
				outSock.writeInt(directory.list().length);
				System.out.println("\tSent the number of files");

                for (String nomeFile: directory.list()){
                    /* Invio file richiesto e attesa esito dal server */
				    // creazione stream di input da file
                    try{
                        inFile = new FileInputStream(nomeDirectory+'/'+nomeFile);
                    }
                    /*
                    * abbiamo gia' verificato che esiste, a meno di inconvenienti, es.
                    * cancellazione concorrente del file da parte di un altro processo, non
                    * dovremmo mai incorrere in questa eccezione.
                    */
                    catch(FileNotFoundException e){
                        System.out
                            .println("Problemi nella creazione dello stream di input da "
                                    + nomeFile + ": ");
                        e.printStackTrace();
                        System.out
                            .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
                        // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                        continue;
                    }
                    // trasmissione del nome
                    try{
                        outSock.writeUTF(nomeFile);
                        System.out.println("Inviato il nome del file " + nomeFile);
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
                    
                    // ricezione esito: nome file
                    // "attiva" => ok, vai col file
                    // "salta"  => il file esiste già, non passare il file
                    try{
                        esito = inSock.readUTF();
                        System.out.println("\tEsito trasmissione: " + esito);
                    }
                    catch(SocketTimeoutException ste){
                        System.out.println("Timeout scattato: ");
                        ste.printStackTrace();
                        socket.close();
                        System.out
                            .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
                        // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                        continue;          
                    }
                    catch(Exception e){
                        System.out
                            .println("Problemi nella ricezione dell'esito, i seguenti: ");
                        e.printStackTrace();
                        socket.close();
                        System.out
                            .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
                        continue;
                        // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                    }

                    if (esito.equalsIgnoreCase("salta")){
                        System.out.println("\t >"+nomeFile+" ignorato.");
                        continue;
                    }
					
					// outSock.writeLong(new File(nomeDirectory + '/' + nomeFile).length());
					long fileLength = inFile.available();
					outSock.writeLong(fileLength);
                    System.out.println("\t"+nomeFile+" is long:"+fileLength);
					
					// trasferimento file
					try{
						//FileUtility.trasferisci_a_linee_UTF_e_stampa_a_video(new DataInputStream(inFile), outSock);
						FileUtility.trasferisci_a_byte_file_binario_length(new DataInputStream(inFile), outSock, fileLength);
						inFile.close(); 			// chiusura file
						System.out.println("Trasmissione di " + nomeFile + " terminata ");
					}
					catch(SocketTimeoutException ste){
						System.out.println("Timeout scattato: ");
						ste.printStackTrace();
						socket.close();
						System.out
							.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
						continue;          
					}
					catch(Exception e){
						System.out.println("Problemi nell'invio di " + nomeFile + ": ");
						e.printStackTrace();
						socket.close();
						System.out
							.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
						continue;
					}
					
					// ricezione esito della trasmissione del contenuto del file
					try{
						esito = inSock.readUTF();
						System.out.println("Esito trasmissione: " + esito);
						System.out.println("Terminata la chiusura della socket: " + socket);
					}
					catch(SocketTimeoutException ste){
						System.out.println("Timeout scattato: ");
						ste.printStackTrace();
						socket.close();
						System.out
							.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
						continue;          
					}
					catch(Exception e){
						System.out
							.println("Problemi nella ricezione dell'esito, i seguenti: ");
						e.printStackTrace();
						socket.close();
						System.out
							.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
						continue;
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					}
				}

				socket.shutdownInput();				// chiudo la socket in downstream
				socket.shutdownOutput(); 			// chiusura socket in upstream

				// tutto ok, pronto per nuova richiesta
				System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
				
			}
			socket.close();
			System.out.println("PutFileClient: termino...");
		}
		// qui catturo le eccezioni non catturate all'interno del while
		// quali per esempio la caduta della connessione con il server
		// in seguito alle quali il client termina l'esecuzione
		catch(Exception e){
			System.err.println("Errore irreversibile, il seguente: ");
			e.printStackTrace();
			System.err.println("Chiudo!");
			System.exit(3); 
	    }
	} // main
} // PutFileClient
