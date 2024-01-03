import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerImpl extends UnicastRemoteObject implements RemOp {
    
    private static final long serialVersionUID = 1L;

	public ServerImpl() throws RemoteException {
        super();
    }

    // Avvio del Server RMI
	public static void main(String[] args) {
		final int REGISTRYPORT = 1099;
        String registryHost = "localhost";
		String serviceName = "ServerAR";

        // Registrazione del servizio RMI
        String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/" + serviceName;

        try {
			ServerImpl serverRMI = new ServerImpl();
			Naming.rebind(completeName, serverRMI);
			System.out.println("Server RMI: Servizio \"" + serviceName + "\" registrato");
		} catch (Exception e) {
			System.err.println("Server RMI \"" + serviceName + "\": " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
    }

    public int conta_righe(String nomeFile, int numParole) throws RemoteException {
        File fileToRead = null;
        int response = -1;
        String line = null;
        String[] lineSplitted;
        try {
            fileToRead = new File(nomeFile);
            if (fileToRead.exists()){
                response = 0;
                BufferedReader reader = new BufferedReader(new FileReader(fileToRead));
                while((line = reader.readLine()) != null){
                	lineSplitted = line.split(" ", numParole+1);
                	if (lineSplitted.length > numParole) {
                		response++;
                	};
                	System.out.println("Server: "+lineSplitted.length+" line=["+line+"]");
                }
                reader.close();
            } else {
                throw new RemoteException("file non esiste");
            }
        } catch (Exception e) {
            throw new RemoteException("nome file");
        }
        return response;
    }

    public int elimina_riga(String nomeFile, int numRiga) throws RemoteException {
    	File fileToRead = null;
    	File fileToWrite = null;
    	int response = -1;
    	int righeCopiate = 0;
    	String line;
    	boolean eliminata = false;

    	try {
    		fileToRead = new File(nomeFile);
    		fileToWrite = new File(nomeFile+".temp");

    		if (fileToRead.exists()){
                BufferedReader reader = new BufferedReader(new FileReader(fileToRead));
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileToWrite));
                
                while ((line = reader.readLine())!=null) {
                	righeCopiate++;
                	if (righeCopiate == numRiga) {
                		eliminata = true;
                	} else {
                		writer.write(line+"\n");
                	}
                }
                reader.close();
                writer.close();
                
                if (eliminata) {
                	response = 1;
                	fileToRead.delete();
//                	fileToWrite.renameTo(fileToRead);
                	fileToWrite.renameTo(new File(nomeFile));
                } else {
                	response = -1;
                	fileToWrite.delete();
                }
    		}
    		// fileToRead does not exist
    		 else {
                throw new RemoteException("file non esiste");
            }
    	} catch(IOException e) {
    		throw new RemoteException("nome file");
    	} catch (Exception e) {
    		throw new RemoteException(e.getLocalizedMessage());
    	}
    	// createTempFile(String prefix, String suffix, File directory)
    	// Creates a new empty file in the specified directory, using the given prefix and suffix strings to generate its name.
    	
    	return response;
    }
}