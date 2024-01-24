/* Ritossa Andrea 0001020070 */

#include <fcntl.h>
#include <unistd.h>


#define DIM_BUFF         100
#define LENGTH_FILE_NAME 90
#define LENGTH_PAROLA    30

/********************************************************/
/* Legge una alla volta le linee del file collocandole nel buffer
 * passato come argomento. Il valore di ritorno è quello della read,
 * che può essere controllato per EOF.
 */
int leggiLinea(int fd, char *linea) {
    char c;
    int  letti, i = 0;
    while ((letti = read(fd, &c, 1) != 0) && (c != '\n')) {
        linea[i] = c;
        i++;
    }
    linea[i] = '\0';
    return letti;
}
/********************************************************/

int main(int argc, char **argv) {
    int     fd_file, length, n_write;
    char    nome_file[LENGTH_FILE_NAME], parola[LENGTH_PAROLA];
    char    linea[DIM_BUFF];

    printf("Nome file: ");
    gets(nome_file);

    printf("Parola da cercare: ");
    gets(parola);

    fd_file = open(nome_file, O_RDONLY);
    if (fd_file < 0)
    {
        printf("File inesistente\n");
        exit(-1);
    }
    while (leggiLinea(fd_file, linea) > 0)
    {
        if (strstr(linea, parola))
        {
            length = strlen(linea) + 1;
            /*
             * send the length of the string
            */
            if((n_write = write(STDOUT_FILENO, &length, sizeof(int))) < 0)
            {
                perror("write");
                break;
            }
            
            printf("\n");

            if ((n_write = write(STDOUT_FILENO, linea, strlen(linea) + 1)) < 0)
            {
                perror("write");
                break;
            }

            /*
             * file terminato: comunicazione
            */

            length = -1;
            if (n_write = write(STDOUT_FILENO, linea, sizeof(int)) < 0){
                perror("write");
                break;
            }            
        }
    }
    


}
