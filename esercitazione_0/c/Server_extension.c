#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>

#define MAX_STRING_LENGTH 256

int main(int argc, char* argv[])
{
    // fare controllo argomenti
    if (argc < 2)
    {
        perror("Numero di argomenti sbagliato …");
        exit(EXIT_FAILURE);
    }

    // preparatre l'array di file descriptors
    int fds [argc - 1];
    for (int i = 0; i < argc - 1; i++)
    {
        fds[i] = open(argv[i+1], O_WRONLY | O_CREAT | O_TRUNC, 00640);
        if (fds[i] < 0)
        {
            perror("P0: Impossibile creare/aprire il file");
            for (int j = 0; j < i - 1; ++j){ close(fds[j]); }
            exit(EXIT_FAILURE);
        }
    }

    // core producer loop
    char riga[MAX_STRING_LENGTH], *content;
    printf("Insert in format [file number]: line to be written. Ex:\n");
    printf("\t1: Hello, World!\n\n");
    while(fgets(riga, MAX_STRING_LENGTH, stdin))
    {
        // Check for EOF to terminate
        if (feof(stdin)) {
            break;
        }

        // Parse the prefix to determine the file index
        int fileIndex;
        if (sscanf(riga, "%d:", &fileIndex) != 1 || fileIndex <= 0 || fileIndex >= argc) {
            fprintf(stderr, "Prefisso invalido in: %s", riga);
            continue;
        }

        content = riga;
        content+=2;
        if(*content == ' '){
            content+=1;
        }

        // Write the line to the corresponding file
        int written = write(fds[fileIndex - 1], content, strlen(content));
        if (written <= 0) {
            perror("Errore nella scrittura sul file");
        }
    }

    // Close all files and free resources
    for (int i = 0; i < argc - 1; ++i) {
        close(fds[i]);
    }

    return 0;
}