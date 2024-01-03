/**
 * Interfaccia remota di servizio
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemOp extends Remote {

    /*
     * Input: nome di un file remoto ed un intero.
     * Output: l numero delle righe che contengono un numero di parole maggiore dell’intero inviato.
    */
	int conta_righe(String nomeFile, int numParole) throws RemoteException;

    /*
     * Input: nome di un file remoto ed un intero.
     * Output: esito dell'eliminazione della riga.
    */
	int elimina_riga(String nomeFile, int numRiga) throws RemoteException; 
}