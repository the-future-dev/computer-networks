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
    int sd, port, index, ris, len;
    char c;
    struct hostent  *host;
    struct sockaddr_in server_address, client_address;
    Request req;

    /* ARGUMENT CHEKCK */
    if (argc != 3){
        printf("Error: %s serverAddress serverPort\n", argv[0]);
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

    /* INIZIALIZZAZIONE INDIRIZZO SERVER */
    memset((char *) &server_address, 0, sizeof(struct sockaddr_in));
    server_address.sin_family = AF_INET;
    server_address.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
    server_address.sin_port        = htons(port);

    /* CREAZIONE SOCKET ---------------------------------- */
    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0) {
        perror("apertura socket");
        exit(1);
    }
    printf("Client: creata la socket sd=%d\n", sd);

    /* BIND SOCKET, a una porta scelta dal sistema --------------- */
    if (bind(sd, (struct sockaddr_in *)&client_address, sizeof(client_address)) < 0) {
        perror("bind socket ");
        exit(1);
    }
    printf("Client: bind socket ok, alla porta %i\n", client_address.sin_port);
    

    /* CORPO CLIENT
     * read: nome del file e il numero della linea.
     */
    printf("Nome file, EOF per terminare: ");
    while(scanf("%255s", req.nome_file)==1){
        printf("Numero linea: ");
        if (scanf("%i", &(req.numero_linea)) != 1){
            do {
                c = getchar();
                printf("%c ", c);
            } while (c != '\n');
            printf("Nome file, EOF per terminare: ");
            continue;
        }
        
        printf("FILE: %s\n", req.nome_file);
        printf("LINE: %i\n", req.numero_linea);

        /* richiesta operazione */
        len = sizeof(server_address);
        if (sendto(sd, &req, sizeof(Request), 0, (struct sockaddr_in *)&server_address, len) < 0) {
            perror("sendto");
            continue;
        }

        if (recvfrom(sd, &ris, sizeof(ris), 0, (struct sockaddr_in *)&server_address, &len) < 0) {
            perror("recvfrom");
            continue;
        }

        printf("\t Esito operazione: %i\n", ris);

        printf("\nNome file, EOF per terminare: ");
    }
}