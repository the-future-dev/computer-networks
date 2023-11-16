/*
 * Socket C Con connessione: row deletion.
 * Server
 * 
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

typedef struct {
    char nome_file[255];
    int numero_linea;
} Request;

void gestore(int signo) {
    int stato;
    while(waitpid(-1, &stato, WNOHANG) > 0) {
        // Gestione del processo figlio terminato
    }
}

int main(int argc, char ** argv) {
    int listen_sd, connection_sd;
    int port, len;
    const int on = 1;
    struct sockaddr_in client_address, server_address;

    /* CONTROLLO ARGOMENTI */
    if (argc != 2){
        printf("Error: %s port\n", argv[0]);
        exit(1);
    } else {
        port = 0; //index
        while(argv[1][port] != '\0'){
            if (argv[1][port] < '0' || argv[1][port] > '9'){
                printf("Secondo argomento non intero");
                exit(2);
            }
            port++;
        }
    }
    port = atoi(argv[1]);
    if (port < 1024 || port > 65535){
        printf("Error: %s port \n", argv[0]);
        exit(2);
    }

    /* INIZIALIZZAZIONE INDIRIZZO SERVER */
    memset((char *) & server_address, 0, sizeof(server_address));
    server_address.sin_family = AF_INET;
    server_address.sin_addr.s_addr = INADDR_ANY;
    server_address.sin_port = htons(port);

    /* CREAZIONE E SETTAGI SOCKET D'ASCOLTO */
    listen_sd = socket(AF_INET, SOCK_STREAM, 0);
    if (listen_sd < 0){
        perror("Creazione socket");
        exit(1);
    }
    printf("Server: creata la socket d'ascolto per le richieste. fd=%d\n", listen_sd);
    
    if(setsockopt(listen_sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("set opzioni socket d'ascolto");
        exit(1);
    }
    printf("Server: set opzioni socket d'ascolto\n");

    if(bind(listen_sd, (struct sockaddr * )&server_address, sizeof(server_address)) < 0) {
        perror("bind socket d'ascolto");
        exit(1);
    }
    printf("Server: bind socket d'ascolto ok\n");

    if (listen(listen_sd, 5) < 0) // creazione coda d'ascolto
    {
        perror("Listen");
        exit(1);
    }
    printf("Server: listen ok\n");

    signal(SIGCHLD, gestore);

    for (;;){
        len = sizeof(client_address);
        if ((connection_sd = accept(listen_sd, (struct sockaddr *)&client_address, &len)) < 0) {
            if (errno == EINTR){
                perror("Forzo la continuazione della accept");
                continue;
            } else
                exit(1);
        }

        if (fork() != 0) {
            /* Processo Padre*/
            close(connection_sd);
        } else {
            /* Processo Figlio*/
            close(listen_sd);
            Request req;
            char ch;
            int file_descriptor, current_line = 1;

            read(connection_sd, &req, sizeof(req));
            
            printf("\n\nRequest received: { nome_file: %s, numero_riga: %i}", req.nome_file, req.numero_linea);
            
            /* Apertura file */
            file_descriptor = open(req.nome_file, O_RDONLY);
            if (file_descriptor < 0)
            {
                perror("Impossibile aprire il file.");
                ch = 'X';
                write(connection_sd, &ch, sizeof(char));
                dup(connection_sd);
                close(connection_sd);
                exit(2);
            }

            while(read(file_descriptor, &ch, sizeof(char)) > 0){
                if ( ch == '\n') current_line++;
                if (current_line != req.numero_linea){
                    if (send(connection_sd, &ch, sizeof(char), 0) < 0){
                        perror("Error sending char to socket");
                        break;
                    }
                } else {
                    printf("%c", ch);
                }
            }
            printf("\n");

            dup(connection_sd);
            close(file_descriptor);
            close(connection_sd);
            exit(0);
            /* End Processo Figlio */
        }
    }

    close(listen_sd); //mai raggiunto perchè il server è in un ciclo infinito
    return 0;
}




