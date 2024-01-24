/* Ritossa Andrea 0001020070 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMI_Server extends UnicastRemoteObject implements RMI_InterfaceFile{
	private static final long serialVersionUID = 1L;
	private static final int MAX_FILES = 6;
	
	private RMI_Server() throws RemoteException{ super(); }
	
	
	public static void main(String[] args) {
		final int PORT_REGISTRY = 1099;
		String registryHost = "localhost";
		String serviceName = "FileService24";
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
	public String[] lista_filetesto(String nomeDir) throws RemoteException {
		File dir = new File(nomeDir);
		
		if (!dir.exists())
			throw new RemoteException("Dir ("+nomeDir+") does not exist on remote server.");
		
		if (!dir.isDirectory())
			throw new RemoteException("File ("+nomeDir+") is not a directory on remote server.");
		
		String [] fileNames = dir.list();
		
		int counter = 0;
		
		for (String name : fileNames) {
			if (name.endsWith(".txt")) {
				counter++;
			}
		}
		
		String[] txtFileNames = new String[counter > 6 ? 6 : counter];
		
		counter = 0;
		for (int i = 0; i < txtFileNames.length && counter < 6; i++) {
			if (fileNames[i].endsWith(".txt")) {
				txtFileNames[counter] = fileNames[i];
				counter++;
			}
		}
		return txtFileNames;
	}


	@Override
	public int elimina_occorrenze(String nomeFile) throws RemoteException {
		File file = new File(nomeFile);
		if (!file.exists() || file.isDirectory() || !file.getName().endsWith(".txt"))
			return -1;
		
		BufferedReader fileReader;
		BufferedWriter fileWriter;
		File tempFile = new File("temp.txt");
		
		int numero_eliminazioni = 0;
		
		try {
			fileReader = new BufferedReader(new FileReader(file));
			fileWriter = new BufferedWriter(new FileWriter(tempFile));
			
			int ch;
			while((ch = fileReader.read()) != -1) {
				if (ch < 'a' || ch > 'z') {
					fileWriter.write(ch);
				} else {
					numero_eliminazioni++;
				}
			}
			fileReader.close();
	        fileWriter.close();
		} catch (FileNotFoundException e) {
			throw new RemoteException("File not found");
		} catch (IOException e) {
			return -1;
		}
		
		if(!file.delete()) {
			System.out.println("Could not delete the original file");
			return -1;
		}
		
		if (!tempFile.renameTo(file)) {
			System.out.println("Could not rename the temp file");
			return -1;
		}
		
		return numero_eliminazioni;
	}
}
