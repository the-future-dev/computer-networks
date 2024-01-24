/* Ritossa Andrea 0001020070 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI_InterfaceFile extends Remote {
	
	/**
	 * @param nomeDirettorio = nome del direttorio remoto
	 * @param parola = parola cercata
	 * @throws RemoteException = FileNotFound, IOException
	 **/
	String[] lista_nomi_file_contenenti_parola_in_linea(String nomeDirettorio, String parola) throws RemoteException;
    
	/**
	 * @param nomeFile = nome del file remoto
	 * @param parola = parola cercata
	 * @throws RemoteException
	 * @return: int esito operazione
	 **/
	int conta_numero_linee(String nomeFile, String parola) throws RemoteException;

}
