/* Server Select
 * 	Un solo figlio per tutti i file.
 */

#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <netdb.h>
#include <netinet/in.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#define DIM_BUFF         100
#define LENGTH_FILE_NAME 20
#define max(a, b)        ((a) > (b) ? (a) : (b))

typedef struct {
    char nome_file[LENGTH_FILE_NAME];
    char word[LENGTH_FILE_NAME];
} RequestDeletion;

/********************************************************/
int delete_from_file(char *name) {
    // DIR           *dir;
    // struct dirent *dd;
    // int            count = 0;
    // dir                  = opendir(name);
    // if (dir == NULL)
    //     return -1;
    // while ((dd = readdir(dir)) != NULL) {
    //     printf("Trovato il file %s\n", dd->d_name);
    //     count++;
    // }
    // printf("Numero totale di file %d\n", count);
    // closedir(dir);
    return 1;
}
/********************************************************/
void gestore(int signo) {
    int stato;
    printf("esecuzione gestore di SIGCHLD\n");
    wait(&stato);
}
/********************************************************/

int main(int argc, char **argv) {
    int       listenfd, connfd, udpfd, fd_file, nready, maxfdp1;
    const int on   = 1;
    char      zero = 0, buff[DIM_BUFF], nome_file[LENGTH_FILE_NAME];
    fd_set    rset;
    int       len, nread, nwrite, num, port;
    RequestDeletion * req_deletion = (RequestDeletion *) malloc(sizeof(RequestDeletion));
    struct sockaddr_in cliaddr, servaddr;

    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if (argc != 2) {
        printf("Error: %s port\n", argv[0]);
        exit(1);
    }
    nread = 0;
    while (argv[1][nread] != '\0') {
        if ((argv[1][nread] < '0') || (argv[1][nread] > '9')) {
            printf("Terzo argomento non intero\n");
            exit(2);
        }
        nread++;
    }
    port = atoi(argv[1]);
    if (port < 1024 || port > 65535) {
        printf("Porta scorretta...");
        exit(2);
    }

    /* INIZIALIZZAZIONE INDIRIZZO SERVER ----------------------------------------- */
    memset((char *)&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family      = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port        = htons(port);

    printf("Server avviato\n");

    /* CREAZIONE SOCKET TCP ------------------------------------------------------ */
    listenfd = socket(AF_INET, SOCK_STREAM, 0);
    if (listenfd < 0) {
        perror("apertura socket TCP ");
        exit(1);
    }
    printf("Creata la socket TCP d'ascolto, fd=%d\n", listenfd);

    if (setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("set opzioni socket TCP");
        exit(2);
    }
    printf("Set opzioni socket TCP ok\n");

    if (bind(listenfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("bind socket TCP");
        exit(3);
    }
    printf("Bind socket TCP ok\n");

    if (listen(listenfd, 5) < 0) {
        perror("listen");
        exit(4);
    }
    printf("Listen ok\n");

    /* CREAZIONE SOCKET UDP ------------------------------------------------ */
    udpfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (udpfd < 0) {
        perror("apertura socket UDP");
        exit(5);
    }
    printf("Creata la socket UDP, fd=%d\n", udpfd);

    if (setsockopt(udpfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("set opzioni socket UDP");
        exit(6);
    }
    printf("Set opzioni socket UDP ok\n");

    if (bind(udpfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("bind socket UDP");
        exit(7);
    }
    printf("Bind socket UDP ok\n");

    /* AGGANCIO GESTORE PER EVITARE FIGLI ZOMBIE -------------------------------- */
    signal(SIGCHLD, gestore);

    /* PULIZIA E SETTAGGIO MASCHERA DEI FILE DESCRIPTOR ------------------------- */
    FD_ZERO(&rset);
    maxfdp1 = max(listenfd, udpfd) + 1;

    /* CICLO DI RICEZIONE EVENTI DALLA SELECT ----------------------------------- */
    for (;;) {
        FD_SET(listenfd, &rset);
        FD_SET(udpfd, &rset);

        if ((nready = select(maxfdp1, &rset, NULL, NULL, NULL)) < 0) {
            if (errno == EINTR)
                continue;
            else {
                perror("select");
                exit(8);
            }
        }

        /* Visione del direttorio ------------------------------------- */
        if (FD_ISSET(listenfd, &rset)) {
            printf("\n");
            printf("Main: Ricevuta richiesta di get di un file\n");
            len = sizeof(struct sockaddr_in);
            if ((connfd = accept(listenfd, (struct sockaddr *)&cliaddr, &len)) < 0) {
                if (errno == EINTR)
                    continue;
                else {
                    perror("accept");
                    exit(9);
                }
            }

            if (fork() == 0) { /* processo figlio che serve la richiesta di operazione */
                close(listenfd);;
                struct dirent *dp;
                DIR *dir;
                char nome_dir[LENGTH_FILE_NAME];

                printf("Stream Socket: child process pid=%i\n", getpid());

                for(;;){
                    if (read(connfd, &nome_dir, sizeof(nome_dir)) <= 0) {
                        perror("read");
                        break;
                    }
                    if (strcmp(nome_dir, "CLOSE") == 0){
                        printf("Stream Socket: Closing connection");
                        break;
                    }

                    printf("Stream Socket: Richiesta la directory %s\n", nome_dir);
                    
                    char c;

                    if (!(dir = opendir(nome_dir))){
                        char * work_a = "404: not found";
                        write(connfd, work_a, sizeof(work_a));
                        printf("\tNot found\n");
                    } else {
                        while((dp = readdir(dir)) != NULL){
                            if (strcmp(dp->d_name, ".") != 0 && strcmp(dp->d_name, "..") != 0) {                                
                                printf("\t %s", dp->d_name);
                                write(connfd, &(dp->d_name), strlen(dp->d_name) + 1);
                            }
                        }
                        closedir(dir);
                        printf("\tend of file sending\n");
                    }
                    char * end_marker = "--END--";
                    write(connfd, end_marker, strlen(end_marker) + 1);
                    printf("\tprinted end of communication\n");
                }

                printf("Stream Socket: Figlio %i: chiudo connessione e termino\n", getpid());
                close(connfd);
                exit(0);
            } // figlio
            /* padre chiude la socket dell'operazione */
            close(connfd);
        } /* fine gestione richieste di file */

        /* Eliminazione ------------------------------------------ */

        if (FD_ISSET(udpfd, &rset)) {
            printf("\n");
            printf("Main: ricevuta richiesta di eliminazione parola in un file specifico\n");
            len = sizeof(struct sockaddr_in);
            if (recvfrom(udpfd, req_deletion, sizeof(RequestDeletion), 0, (struct sockaddr *)&cliaddr, &len) <
                0) {
                perror("recvfrom");
                continue;
            }
            printf("Datagram Socket: richiesta deletion della parola [%s] nel file %s\n", req_deletion->word, req_deletion->nome_file);
            num = delete_from_file(nome_file);
            printf("Datagram Socket:  Risultato del conteggio: %i\n", num);
            

            if (sendto(udpfd, &num, sizeof(num), 0, (struct sockaddr *)&cliaddr, len) < 0) {
                perror("sendto");
                continue;
            }

        } /* fine gestione richieste di conteggio */
    }     /* ciclo for della select */
}
