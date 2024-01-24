/* Client per richiedere l'invio di un file (get, versione 1) */

#include <fcntl.h>
#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#define DIM_BUFF         100
#define MAX_LEN          20

typedef struct {
    char nome_file[MAX_LEN];
    char word[MAX_LEN];
} Request;

int main(int argc, char *argv[]) {
    int                sd, nread, port;
    char               c, ok;
    struct hostent    *host;
    struct sockaddr_in servaddr;
    Request             req;

    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if (argc != 3) {
        printf("Error:%s serverAddress serverPort\n", argv[0]);
        exit(1);
    }
    printf("Client avviato\n");

    /* PREPARAZIONE INDIRIZZO SERVER ----------------------------- */
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

    /* CREAZIONE E CONNESSIONE SOCKET (BIND IMPLICITA) ----------------- */
    sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd < 0) {
        perror("apertura socket ");
        exit(3);
    }
    printf("Creata la socket sd=%d\n", sd);

    if (connect(sd, (struct sockaddr *)&servaddr, sizeof(struct sockaddr)) < 0) {
        perror("Errore in connect");
        exit(4);
    }
    printf("Connect ok\n");

    /* CORPO DEL CLIENT: */
    /* ciclo di accettazione di richieste di file ------- */
    printf("Nome del file da richiedere: ");

    while (gets(&(req.nome_file))) {

        printf("Parola da cercare: ");
        gets(&(req.word));

        if (write(sd, &req, (sizeof(Request))) < 0) {
            perror("write");
            break;
        }
        printf("Richiesta del file %s inviata... \n", req.nome_file);

        if (read(sd, &ok, 1) < 0) {
            perror("read");
            break;
        }
        
        printf("Status Opening file: %c", ok);
        

        /*
        */
        
        printf("\n\n");
        printf("Nome del file da richiedere: ");
    } // while
    printf("\nClient: termino...\n");
    shutdown(sd, 0);
    shutdown(sd, 1);
    close(sd);
    exit(0);
}
