package esercitazione_0;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class Server {

    public static void main(String[] args) {
    	BufferedReader reader = null;
    	BufferedWriter writer = null;
    	String inputline, filename;

        // ARGUMENT VALIDATION
    	if (args.length == 0) {
            System.err.println("Error: No filename provided.");
            System.exit(0);
        }

        filename = args[0];
        if (!filename.endsWith(".txt")) {
            System.err.println("Error: Invalid file extension. Please provide a .txt file.");
            System.exit(1);
        }

        System.out.println("Filename validated: " + filename);

        reader = new BufferedReader(new InputStreamReader(System.in));

        try {
        	writer = new BufferedWriter(new FileWriter(filename));

        	System.out.println("Start entering lines (CTRL+D or CTRL+Z to finish):");
        	while((inputline = reader.readLine()) != null) {
        		inputline += '\n';
            	writer.write(inputline.toString());	
        	}
        	writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("File writing completed.");
    }
}