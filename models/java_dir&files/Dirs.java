/* Ritossa Andrea 0001020070 */

public class Dirs{

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
}