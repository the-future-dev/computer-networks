import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI_InterfaceFile extends Remote {
	
	String[] visualizza_lista() throws RemoteException;
    
	int visualizza_numero(String citta, String via) throws RemoteException;

}
