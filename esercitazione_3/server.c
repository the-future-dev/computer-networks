#include <arpa/inet.h>
#include <netdb.h>
#include <netinet/in.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/errno.h>
#include <sys/socket.h>
#include <unistd.h>

static const int ON = 1;
static int DIM_CODA_ASCOLTO = 5;

void handler(int signo) 
{
    int stato;
    printf("Esecuzione gestore di SIGCHLD\n");
    wait(&stato);
}

int main(int argc, char* argv[])
{
    // Per evitare figli zombie
    signal(SIGCHLD, handler);

    struct sockaddr_in client_address, server_address;
    struct hostent *host;
    static int PORT;
    // Controllo che ci sia almeno un argomento
    if (argc != 2) {
        printf("Syntax: ./server <port>\n");
        exit(1);
    }
    // Controllo che la porta passata sia un numero
    for (int num = 0; argv[1][num] != '\0'; num++) {
        if ((argv[1][num] < '0') || (argv[1][num] > '9')) {
            printf("<port> deve essere un numero intero\n");
            exit(2);
        }
    }
    PORT = atoi(argv[1]);
    // Controllo che il valore della porta sia ammesso
    if (PORT < 1024 || PORT > 65535) {
        printf("Error: %s port\n", argv[0]);
        printf("1024 <= port <= 65535\n");
        exit(2);
    }

    // Creo l'indirizzo server
    memset((char *)&server_address, 0, sizeof(server_address));
    server_address.sin_family      = AF_INET;
    server_address.sin_addr.s_addr = INADDR_ANY;
    server_address.sin_port        = htons(PORT);

    // Creo la socket di ascolto
    int listen_sd = socket(AF_INET, SOCK_STREAM, 0);
    if (listen_sd < 0) {
        perror("Errore creazione della socket di ascolto");
        exit(1);
    }
    printf("Creata socket di ascolto con sd=%d\n", 
        listen_sd);
    if (setsockopt(listen_sd, SOL_SOCKET, SO_REUSEADDR, &ON, sizeof(ON)) < 0) {
        perror("Errore configurazione socket d'ascolto");
        exit(1);
    }
    printf("Configurata socket di ascolto\n");
    if (bind(listen_sd, (struct sockaddr_in *)&server_address, 
        sizeof(server_address)) < 0) {
        perror("Errore bind socket d'ascolto");
        exit(1);
    }
    printf("Bind eseguito per socket di ascolto");

    if (listen(listen_sd, DIM_CODA_ASCOLTO) < 0) {
        perror("Listen\n");
        exit(1);
    }
    printf("Avviata listen su socket di ascolto\n");

    int connection_sd;
    unsigned int client_address_length; 
    int fd;
    char read_character;
    // Ciclo di ricezione richieste
    while (1) {
        client_address_length = sizeof(client_address);
        if ((connection_sd = accept(listen_sd,
                 (struct sockaddr_in *) &client_address,
                  &client_address_length)) < 0) {
            // Se la accept viene interrotta da un segnale, salto una 
            // iterazione
            if (errno == EINTR) {
                perror("Errore durante la acceot: forzo la continuazione della accept");
                continue;
            } 
            else {
                exit(1);
            }      
        }

        // Figlio
        if (fork() == 0) {
            // Chiudo il socket descriptor che non utilizzo
            close(listen_sd);

            // Indirizzo del client
            host = gethostbyaddr((char *)&client_address.sin_addr,
                                 sizeof(client_address.sin_addr),
                                 AF_INET);
            // Se non trovo il client
            if (host == NULL) {
                printf("Non riesco a trovare il client\n");
                continue;
            } 
            else {
                printf("Server (figlio): host client e' %s \n", host->h_name);
            } 

            // Redirezione I/O sul socket descriptor della connessione
            close(1);
            // close(0);
            dup(connection_sd);
            dup(connection_sd);
            close(connection_sd);
            
            char *risultato;
            gets(risultato);
            printf("Mi hanno mandato: %s\n", risultato);

            // TODO Il server esegue l'operazione richiesta
            // while (read_character = read(fd, &read_character, sizeof(char)) < 0) {

            // }
        }

        close(connection_sd);
    }
}
