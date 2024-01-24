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

#define MAX_LEN          20
#define MAX_LINE         256
#define max(a, b)        ((a) > (b) ? (a) : (b))

typedef struct {
    char nome_file[MAX_LEN];
    char word[MAX_LEN];
} Request;

/********************************************************/
void gestore(int signo) {
    int stato;
    printf("esecuzione gestore di SIGCHLD\n");
    wait(&stato);
}
/********************************************************/

int main(int argc, char **argv) {
    int       listenfd, connfd, udpfd, nready, maxfdp1, fd;
    const int on   = 1;
    fd_set    rset;
    int       len, nread, count, port;
    struct sockaddr_in cliaddr, servaddr;
    Request     req;

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

        /* GESTIONE RICHIESTE DI GET DI UN FILE ------------------------------------- */
        if (FD_ISSET(listenfd, &rset)) {
            printf("Ricevuta richiesta di get di un file\n");
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
                close(listenfd);
                printf("Dentro il figlio, pid=%i\n", getpid());
                
                read(connfd, &req, sizeof(Request));

                count = 0;

                if ((fd = open(req.nome_file, O_RDONLY)) < 0) {
                    perror("open file sorgente");
                    count = -1;
                } else{
                    
                }

                write(connfd, &status, (sizeof(char)));

                if (status == 'W'){
                    /* File exists */
                }
                
                printf("Figlio %i: chiudo connessione e termino\n", getpid());
                close(connfd);
                exit(0);
            } // figlio
            /* padre chiude la socket dell'operazione */
            close(connfd);
        } /* fine gestione richieste di file */

        /* GESTIONE RICHIESTE DI CONTEGGIO ------------------------------------------ */
        if (FD_ISSET(udpfd, &rset)) {
            printf("Server: ricevuta richiesta di conteggio occorrenze in un file\n");
            len = sizeof(struct sockaddr_in);
            if (recvfrom(udpfd, &req, sizeof(Request), 0, (struct sockaddr *)&cliaddr, &len) <
                0) {
                perror("recvfrom");
                continue;
            }
            printf("Richiesto conteggio di occorrenze di %s in file %s\n", req.word, req.nome_file);

            if ((fd = open(req.nome_file, O_RDONLY)) < 0) {
                perror("open file sorgente");
                continue;
            }

            char line [MAX_LINE];

            count = 0;
            while (read(fd, line, sizeof(line)) > 0){
                if (strstr(line, req.word) != NULL){
                    count++; 
                }
            }
            printf("Risultato del conteggio: %i\n", count);

            if (sendto(udpfd, &count, sizeof(count), 0, (struct sockaddr *)&cliaddr, len) < 0) {
                perror("sendto");
                continue;
            }

        } /* fine gestione richieste di conteggio */
    }     /* ciclo for della select */
}
