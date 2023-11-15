#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

/* Struttura di una richiesta */
typedef struct {
    char nome_file[255];
} Request;

int main(int argc, char **argv){
    struct sockaddr_in clientaddr, serveraddr;
    struct hostent *host;
    int port, index, sd, len, response;

    if (argc != 3){
        printf("Error: %s serverAddress serverPort\n", argv[0]);
        exit(1);
    }

    /* INPUT FORMAT CONTROL */
    index = 0;
    while (argv[2][index] != '\0') {
        if ((argv[2][index] < '0')||(argv[2][index] > '9')){
            printf("Port argument is not a number");
            printf("Error: %s serverAddress serverPort\n", argv[0]);
            exit(2);
        }
        index++;
    }

    /* INIZIALIZZAZIONE INDIRIZZO CLIENT AND SERVER */
    // passando 0 ci leghiamo ad un qualsiasi indirizzo libero. può non funzionare. Altre opzioni?
    memset((char * ) & clientaddr, 0, sizeof(struct sockaddr_in));
    clientaddr.sin_family = AF_INET;
    clientaddr.sin_addr.s_addr = INADDR_ANY;
    clientaddr.sin_port = 0;

    memset((char * ) & serveraddr, 0, sizeof(serveraddr));
    serveraddr.sin_family = AF_INET;
    host = gethostbyname(argv[1]);
    port = atoi(argv[2]);

    /* INPUT VALIDATION */
    if (port < 1024 || port > 65535) {
        printf("Error: %s non è una porta accettabile\n.", argv[2]);
        exit(2);
    }
    if (host == NULL) {
        printf("Error: %s not found is /etc/hosts\n", argv[1]);
        exit(2);
    }
    serveraddr.sin_addr.s_addr = ((struct in_addr *)(host -> h_addr))-> s_addr;
    serveraddr.sin_port = htons(port);

    /* CREAZIONE SOCKET */
    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0){
        perror("Apertura socket");
        exit(1);
    }
    printf("Client: creata la socket. SD=%d\n", sd);

    /* BIND SOCKET, a una porta scelta dal sistema. */
    if (bind(sd, (struct sockaddr_in *) &clientaddr, sizeof(clientaddr)) < 0){
        perror("Bind error");
        exit(1);
    }   
    printf("Client: socket binded to port %d\n", clientaddr.sin_port);

    /* CORPO DEL CLIENT: ciclo di accettazione di richieste da utente. */
    Request req;
    printf("\nInserisci il nome di un file: ");
    while(scanf("%255s", req.nome_file)==1){
        /* richiesta operazione */
        len = sizeof(serveraddr);
        if (sendto(sd, &req, sizeof(Request), 0, (struct sockaddr_in *)&serveraddr, len) < 0){
            perror("Error sending the request");
            continue;
        }
        printf("\nRequest sent for file {%s}.", req.nome_file);

        /* richiesta del risultato */
        if (recvfrom(sd, &response, sizeof(response), 0, (struct sockaddr_in *) &serveraddr, &len) < 0){
            perror("Error receiving the response");
            continue;
        }
        printf("\nLa lunghezza della parola più lunda dentro %s è %i", req.nome_file, response);

        // New request:
        printf("\n\nInserisci il nome di un file, EOF per terminare: ");
    }

    /* Cleaning */
    close(sd);
    printf("\n Client: terminazione.\n");
    exit(0);
}