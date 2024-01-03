import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {
	public static void main(String[] args) {
		final int REGISTRYPORT = 1099;
		String registryHost = null; // host remoto con registry
		String serviceName = "";
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		
		if (args.length != 2) {
			System.out.println("Sintassi: RMI_Registry_IP ServiceName");
			System.exit(1);
		}		
		registryHost = args[0];
		serviceName = args[1];
		System.out.println("Invio richieste a " + registryHost + " per il servizio di nome " + serviceName);
		
		try {
			String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/" + serviceName;
			RemOp serverRMI = (RemOp) Naming.lookup(completeName);
			System.out.println("ClientRMI: Servizio \"" + serviceName + "\" connesso");

			System.out.println("\nRichieste di servizio fino a fine file");

			String service;
			System.out.print("Servizio (C=Conta Righe, E=Elimina Righe): ");
			while ((service = stdIn.readLine()) != null) {
				try {
					if (service.equals("C")) {
						// Conta Righe con numero di parole maggiore di un dato numero
						System.out.print("Nome file: ");
						String nomeFile = stdIn.readLine();
						
						System.out.println("Minimum n of words for line: ");
						int numParole = Integer.parseInt(stdIn.readLine());
						
						int numOfLines = serverRMI.conta_righe(nomeFile, numParole);
						System.out.println(numOfLines >= 0 ?
													"Ci sono "+numOfLines+" righe papabili." :
													"Errore");
					} // end Conta Righe
					else  if (service.equals("E")) {
						// Elimina Riga
						System.out.print("Nome file: ");
						String nomeFile = stdIn.readLine();
						
						System.out.println("Numero linea: ");
						int numLinea = Integer.parseInt(stdIn.readLine());
						int response = serverRMI.elimina_riga(nomeFile, numLinea);
						
						System.out.println("Eliminazione = "+response);
					} // end Elimina Riga
					else
						System.out.println("Servizio non disponibile");
					
				} catch (RemoteException e) {
					System.err.println(e.getLocalizedMessage());
				}
				
				System.out.print("Servizio (C=Conta Righe, E=Elimina Righe): ");
			}
		} catch (NotBoundException nbe) {
			System.err.println("ClientRMI: il nome fornito non risulta registrato; " + nbe.getMessage());
			nbe.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			System.err.println("ClientRMI: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

	}

}
