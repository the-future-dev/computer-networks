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
#include <unistd.h>

/* Struttura di una richiesta */
typedef struct {
    char nome_file[255];
} Request;

int main(int argc, char ** argv) {
    int sd, port, len, response, index;
    const int          on = 1;
    struct sockaddr_in clientaddr, serveraddr;
    struct hostent *clienthost;
    Request *req = (Request *)malloc(sizeof(Request));
    FILE *file;

    /* INPUT VALIDATION AND TYPE CHEKING*/
    if (argc != 2){
        printf("Error: %s port\n", argv[0]);
        exit(1);
    }
    index = 0;
    while(argv[1][index] != '\0'){
        if(argv[1][index] < '0' || argv[1][index] > '9'){
            printf("Error: %s port not in the correct format.\n", argv[0]);
            exit(2);
        }
        index++;
    }
    port = atoi(argv[1]);
    if (port < 1024 || port > 65535){
        printf("Error: %s port not in correct range", argv[0]);
        exit(2);
    }

    /* INIZIALIZZAZIONE INDIRIZZO SERVER */
    memset((char *)&serveraddr, 0, sizeof(serveraddr));
    serveraddr.sin_family = AF_INET;
    serveraddr.sin_addr.s_addr = INADDR_ANY;
    serveraddr.sin_port = htons(port);

    /* SOCKET: CREAZIONE, SETTAGGIO OPZIONI e CONNESSIONE */
    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0){
        perror("Error durante la creazione della socket");
        exit(1);
    }
    printf("Server: creata la socket. SD=%d\n", sd);

    if (setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0){
        perror("Error durante il settagggio opzioni della socket");
        exit(1);
    }
    printf("\t opzioni socket: ok\n");

    if(bind(sd, (struct sockaddr_in *)&serveraddr, sizeof(serveraddr))<0){
        perror("Error during socket binding");
        exit(1);
    }
    printf("\t bind socket: ok\n");

    /* CICLO DI RICEZIONE RICHIESTE */
    for(;;){
        len = sizeof(struct sockaddr_in);
        if (recvfrom(sd, req, sizeof(Request), 0, (struct sockaddr_in *)&clientaddr, &len) < 0){
            perror("Error receiving a request");
            continue;
        }
        // req->nome_file[sizeof(req->nome_file) - 1] = '\0';
        printf("Server: received a request.\n");
        response = 0;
        printf("\tnome_file: %s\n", req->nome_file);
        
        char longer_word[256] = "";

        if ((file = fopen(req->nome_file, "r")) == NULL){
            response = -1;
        } else{
            char word[256];
            
            while(fscanf(file, "%255s", word) == 1){
                if (strlen(word) > response){
                    response = strlen(word);
                    strcpy(longer_word, word);
                }
            }
            fclose(file);
        }
        printf("\tlonger word: %s", longer_word);
        printf("\tresponse: %i\n", response);
        fflush(stdout);
        
        if (sendto(sd, &response, sizeof(response), 0, (struct sockaddr_in *)&clientaddr, len)<0) {
            perror("Error sending response");
            continue;
        }
    }
    free(req);
}
