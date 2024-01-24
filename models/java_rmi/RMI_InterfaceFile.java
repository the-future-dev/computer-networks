/* Ritossa Andrea 0001020070 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI_InterfaceFile extends Remote {
	
	/**
	 * @param nomeFile = nome del file remoto
	 * @param wordsNum = soglia di parole
	 * @throws RemoteException = FileNotFound, IOException
	 **/
	String[] visualizza_lista() throws RemoteException;
    
	/**
	 * @param fileName = nome del file remoto
	 * @param rowNum = numero della riga da eliminare
	 * @throws RemoteException = = FileNotFound, IOException, numero riga troppo grande
	 * @return: int esito operazione
	 **/
	int visualizza_numero(String citta, String via) throws RemoteException;

}
