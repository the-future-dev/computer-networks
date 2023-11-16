/* lw_client.c
 * Il cliente invia il nome di un file remoto al server e
 * riceve la lunghezza della parola più lunga del file.
 */

#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#define LINE_LENGTH 128

int main(int argc, char **argv) {
    struct hostent    *host;
    struct sockaddr_in clientaddr, servaddr;
    int                port, nread, sd, len = 0, wordCount;
    char               nomeFile[LINE_LENGTH];

    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if (argc != 3) {
        printf("Error:%s serverAddress serverPort\n", argv[0]);
        exit(1);
    }

    /* INIZIALIZZAZIONE INDIRIZZO CLIENT -------------------------- */
    memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));
    clientaddr.sin_family = AF_INET;
    clientaddr.sin_addr.s_addr == INADDR_ANY;
    clientaddr.sin_port = 0;

    /* INIZIALIZZAZIONE INDIRIZZO SERVER -------------------------- */
    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    host                = gethostbyname(argv[1]); /* indirizzo gia' in formato di rete */

    /*CONTROLLO PARAMETRI SERVER*/
    // controllo su porta
    nread = 0;
    while (argv[2][nread] != '\0') {
        if ((argv[2][nread] < '0') || (argv[2][nread] > '9')) {
            printf("Secondo argomento non intero\n");
            printf("Error:%s serverAddress serverPort\n", argv[0]);
            exit(2);
        }
        nread++;
    }
    port = atoi(argv[2]);
    if (port < 1024 || port > 65535) {
        printf("Port scorretta...");
        exit(2);
    }
    // controllo su host
    if (host == NULL) {
        printf("%s not found in /etc/hosts\n", argv[1]);
        exit(2);
    } else {
        servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
        servaddr.sin_port        = htons(port);
    }

    /* CREAZIONE SOCKET ---------------------------------- */
    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0) {
        perror("apertura socket");
        exit(1);
    }
    printf("Client: creata la socket sd=%d\n", sd);

    /* BIND SOCKET, a una porta a caso ------------------- */
    if (bind(sd, (struct sockaddr *)&clientaddr, sizeof(clientaddr)) < 0) {
        perror("bind socket ");
        exit(1);
    }
    printf("Client: bind socket ok, alla porta %i\n", clientaddr.sin_port);

    /* CORPO DEL CLIENT:
     ciclo di accettazione di richieste da utente ------- */
    printf("Dammi il nome di file, EOF per terminare: ");

    /* ATTENZIONE!!
     * Cosa accade se la riga e' piu' lunga di LINE_LENGTH-1?
     * Stesso dicasi per le altre gets...
     */
    while (gets(nomeFile)) {
        printf("Nome file: %s\n", nomeFile);
        len = sizeof(servaddr);
        /* richiesta operazione */
        if (sendto(sd, &nomeFile, strlen(nomeFile) + 1, 0, (struct sockaddr *)&servaddr,
                   /*sizeof(servaddr)*/ len) < 0)
        {
            perror("sendto");
            // se questo invio fallisce il client torna all'inzio del ciclo
            printf("Dammi il nome di file, EOF per terminare: ");
            continue;
        }

        /* ricezione del risultato */
        printf("Attesa del risultato...\n");

        // ATTENZIONE! L'ultimo parametro di recvfrom viene usato sia come parametro di input
        // che di output, len DEVE essere inizializzato alla lunghezza della struttura
        // sockaddr_in PRIMA di essere utilizzato
        if (recvfrom(sd, &wordCount, sizeof(wordCount), 0, (struct sockaddr *)&servaddr, &len) < 0)
        {
            perror("recvfrom");
            continue;
        }

        if (wordCount < 0) {
            printf("File non esistente lato server\n");
        }
        // se questa ricezione fallisce il client torna all'inzio del ciclo
        else
        {
            printf("Esito dell'operazione, numero caratteri %d\n", wordCount);
        }
        printf("Dammi il nome di file, EOF per terminare: ");
    } // while gets

    close(sd);
    printf("\nClient: termino...\n");
    exit(0);
}
