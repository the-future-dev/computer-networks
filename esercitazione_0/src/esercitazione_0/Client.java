package esercitazione_0;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class Client {
    public static void main(String[] args){
    	/*
    	 * Input:
    	 * 		filter_word
    	 * 		filename.txt
         * 
         * The funciton also support input redirection, so it can be called with:
         *      filter_word
         *      < filename.txt
         * 
         * Output:
         *      filtered content of filename.txt
    	 */
        Reader r = null;
        
        // fare controllo argomenti
        if (args.length != 1 && args.length != 2){
            System.out.println("Input errato: necessito di un filtro e di un file di input");
            System.exit(0);
        }

        try {
            r = (args.length == 2) ? new FileReader(args[1]) : new InputStreamReader(System.in);
        } catch(FileNotFoundException e) {
            System.out.println("File "+args[1]+" non trovato");
            System.exit(1);}
            try {
                int x;
                char ch;
                while ((x = r.read()) >=0){
                    ch = (args[0].contains(Character.toString(x))) ? '*': (char) x;
                    System.out.print(ch);
                }
                r.close();
            } catch(IOException ex) {
                System.out.println("Errore di input");
                System.exit(2);
            }
        }
}