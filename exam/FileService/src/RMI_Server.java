import java.io.BufferedReader;
import java.io.File;
//import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMI_Server extends UnicastRemoteObject implements RMI_InterfaceFile{
	
	private static final long serialVersionUID = 1L;

	private String local_path = System.getProperty("user.dir");
	
	/*
	 private FileFilter txtFiles = new FileFilter() {
	 

		@Override
		public boolean accept(File pathname) {
			if (pathname.getName().endsWith(".txt"))
				return true;
			return false;
		}
		
	};
	*/
	
	private RMI_Server() throws RemoteException {
		super();
	}
	
	@Override
	public String[] lista_nomi_file_contenenti_parola_in_linea(String nomeDirettorio, String parolaCercata) throws RemoteException {
		System.out.println("Requested dir: ");
		System.out.println(">"+local_path+"/"+nomeDirettorio);
		
		File dir = new File(local_path+"/"+nomeDirettorio);
		
		/*
		 * DIR:
		 * The list() methods return an array of Strings.
		 * The listFiles() methods return an array of File objects.
		 */
		
		if (!dir.exists() || !dir.isDirectory()) {
			System.out.println(">   DIR exists [" + (dir.exists() ? "true" : "false") + "] ");
			System.out.println(">   DIR isDirectory ["+(dir.isDirectory() ? "true" : "false") + "]");
			throw new RemoteException("directory does not exist");
//			return null;
		}
		
		File[] files = dir.listFiles();
		boolean [] containsWord = new boolean[files.length]; // automatically initialized as false.
		int size = 0;
		
		if (files.length == 0) {
			System.out.println(">   DIR isEmpty");
			return null;
		}
		
		BufferedReader in;
		FileReader isR;
		File f;
		String line;
		
		for (int i = 0; i < files.length; i++) {
			f = files[i];
			// System.out.println("Checking file ["+f.getName()+"]");
			
			try {
				isR = new FileReader(f);
				in = new BufferedReader(isR);
				
				while(!containsWord[i] && (line = in.readLine()) != null) {
					if (line.indexOf(parolaCercata) != -1) {
						containsWord[i] = true;
						size++;
					}
				}
				
				in.close();
				isR.close();
			} catch (IOException ex) {
				throw new RemoteException(ex.getLocalizedMessage());
			}
		}
		
		String [] output = new String[size];
		size = 0;
		for (int i = 0; i < files.length; i++) {
			if (containsWord[i]) {
				output[size++] = files[i].getName();
			}
		}
		System.out.println("---");
		return output;
	}

	@Override
	public int conta_numero_linee(String nomeFile, String parolaCercata) throws RemoteException {
		File file = new File(nomeFile);
		BufferedReader fileReader;
		int numero_linee = 0;
		
		if (!file.exists())
			throw new RemoteException("File does not exist");
		
		if (file.isDirectory())
			throw new RemoteException("File is a directory");
		
		if (!file.getName().endsWith(".txt"))
			return -1;
		
		try {
			fileReader = new BufferedReader( new FileReader(file) );
			String line;
			
			for (int i = 0; (line = fileReader.readLine())!= null; i++) {
				if (line.indexOf(parolaCercata) != -1) {
					numero_linee++;
					System.out.println("Occurrency in line: "+(i+1));
				}
			}
			
			fileReader.close();
		} catch (FileNotFoundException e) {
			throw new RemoteException("File not found");
		} catch (IOException e) {
			throw new RemoteException(e.getLocalizedMessage());
		}

		return numero_linee;
	}

	public static void main(String[] args) {
		final int PORT_REGISTRY = 1099;
		String registryHost = "localhost";
		String serviceName = "FileService";
		String completeName = "//"+ registryHost + ":" + PORT_REGISTRY + "/" + serviceName;		
		/*
		 * Initialization
		 */
		
		
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
	
}
