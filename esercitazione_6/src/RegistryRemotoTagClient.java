import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistryRemotoTagClient extends Remote {
	
	public Remote[] cercaTag(String tag) throws RemoteException;
	
	public Remote cerca(String nomeLogico) throws RemoteException;

	public Remote[] cercaTutti(String nomeLogico) throws RemoteException;

}
