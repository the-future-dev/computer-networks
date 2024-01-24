/* Ritossa Andrea 0001020070 */

/* stream_client.c
 * Cliente invia nome dir e riceve
 * nomi file in dir secondo livello
 */

#include <fcntl.h>
#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#define LINE_LENGTH 256

int main(int argc, char *argv[]) {
    int                port, nread, sd, nwrite, fd;
    char               directoryName[LINE_LENGTH], buffChar;
    struct hostent    *host;
    struct sockaddr_in servaddr;

    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if (argc != 3) {
        printf("Usage: %s serverAddress serverPort\n", argv[0]);
        exit(1);
    }
    nread = 0;
    while (argv[2][nread] != '\0') {
        if ((argv[2][nread] < '0') || (argv[2][nread] > '9')) {
            printf("Second argument must be an integer!\n");
            printf("Usage: %s serverAddress serverPort\n", argv[0]);
            exit(2);
        }
        nread++;
    }
    port = atoi(argv[2]);
    if (port < 1024 || port > 65535) {
        printf("Port must be in the range [1024, 65535]\n");
        exit(2);
    }

    /* INIZIALIZZAZIONE INDIRIZZO SERVER -------------------------- */
    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    host                = gethostbyname(argv[1]);
    if (host == NULL) {
        printf("Error while resolving %s to IP address\n", argv[1]);
        exit(1);
    }
    servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
    servaddr.sin_port        = htons(port);

    /* CREAZIONE SOCKET ------------------------------------ */
    sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd < 0) {
        perror("Open socket: ");
        exit(3);
    }
    printf("Client: created socket sd=%d\n", sd);

    /* Operazione di BIND implicita nella connect */
    if (connect(sd, (struct sockaddr *)&servaddr, sizeof(struct sockaddr_in)) < 0) {
        perror("connect");
        exit(3);
    }
    printf("Client: connect ok\n");

    /* CORPO DEL CLIENT:
     ciclo di accettazione di richieste da utente ------- */
    printf("Inserire nome del direttorio, EOF per terminare: ");
    while (gets(directoryName)) {
        printf("Invio il nome del direttorio: %s\n", directoryName);
        // #1
        nwrite = write(sd, directoryName, strlen(directoryName));

        // #2 Lettura risposta dal server
        read(sd, &buffChar, sizeof(char));
        if (buffChar == 'S') {
            printf("Ricevo e stampo i file nel direttorio remoto:\n");
            
            int nameLength;
            // #3 name size
            while ((nread = read(sd, &nameLength, sizeof(int))) > 0) {
                printf("\n");
                if (nameLength == '\1')
                    break;
                if (nameLength > LINE_LENGTH) {
                    printf("ERROR: %d", nameLength);
                }
                // #4 name file
                char fileName [nameLength]; 
                if (read(sd, &fileName, sizeof(char) * nameLength) < 0){
                    printf("Error reading filename");
                }
                
                write(1, &fileName, sizeof(char) * nameLength);
                
                // sprintf(fileName, "out/%s", fileName);

                //create / open file
                int fd;

                if ((fd = open(fileName, O_WRONLY | O_CREAT, 0644)) < 0) {
                    perror("open file destinatario");
                    printf("Nome del file, EOF per terminare: ");
                    continue;
                }

                printf("Opened %s: writing;\n", fileName);
                
                // #5 r/w file content
                while (nread = (read(sd, &buffChar, sizeof(char))) > 0)
                {
                    if (buffChar == '\1'){
                        // #6 end file
                        buffChar = EOF;
                        write(fd, &buffChar, sizeof(char));
                        break;
                    }
                    else if (buffChar == '\0')
                        write(fd, &buffChar, sizeof(char));
                    else
                        write(fd, &buffChar, sizeof(char));
                }
                printf("End writing\n");
                close(fd);

                printf("\n");
            }

            // // #3 Read name
            // while ((nread = read(sd, &buffChar, sizeof(char))) > 0) {
            //     if (buffChar == '\1') {
            //         break;
            //     } else if (buffChar == '\0') {
            //         printf("\n");
            //     } else {
            //         write(1, &buffChar, sizeof(char));
            //     }
            // }
            
        } else {
            printf("directory non presente sul server\n");
        }
        printf("Nome del direttorio, EOF per terminare: ");
    }
    /* Chiusura socket in ricezione */
    close(sd);
    printf("\nClient: termino...\n");
    exit(0);
}