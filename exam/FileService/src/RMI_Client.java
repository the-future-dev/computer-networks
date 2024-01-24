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
        String serviceName = "FileService";
    	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    	String completeName = "//" + registryHost + ":" + PORT_REGISTRY + "/" + serviceName;
        
    	// System.out.println("CompleteName: " + completeName);
    	try {
    		/*
    		 * Remote RMI connection.
    		 */
            RMI_InterfaceFile rmiInterface = (RMI_InterfaceFile) Naming.lookup(completeName);
            
            System.out.print("Servizio (L=Lista files, C=Conta linee, E=Exit): ");
            String service;
            
            try {
            	/*
            	 * Remote RMI interaction.
            	 */
            	String word, name;
            	while ((service = stdIn.readLine()) != null) {
                    switch (service) {
                        case "L":
                        	System.out.println("Inserisci il nome del direttorio:");
                            name = stdIn.readLine();
                            
                            System.out.println("Inserisci la parola cercata:");
                            word = stdIn.readLine();
                            
                            try {
                            	String[] nomi_files = rmiInterface.lista_nomi_file_contenenti_parola_in_linea(name, word);
                                
                                if (nomi_files != null) {
                                	System.out.println("Città: ");
                                    for (String w : nomi_files)
                                    	System.out.println("\t" + w);
                                } else {
                                	System.out.println("Empty directory");
                                }
                            } catch(RemoteException e) {
                            	System.out.println("Erorr: "+ e.getLocalizedMessage());
                            }
                            
                            break;
                        case "C":
                        	System.out.println("Inserisci il nome del file:");
                            name = stdIn.readLine();
                            
                            System.out.println("Inserisci la parola cercata:");
                            word = stdIn.readLine();
                            
                            try {
	                            int count = rmiInterface.conta_numero_linee(name, word);
	                            
	                            if (count >= 0) {
	                            	System.out.println("Numero di occorrenze della parola: " + count);
	                            } else {
	                            	System.out.println("Errore nella procedura: "+count);
	                            }
                            } catch (RemoteException e) {
                            	System.out.println("Error: "+e.getLocalizedMessage());
                            }
                            break;
                        case "E":
                            System.exit(0);
                    }
                    System.out.print("Servizio (L=Lista files, C=Conta linee, E=Exit): ");
                } // while
            	
            } catch (IOException ex) {
            	System.out.println("IOException occurred.");
            	ex.printStackTrace();
            	System.exit(2);
            }            
        } catch (NotBoundException | MalformedURLException | RemoteException ex){
            if(ex instanceof  MalformedURLException){
                System.out.println("MalformedURLException occurred. Exit");
                ex.printStackTrace();
                System.exit(2);
            }
            else if (ex instanceof  RemoteException){
                System.out.println("Remote Exception occurred. Exit");
                ex.printStackTrace();
                System.exit(2);
            }
        } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
            System.exit(3);
        }
    }
}
