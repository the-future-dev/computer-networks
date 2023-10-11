package esercitazione_0;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Server {

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

        System.out.println("Filename validated: " + filename);

        Scanner scanner = new Scanner(System.in);

        // Get # of lines of the file
        System.out.print("How many lines do you want to write? ");
        int numLines = scanner.nextInt();
        scanner.nextLine(); // Consume newline left-over

        // Get the content of each line
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < numLines; i++) {
            System.out.print("Enter content for line " + (i + 1) + ": ");
            String lineContent = scanner.nextLine();
            content.append(lineContent).append(System.lineSeparator());
        }
        
        scanner.close();
        System.out.println("Writing to file: " + filename);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("File writing completed.");
    }
}