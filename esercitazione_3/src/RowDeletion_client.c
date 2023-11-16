/*
 * Socket C Con connessione: row deletion.
 * Client
 * 
*/

#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    char nome_file[255];
    int numero_linea;
} Request;

int main(int argc, char *argv[]){
    int sd, port, index;
    struct hostent  *host;
    struct sockaddr_in server_address;
    Request req;

    if (argc != 3){
        printf("Usage: %s serverAddress serverPort\n", argv[0]);
        exit(1);
    }

    index = 0;
    while (argv[2][index] != '\0') {
        if ((argv[2][index] < '0') || (argv[2][index] > '9')) {
            printf("Secondo argomento non intero\n");
            exit(2);
        }
        index++;
    }
    port = atoi(argv[2]);
    host = gethostbyname(argv[1]);
    
    /* VERIFICA PORT e HOST */
    if (port < 1024 || port > 65535) {
        printf("%s = porta scorretta...\n", argv[2]);
        exit(2);
    }
    if (host == NULL) {
        printf("%s not found in /etc/hosts\n", argv[1]);
        exit(2);
    }

    /* indirizzo server */
    memset((char *) &server_address, 0, sizeof(struct sockaddr_in));
    server_address.sin_family = AF_INET;
    server_address.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
    server_address.sin_port        = htons(port);

    /* creazione socket */
    sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd < 0) {
        perror("apertura socket");
        exit(1);
    }
    printf("Client: creata la socket sd=%d\n", sd);

    /* connessione al server */
    if (connect(sd, (struct sockaddr *)&server_address, sizeof(server_address)) < 0) {
        perror("Errore nella connessione al server");
        exit(1);
    }

    /* corpo client*/
    printf("Nome file, EOF per terminare: ");
    while(scanf("%255s", req.nome_file)==1){
        printf("Numero linea: ");
        if (scanf("%i", &(req.numero_linea)) != 1){
            char c;
            do {
                c = getchar();
                printf("%c ", c);
            } while (c != '\n');
            printf("Nome file, EOF per terminare: ");
            continue;
        }

        printf("Request: { file: %s, line: %i }", req.nome_file, req.numero_linea);

        /* richiesta */
        if (write(sd, &req, sizeof(Request)) < 0) {
            perror("write");
            continue;
        }

        char response;
        /* risposta */
        while (read(sd, &response, sizeof(response)) > 0)
        {
            printf("%c", response);
        }

        printf("\nNome file, EOF per terminare: ");
    }
}