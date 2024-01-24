import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI_InterfaceFile extends Remote {
	
	/**
	 * @param nomeDir = nome del direttorio
	 * @throws RemoteException = FileNotFound, IOException
	 * @return lista dei primi N <= 6 nomi di file di testo
	 **/
	String[] lista_filetesto(String nomeDir) throws RemoteException;
    
	/**
	 * @param nomeFile = nome del file
	 * @throws RemoteException
	 * @return: int esito operazione:
	 * 	 	>=0	:	numero di eliminazioni effettuate
	 * 		-1	: 	errori.. insuccesso
	 **/
	int visualizza_numero(String nomeFile) throws RemoteException;

}
