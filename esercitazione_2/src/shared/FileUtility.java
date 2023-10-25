package shared;
/* FileUtility.java */

import java.io.*;

public class FileUtility {

	/**
	 * Nota: sorgente e destinazione devono essere correttamente aperti e chiusi
	 * da chi invoca questa funzione.
	 *  
	 */
	public static void trasferisci_a_byte_file_binario(DataInputStream src,
			DataOutputStream dest, long length) throws IOException {
	
		// ciclo di lettura da sorgente e scrittura su destinazione
	    int buffer;    
	    try {
	    	// esco dal ciclo all lettura di un valore negativo -> EOF
	    	// N.B.: la funzione consuma l'EOF
	    	while ((buffer=src.read()) >= 0) {
	    		dest.write(buffer);
	    	}
	    	dest.flush();
	    }
	    catch (EOFException e) {
	    	System.out.println("Problemi, i seguenti: ");
	    	e.printStackTrace();
	    }
	}

	public static void trasferisci_a_byte_file_binario_length(DataInputStream src, DataOutputStream dest, long length) 
		throws IOException {
		byte[] buffer = new byte[1000]; 
		int bytesRead;
		while (length > 0 && (bytesRead = src.read(buffer, 0, (int) Math.min(buffer.length, length))) != -1) {
			dest.write(buffer, 0, bytesRead);
			length -= bytesRead;
		}
	}

	public static void consumaByteStream(DataInputStream src, long length) throws IOException {
		byte[] buffer = new byte[1000];
		int bytesRead;
		while (length > 0 && (bytesRead = src.read(buffer, 0, (int) Math.min(buffer.length, length))) != -1) {
			length -= bytesRead;
		}
	}

}