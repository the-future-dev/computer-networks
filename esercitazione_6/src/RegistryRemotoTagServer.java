
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistryRemotoTagServer extends RegistryRemotoTagClient {
	
	public boolean associaTag(String nomeLogico, String tag) throws RemoteException;
	
	public boolean aggiungi(String nomeLogico, Remote riferimento) throws RemoteException;

	public Object[][] restituisciTutti() throws RemoteException;

	public boolean eliminaPrimo(String nomeLogico) throws RemoteException;

	public boolean eliminaTutti(String nomeLogico) throws RemoteException;
}
