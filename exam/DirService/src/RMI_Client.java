/* Ritossa Andrea 0001020070 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class RMI_Client {
    public static void main(String[] args) {
        if (args.length != 1) {
        	System.out.println("Usage: RMI_Client <Host>");
        	System.exit(1);
        }
    	
    	final int PORT_REGISTRY = 1099;
        String registryHost = args[0];
        String serviceName = "DirService";
    	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    	String completeName = "//" + registryHost + ":" + PORT_REGISTRY + "/" + serviceName;
        
    	// System.out.println("CompleteName: " + completeName);
    	try {
    		/*
    		 * Remote RMI connection.
    		 */
            RMI_InterfaceFile rmiInterface = (RMI_InterfaceFile) Naming.lookup(completeName);
            
            System.out.print("Servizio (L=Lista, N=Numero, E=Exit): ");
            String service, nomeFile, parola;
            
            try {
            	/*
            	 * Remote RMI interaction.
            	 */
            	while ((service = stdIn.readLine()) != null) {
            		switch (service) {
                        case "L":
                        	System.out.println("Inserisci il nome direttorio:");
                            nomeFile = stdIn.readLine();
                            System.out.println("Inserisci la parola:");
                            parola = stdIn.readLine();
                            
                            try {
                                String[] files = rmiInterface.lista_nomi_file_contenenti_parola_in_linea(nomeFile, parola);
                                System.out.println("Files: ");
                                if (files != null) {
                                   for (String w : files)
                                    	System.out.println("\t" + w);
                                } else {
                                	System.out.println("Empty");
                                }
                            } catch (RemoteException e) {
                            	System.out.println("Files: "+e.getMessage());
                            }
                            break;
                        case "N":
                            System.out.println("Inserisci il nome file:");
                            nomeFile = stdIn.readLine();
                            System.out.println("Inserisci la parola:");
                            parola = stdIn.readLine();
                            int count = rmiInterface.conta_numero_linee(nomeFile, parola);
                            System.out.println("Numero di linee: " + count);
                            break;
                        case "E":
                            System.exit(0);
                    }
            		System.out.println("");
                    System.out.print("Servizio (L=Lista, N=Numero, E=Exit): ");
                } // while
            	
            } catch (IOException ex) {
            	System.out.println("IOException occurred.");
            	System.exit(2);
            }            
        } catch (NotBoundException | MalformedURLException | RemoteException ex){
            if(ex instanceof  MalformedURLException){
                System.out.println("MalformedURLException occurred. Exit");
                System.exit(2);
            }
            else if (ex instanceof  RemoteException){
                System.out.println("Remote Exception occurred. Exit");
                System.exit(2);
            }
        } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
            System.exit(3);
        }
    }
}
