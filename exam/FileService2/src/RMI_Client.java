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
        String serviceName = "FileService2";
    	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    	String completeName = "//" + registryHost + ":" + PORT_REGISTRY + "/" + serviceName;
        
    	try {
    		/*
    		 * Remote RMI connection.
    		 */
            RMI_InterfaceFile rmiInterface = (RMI_InterfaceFile) Naming.lookup(completeName);
            
            System.out.print("Servizio (L=Lista, N=Numero, E=Exit): ");
            String service;
            
            try {
            	/*
            	 * Remote RMI interaction.
            	 */
            	while ((service = stdIn.readLine()) != null) {
                    switch (service) {
                        case "L":
                        	System.out.println("Inserisci il nome del direttorio da analizzare:");
                        	String nomeDir = stdIn.readLine();
                        	String[] files;
                        	
                        	try {
                                files = rmiInterface.lista_filetesto(nomeDir);                        		
                        	} catch (RemoteException e) {
                        		System.out.println("Error in analyzing the dir");
                        	}
                            System.out.println("Files: ");
                            for (String w : files)
                            	System.out.println("\t" + w);
                            break;
                        case "N":
                            System.out.println("Inserisci la città:");
                            String city = stdIn.readLine();
                            System.out.println("Inserisci la via:");
                            String street = stdIn.readLine();
                            int count = rmiInterface.visualizza_numero(city, street);
                            System.out.println("Numero di calze: " + count);
                            break;
                        case "E":
                            System.exit(0);
                    }
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
