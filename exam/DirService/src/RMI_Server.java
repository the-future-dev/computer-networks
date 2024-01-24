/* Ritossa Andrea 0001020070 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMI_Server extends UnicastRemoteObject implements RMI_InterfaceFile{
	private static final long serialVersionUID = 1L;
	
	private RMI_Server() throws RemoteException{ super(); }
	
	public static void main(String[] args) {
		final int PORT_REGISTRY = 1099;
		String registryHost = "localhost";
		String serviceName = "DirService";
		String completeName = "//"+ registryHost + ":" + PORT_REGISTRY + "/" + serviceName;
		
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



	@Override
	public String[] lista_nomi_file_contenenti_parola_in_linea(String nomeDirettorio, String parola)
			throws RemoteException {
		File dir = new File (nomeDirettorio);
		BufferedReader fileReader;
		String line;
		
		if (!dir.exists())
			throw new RemoteException("Il direttorio indicato non esiste.");
		
		if (dir.isFile())
			throw new RemoteException("Il file indicato come direttorio non è nel formato corretto.");
		
		File [] files = dir.listFiles();
		System.out.println("Parola: ["+parola+"]");
		boolean [] contains = new boolean [files.length];
		int size = 0;
		
		for (int i = 0; i < files.length; i++) {
			try {
				fileReader = new BufferedReader(new FileReader(files[i]));
				while ((line = fileReader.readLine()) != null) {
					if (line.indexOf(parola) != -1) {
						contains[i] = true;
						size++;
					}
				}
				// System.out.println("File("+files[i].getName()+"):"+contains[i]);
//				fileReader.close();
			} catch (IOException e) {
				System.out.println("! INTERNAL SERVER ERROR:");
				e.printStackTrace();
				
				throw new RemoteException("Internal Server Error");
			}
		}
		
		String [] ret = new String[size];
		size = 0;
		for (int i = 0; i < files.length; i++) {
			if (contains[i]) {
				ret[size++] = files[i].getName();
			}
		}
		System.out.println("\t> found "+size+" times");
		System.out.println();
		return ret;
	}



	@Override
	public int conta_numero_linee(String nomeFile, String parola) throws RemoteException {
		File file = new File (nomeFile);
		BufferedReader br;
		String line;
		int size = 0;

		System.out.println("Conta numero linee [ "+nomeFile+" ," parola+" ]");
		if (file == null || !file.exists() || file.isDirectory()) {
			return -1;
		}
		
		try {
			br = new BufferedReader (new FileReader(file));
			while ((line = br.readLine()) != null) {
				if (line.indexOf(parola) != -1) {
					size++;
				}
			}
			br.close();
		} catch (IOException e) {
			System.out.println("IO error:");
			e.printStackTrace();
			return -2;
		}

		System.out.println("\tResult: "+size);
		System.out.println();
		return size;
	}
}
