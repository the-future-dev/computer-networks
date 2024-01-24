import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI_InterfaceFile extends Remote {
	
	/**
	 * @param fileDirettorio = nome del direttorio remoto
	 * @param parolaCercata = nome della parola di cui cercare le occorrenze
	 * @throws RemoteException = = FileNotFound, IOException
	 * @return: String[] nomi files contenenti la parola cercata
	 **/
	public String[] lista_nomi_file_contenenti_parola_in_linea(String nomeDirettorio, String parolaCercata) throws RemoteException;
    
	/**
	 * @param fileName = nome del file remoto
	 * @param parolaCercata = nome della parola di cui cercare le occorrenze
	 * @throws RemoteException = = FileNotFound, IOException, numero riga troppo grande
	 * @return: int esito operazione
	 **/
	public int conta_numero_linee(String nomeFile, String parolaCercata) throws RemoteException;
}
