import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {
	
    public static void main(String[] args) {
        final int REGISTRYPORT = 1099;
        String registryHost;
        String serviceName = "ServiziParla";

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        /*CHECK PARAM*/
        if (args.length != 1) {
            System.out.println("Usage: ClientName <Host> ");
            System.exit(1);
        }
        registryHost = args[0];
        String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/" + serviceName;

        try {
            // REMOTE RMI CONN
            RemOp serverRMI = (RemOp) Naming.lookup(completeName);
            System.out.println("\nRichieste fino a EOF...");
            System.out.print("Servizio (A=Aggiungi stanza, E=Elimina utente): ");
            String service;

            //USER INTERACTION
            try {
                while ((service = stdIn.readLine()) != null) {
                    if (service.equals("A")){
                    	System.out.println("Nome Stanza:");
                    	String stanza = stdIn.readLine();
                    	
                    	System.out.println("Tipo Comunicazione (P/M):");
                    	char comunicazione = stdIn.readLine().charAt(0);
                    	
                    	System.out.println("Aggiungi Stanza");
                    	
                    	if (serverRMI.aggiungi_stanza(stanza, comunicazione)) {
                    		System.out.println("Success!");
                    	} else {
                    		System.out.println("Failure!");
                    	}
                    } else if (service.equals("E")){
                    	System.out.println("Nome Utente:");
                    	String nomeUtente = stdIn.readLine();
                    	
                    	Stanza[] result = serverRMI.elimina_utente(nomeUtente);
                        
                    	if (result == null) {
                    		System.out.println("NULL");
                    	} else {
                    		System.out.println("NOT NULL "+result.length);
                    	}
                    	
                    	for (int i = 0; i < result.length; i++) {
                    		System.out.println(result.toString());
                    	}
                    } else System.out.println("Operation not permitted.");

                    System.out.print("Servizio (A=Aggiungi stanza, E=Elimina utente): ");
                }
            }
            catch(IOException ex){
                System.out.println("IOException occurred. Exit");
                ex.printStackTrace();
                System.exit(2);
            }

        }
        catch (NotBoundException | MalformedURLException | RemoteException ex){
            if(ex instanceof  MalformedURLException){
                System.out.println("MalformedURLException occurred. Exit");
                System.exit(2);
            }
            else if (ex instanceof  RemoteException){
                System.out.println("Remote Exception occurred. Exit");
                System.exit(2);
            }
        }
    }
}