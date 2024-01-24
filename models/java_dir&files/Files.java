/* Ritossa Andrea 0001020070 */

public class Files{
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

}