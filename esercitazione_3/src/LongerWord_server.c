/*
 * Socket C Senza connessione: Parola più lunga.
 * Server
 * 
*/

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

/* Structure of a request */
typedef struct {
    char nome_file[255];
} Request;

int main(int argc, char ** argv) {
    int sd, port, response;
    const int on = 1;
    struct sockaddr_in clientaddr, serveraddr;
    socklen_t len;
    Request req;
    FILE *file;

    /* Input validation and type checking */
    if (argc != 2){
        printf("Usage: %s port\n", argv[0]);
        exit(1);
    }
    for (int index = 0; argv[1][index] != '\0'; index++){
        if (argv[1][index] < '0' || argv[1][index] > '9'){
            printf("Error: Port not in the correct format.\n");
            exit(2);
        }
    }
    port = atoi(argv[1]);
    if (port < 1024 || port > 65535){
        printf("Error: Port not in correct range.\n");
        exit(2);
    }

    /* Server address initialization */
    memset(&serveraddr, 0, sizeof(serveraddr));
    serveraddr.sin_family = AF_INET;
    serveraddr.sin_addr.s_addr = INADDR_ANY;
    serveraddr.sin_port = htons(port);

    /* Socket creation, option setting, and binding */
    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0){
        perror("Socket creation error");
        exit(1);
    }
    printf("Server: socket created. SD=%d\n", sd);

    if (setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0){
        perror("Setting socket options error");
        exit(1);
    }
    printf("\tSocket options set successfully\n");

    if (bind(sd, (struct sockaddr *)&serveraddr, sizeof(serveraddr)) < 0){
        perror("Socket binding error");
        exit(1);
    }
    printf("\tSocket binding successful\n");

    /* Request reception loop */
    for(;;){
        len = sizeof(clientaddr);
        if (recvfrom(sd, &req, sizeof(Request), 0, (struct sockaddr *)&clientaddr, &len) < 0){
            perror("Error receiving request");
            continue;
        }
        printf("Server: received request for file %s\n", req.nome_file);
        
        response = 0;
        char longer_word[256] = "";
        if ((file = fopen(req.nome_file, "r")) == NULL){
            response = -1;
        } else{
            char word[256];
            while(fscanf(file, "%255s", word) == 1){
                if (strlen(word) > response){
                    strcpy(longer_word, word);
                }
            }
            response = strlen(longer_word);
            fclose(file);
        }
        printf("\tLongest word: %s", longer_word);
        printf("\tResponse: %i\n", response);
        fflush(stdout);
        
        if (sendto(sd, &response, sizeof(response), 0, (struct sockaddr *)&clientaddr, len) < 0) {
            perror("Error sending response");
            continue;
        }
    }
}
