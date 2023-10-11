#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/wait.h>

int charInString(const char *str, char ch)
{
    while (*str != '\0')
    {
        if (*str == ch)
        {
            return 1;
        }
        str++;
    }
    return 0;
}

void processFile(const char *filter, const char *file_in)
{
    char read_char;
    int nread, fd, fd_temp;
    char tempFileName[256];
    sprintf(tempFileName, "%s.temp", file_in);

    fd = open(file_in, O_RDONLY);
    if (fd < 0)
    {
        perror("Impossibile aprire il file.");
        exit(2);
    }

    fd_temp = open(tempFileName, O_WRONLY | O_CREAT | O_TRUNC, 00640);
    if (fd_temp < 0)
    {
        perror("Impossibile creare il file temporaneo.");
        close(fd);
        exit(2);
    }

    while ((nread = read(fd, &read_char, sizeof(char)))) /* un carattere alla volta fino ad EOF*/
    {
        if (nread > 0)
        {
            char output_char = charInString(filter, read_char) ? '*' : read_char;
            write(fd_temp, &output_char, sizeof(char));
        }
        else
        {
            fprintf(stderr, "(PID %d) impossibile leggere dal file %s", getpid(), file_in);
            perror("Errore!");
            close(fd);
            close(fd_temp);
            exit(3);
        }
    }

    close(fd);
    close(fd_temp);

    // Sostituire il file originale con il file temporaneo
    if (rename(tempFileName, file_in) != 0)
    {
        perror("Errore nella rinominazione del file temporaneo.");
        exit(4);
    }
}

int main(int argc, char *argv[])
{
    if (argc < 3)
    {
        perror("Error: numero di argomenti sbagliato");
        exit(1);
    }

    char *filter = argv[1];

    for (int i = 2; i < argc; ++i)
    {
        pid_t pid = fork();
        if (pid == 0)
        {
            // Sono nel processo figlio
            printf("Process[%d]: opened\n", i-1);
            processFile(filter, argv[i]);
            printf("Process[%d]: closed\n", i-1);
            exit(0);
        }
        else if (pid < 0)
        {
            perror("Errore di fork");
            exit(2);
        }
    }

    // Aspetta che tutti i processi figli terminino
    while (wait(NULL) > 0);

    return 0;
}