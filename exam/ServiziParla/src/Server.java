import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Server extends UnicastRemoteObject implements RemOp {
	private Server() throws RemoteException { super(); }
	
	private static final long serialVersionUID = 1L;
	private static final int NUM_STANZE = 5;
	private static Stanza[] stanze;
	
	@Override
	public boolean aggiungi_stanza(String nomeStanza, char tipoComunicazione) throws RemoteException {
		System.out.println();
		System.out.println("New Stanza");
		if (tipoComunicazione != 'P' && tipoComunicazione != 'M') {
			return false;
		}
		
		boolean result = false;
		int index;
		for (index = 0; index < NUM_STANZE && !result; index++) {
			if (stanze[index].getNome().equals("L")) {
				result = true;
			}
		}
		
		if (result) {
			index--;
			/* if we found a free spot */
			stanze[index].setNome(nomeStanza);
			stanze[index].setStato(tipoComunicazione);
			stanze[index].setS(false);
		
			System.out.println("Success:");
	        for (int i =0; i < NUM_STANZE; i++) System.out.println(stanze[i].toString());
	        
		} else {
			System.out.println("No space found");
		}
		System.out.println();
		return result;
	}

	@Override
	public Stanza[] elimina_utente(String nomeUtente) throws RemoteException {
		Stanza [] w_stanze;
		System.out.println("");
		System.out.println("Eliminazione "+nomeUtente);
		boolean indexes [] = new boolean [NUM_STANZE];
		for (int i = 0; i < NUM_STANZE; i++) indexes[i] = false;
		
		int index = 0;
		
		for (int i = 0; i < NUM_STANZE; i++) {
			for (int j = 0; j < Stanza.MAX_UTENTI; j++) {
				if (stanze[i].getUtenti()[j].equals(nomeUtente)) {
					stanze[i].setUtenteIdx("L", j);
					indexes[i] = true;
					index++;
				}
			}
		}
		
		System.out.println("Trovato "+index+" volte");
		
		w_stanze = new Stanza[index];
		
		
		for (int i = 0; i < NUM_STANZE; i++) {
			if (indexes[i]) {
				w_stanze[--index] = stanze[i];
				System.out.println("INDEX: "+index);
			}
		}
		if (index != 0) System.out.println("FUCK");
		else System.out.println("Success");
		
		for (int i =0; i < NUM_STANZE; i++) System.out.println(stanze[i].toString());
		
		System.out.println("");
		return w_stanze;
	}
	
	
	public static void main(String args []) {
		final int REGISTRYPORT = 1099;
        String registryHost = "localhost";
        String serviceName = "ServiziParla";
        String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/" + serviceName;
        
        /*
         * State Initialization
        */
        stanze = new Stanza[NUM_STANZE];
        for (int i = 0; i < NUM_STANZE; i++)
        	stanze[i] = new Stanza();
        
        
        stanze[0].setNome("Stanza 1");
        stanze[0].setStato('P');
        stanze[0].setUtenti(new String[]{"Ciao", "Hei", "B", "Quo", "L"});
        
        stanze[1].setNome("Stanza 2");
        stanze[1].setStato('P');
        stanze[1].setS(true);
        stanze[1].setUtenti(new String[]{"Pippo", "Pluto", "Orazio", "Eracle", "L"});
        
        stanze[2].setNome("Stanza 3");
        stanze[2].setStato('M');
        stanze[2].setS(false);
        stanze[2].setUtenti(new String[]{"Ciao", "Hei", "Quo", "Qua", "L"});
        
        // print all
        for (int i =0; i < NUM_STANZE; i++) System.out.println(stanze[i].toString());
        
        try {
        	/*
        	 * Registration to RMI
        	 */
        	Server serverRMI = new Server();
        	Naming.rebind (completeName, serverRMI);
        	
        } catch (RemoteException | MalformedURLException ex){
            if (ex instanceof  RemoteException){
                System.out.println("Remote Exception occurred. Exit");
                System.exit(2);
            }
            else if(ex instanceof  MalformedURLException){
                System.out.println("MalformedURLException occurred. Exit");
                System.exit(2);
            }
        }
        
	}

}
