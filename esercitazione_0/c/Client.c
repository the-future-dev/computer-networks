#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>

int charInString(char * str, char ch){
    while(*str != '\0'){
        if (*str == ch){
            return 1;
        }
        str++;
    }
    return 0;
}

int main(int argc, char* argv[])
{
    char *file_in, *filter, read_char; int nread, fd;

    // controllo argomenti
    if (argc != 3 && argc != 2)
    {
        perror("Error: numero di argomenti sbagliato");
        exit(1);
    }
    filter = argv[1];

    if ( argc == 3){
        file_in = argv[2];

        // ciclo del programma
        fd = open(file_in, O_RDONLY);
        if (fd<0)
        {
            perror("P0: Impossibile aprire il file.");
            exit(2);
        }
    } else {
        file_in = stdin; //NULL
    }

    while(nread = read(fd, &read_char, sizeof(char))) /* un carattere alla volta fino ad EOF*/
    {
        if (nread > 0)
            putchar( charInString(filter, read_char) ? '*' : read_char);
        else
        {
            printf("(PID %d) impossibile leggere dal file %s", getpid(), file_in);
            perror("Errore!"); close(fd); exit(3);
        }
    }

    close(fd);
}