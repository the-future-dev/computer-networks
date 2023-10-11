package esercitazione_0;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Client {

    public static void main(String[] args) {
        // ARGUMENT VALIDATION
        if (args.length == 0) {
            System.err.println("Error: No filename provided.");
            return;
        }

        String filename = args[0];
        if (!filename.endsWith(".txt")) {
            System.err.println("Error: Invalid file extension. Please provide a .txt file.");
            return;
        }

        // FILE READING
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                System.out.println("Line " + lineNumber + ": " + line);
                lineNumber++;
            }
        } catch (IOException e) {
            System.err.println("Error: Unable to read from file. " + e.getMessage());
        }
    }
}
