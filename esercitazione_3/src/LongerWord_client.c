/*
 * Socket C Senza connessione: Parola più lunga.
 * Client
 * 
*/

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
    socklen_t serverlen;

    if (argc != 3){
        printf("Usage: %s serverAddress serverPort\n", argv[0]);
        exit(1);
    }

    /* Input Format Control */
    index = 0;
    while (argv[2][index] != '\0') {
        if ((argv[2][index] < '0')||(argv[2][index] > '9')){
            printf("Port argument is not a number");
            printf("Usage: %s serverAddress serverPort\n", argv[0]);
            exit(2);
        }
        index++;
    }

    /* Initialize client and server address */
    memset(&clientaddr, 0, sizeof(struct sockaddr_in));
    clientaddr.sin_family = AF_INET;
    clientaddr.sin_addr.s_addr = INADDR_ANY;
    clientaddr.sin_port = 0;

    memset(&serveraddr, 0, sizeof(serveraddr));
    serveraddr.sin_family = AF_INET;
    host = gethostbyname(argv[1]);
    port = atoi(argv[2]);

    /* Input Validation */
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

    /* Socket Creation */
    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0){
        perror("Apertura socket");
        exit(1);
    }
    printf("Client: creata la socket. SD=%d\n", sd);

    /* Bind Socket */
    if (bind(sd, (struct sockaddr *)&clientaddr, sizeof(clientaddr)) < 0){
        perror("Error during socket binding");
        exit(1);
    }   
    printf("Client: socket bound to port %d\n", clientaddr.sin_port);

    /* Client body: loop for accepting requests from user */
    Request req;

    printf("\nInserisci il nome di un file: ");
    while(gets(req.nome_file)){
        /* sending request */
        serverlen = sizeof(serveraddr);
        if (sendto(sd, &req, sizeof(Request), 0, (struct sockaddr *)&serveraddr, serverlen) < 0){
            perror("Error sending the request");
            continue;
        }
        printf("\nRequest sent for file {%s}.", req.nome_file);

        /* receiving response */
        if (recvfrom(sd, &response, sizeof(response), 0, (struct sockaddr *) &serveraddr, &serverlen) < 0){
            perror("Error receiving the response");
            continue;
        }
        printf("\nThe longest word in %s is %i characters long", req.nome_file, response);

        // New request:
        printf("\n\nInserisci il nome di un file, EOF per terminare: ");
    }

    /* Cleaning up */
    close(sd);
    printf("\n Client: terminazione.\n");
    exit(0);
}