/* Ritossa Andrea 0001020070 */

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMI_Server extends UnicastRemoteObject implements RMI_InterfaceFile{
	private static final long serialVersionUID = 1L;
	private static Calza[] portaCalze;
	
	private RMI_Server() throws RemoteException{ super(); }
	
	@Override
	public String[] visualizza_lista() throws RemoteException {
		String[] cities = new String[portaCalze.length];
		String w;
		int index = 0;
		
		for (int i = 0; i < portaCalze.length; i++) {
			w = portaCalze[i].getCittà();
			boolean duplicato = false;
			
			for (int j = 0; j < index; j++) {
				if (w.equals(cities[j])) {
					duplicato = true;
				}
			}
			
			if (!duplicato) {
				cities[index] = w;
				index++;
			}
		}
		
		// Squeeze the array
		String[] temp = new String[index];
		for (int i = 0; i < index; i++) {
			temp[i] = cities[i];
		}
		
        return temp;
    }

	@Override
	public int visualizza_numero(String citta, String via) throws RemoteException {
		if (citta == null || via == null)
			throw new RemoteException("Invalid inputs: null");
		
		int count = 0;
        for (Calza calza : portaCalze) {
            if (calza.getCittà().equals(citta) && calza.getVia().equals(via))
                count++;
        }
        return count;
    }
	
	private static void inserisciCalza(int index, String id, Tipo tipo, boolean carbone, String citta, String via, String messaggio) {
		try {
			portaCalze[index] = new Calza(id, tipo, carbone, citta, via, messaggio);
			System.out.println("Inserita nuova calza: " + portaCalze[index].toString());
		}catch(Exception e) {
			System.out.println("Errore - calza non inserita: "+e.getMessage());
		}
	}
	
	private static String[] visualizzaCalze(Tipo tipo, boolean carbone){
		int index = 0;
		for (Calza calza : portaCalze)
			if (calza.getTipo().equals(tipo) && calza.getCarbone() == carbone)
				index++;
		
		String[] calze = new String[index];
		
		index = 0;
		for (Calza calza : portaCalze) {
			if (calza.getTipo().equals(tipo) && calza.getCarbone() == carbone) {
				calze[index] = calza.toString();
				index++;
			}
		}
		
		return calze;
	}
	
	public static void main(String[] args) {
		final int PORT_REGISTRY = 1099;
		String registryHost = "localhost";
		String serviceName = "BefanaFelice";
		String completeName = "//"+ registryHost + ":" + PORT_REGISTRY + "/" + serviceName;
		int num_calze = 5;
		
		try {
			portaCalze = new Calza[num_calze];
			
			inserisciCalza(0, "0", Tipo.NORMALE, false, "Bologna", "A", "...");
			inserisciCalza(1, "1", Tipo.CELIACO, false, "Bologna", "B", "...");
			inserisciCalza(2, "2", Tipo.NORMALE, true,  "Parma", "C", "...");
			inserisciCalza(3, "3", Tipo.NORMALE, false, "Genova", "A", "...");
			inserisciCalza(4, "4", Tipo.NORMALE, false, "Milano", "A", "...");
		} catch (Exception e) {
			System.out.println("Errore durante inizializzazione calze: "+e.getLocalizedMessage());
			System.exit(0);
		}
		
		/* Test */
		for (String w : visualizzaCalze(Tipo.NORMALE, true)) {
			System.out.println(w);
		}
		
//		for (Calza w : portaCalze) {
//			System.out.println(w.toString());
//		}
		
		try {
			/*
			 * RMI registration
			 */
			RMI_Server serverRMI = new RMI_Server();
			Naming.rebind(completeName, serverRMI);
			System.out.println("");
			System.out.println("CompleteName: " + completeName);
		} catch (RemoteException | MalformedURLException ex){
            if (ex instanceof  RemoteException){
                System.out.println("Remote Exception occurred. Exit");
                ex.printStackTrace();
                System.exit(2);
            }
            else if(ex instanceof  MalformedURLException){
                System.out.println("MalformedURLException occurred. Exit");
                System.exit(2);
            }
        } catch(Exception e) {
			System.out.print(e.getMessage());
			System.exit(1);
		}
		
	}
}
