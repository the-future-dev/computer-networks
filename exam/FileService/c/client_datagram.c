/* Client per richiedere il numero di file in un direttorio remoto */

#include <dirent.h>
#include <fcntl.h>
#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#define MAX_LEN 20

typedef struct {
    char nome_file[MAX_LEN];
    char word[MAX_LEN];
} Request;

int main(int argc, char **argv) {
    struct hostent    *host;
    struct sockaddr_in clientaddr, servaddr;
    int                sd, nread, port;

    int  len, num;
    Request     req;

    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if (argc != 3) {
        printf("Error:%s serverAddress serverPort\n", argv[0]);
        exit(1);
    }

    /* INIZIALIZZAZIONE INDIRIZZO SERVER--------------------- */
    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    host                = gethostbyname(argv[1]);
    if (host == NULL) {
        printf("%s not found in /etc/hosts\n", argv[1]);
        exit(2);
    }

    nread = 0;
    while (argv[2][nread] != '\0') {
        if ((argv[2][nread] < '0') || (argv[2][nread] > '9')) {
            printf("Secondo argomento non intero\n");
            exit(2);
        }
        nread++;
    }
    port = atoi(argv[2]);
    if (port < 1024 || port > 65535) {
        printf("Porta scorretta...");
        exit(2);
    }

    servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
    servaddr.sin_port        = htons(port);

    /* INIZIALIZZAZIONE INDIRIZZO CLIENT--------------------- */
    memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));
    clientaddr.sin_family      = AF_INET;
    clientaddr.sin_addr.s_addr = INADDR_ANY;
    clientaddr.sin_port        = 0;

    printf("Client avviato\n");

    /* CREAZIONE SOCKET ---------------------------- */
    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0) {
        perror("apertura socket");
        exit(3);
    }
    printf("Creata la socket sd=%d\n", sd);

    /* BIND SOCKET, a una porta scelta dal sistema --------------- */
    if (bind(sd, (struct sockaddr *)&clientaddr, sizeof(clientaddr)) < 0) {
        perror("bind socket ");
        exit(1);
    }
    printf("Client: bind socket ok, alla porta %i\n", clientaddr.sin_port);

    /* CORPO DEL CLIENT: */
    printf("La parola da cercare: ");

    while (gets(&(req.word))) {
        printf("Il nome del file: ");
        gets(&(req.nome_file));

        /* invio richiesta */
        len = sizeof(servaddr);
        if (sendto(sd, &req, sizeof(Request), 0, (struct sockaddr *)&servaddr, len) < 0)
        {
            perror("scrittura socket");
            printf("Nome del direttorio: ");
            continue; // se questo invio fallisce il client torna all'inzio del ciclo
        }

        /* ricezione del risultato */
        printf("Attesa del risultato...\n");
        if (recvfrom(sd, &num, sizeof(num), 0, (struct sockaddr *)&servaddr, &len) < 0) {
            perror("recvfrom");
            printf("Nome del direttorio: ");
            continue; // se questa ricezione fallisce il client torna all'inzio del ciclo
        }

        if (num < 0)
            printf(">[%d]] Il file %s non esiste\n", num, req.nome_file);
        else
            printf("Nel file %s ci sono |%d| occorrenze di %s.\n", req.nome_file, num, req.word);

        printf("\n\nLa parola da cercare: ");
    } // while

    printf("\nClient: termino...\n");
    shutdown(sd, 0);
    shutdown(sd, 1);
    close(sd);
    exit(0);
}
