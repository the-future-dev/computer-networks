// #include "Calza.h"

#include <errno.h>
#include <netinet/in.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/wait.h>

void gestore(int sign){
    int stato;
    printf("Esecuzione gestore di SIGCHLD\n");
    wait(&stato);
}

// void stampa_tabella(Calza* arr_calze);
// int add_calza_to_portacalze(Calza * porta_calze, int index, char * id, char * tipo, char carbone, char* citta, char* via, char* messaggio);

int main (int argc, char ** argv){
    const int on = 1;

    struct sockaddr_in client_address, server_address;
    int tcp_fd, udp_fd, maxfdp1, connection_fd;
    int index, port;
    fd_set rset;
    // Calza porta_calze [MAX_CALZE];
    // Calza nuova_calza;

    /*
     * Input parsing
    */
    if(argc != 2) {
		printf("Errore argomenti. Usage: %s <serverPort> \n", argv[0]);
		exit(1);
	}
    index = 0;
    while (argv[1][index] != '\0') //Verifica che l'argomento sia intero
    {
        if (argv[1][index] < '0' || argv[1][index] > '9'){
            printf("Secondo argomento non intero");
            exit(1);
        }
        index++;
    }
    port = atoi(argv[1]);
    if (port < 1024 || port > 65535){
        printf("Error: port out of bound [1024, 65535] - %d", port);
        exit(1);
    }

    /*
     * Inizializzazione di porta calze
    */
    for (index = 0; index < MAX_CALZE; index++){
        strcpy(porta_calze[index].identificatore, "-1");
    }
    // add_calza_to_portacalze(porta_calze, 0, "0", "Normale", 'N', "Bologna", "Saragozza", "Ciaoo");
    // add_calza_to_portacalze(porta_calze, 1, "1", "Celiaco", 'S', "Roma", "Saragozza", "Hello");
    // add_calza_to_portacalze(porta_calze, 2, "2", "Normale", 'S', "Bologna", "Saragozza", "Welcome");
    // add_calza_to_portacalze(porta_calze, 3, "3", "Normale", 'N', "Bologna", "Mascarella", "Hei Hei");
    // add_calza_to_portacalze(porta_calze, 4, "4", "Normale", 'N', "Bologna", "Mascarella", "Bye Bye");

    stampa_tabella(porta_calze);

    /*
     * Inizializzazione indirizzo server e bind
    */
    memset((char *) &server_address, 0, sizeof(server_address));
    server_address.sin_family = AF_INET;
    server_address.sin_addr.s_addr = INADDR_ANY;
    server_address.sin_port = htons(port);
    printf("Server Avviato!\n");

    /*
     * Socket Stream (TCT): creazione e settaggio
    */
    tcp_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (tcp_fd < 0) { perror("Errore aprendo socket TCP");  exit(2); }
    else printf("TCP: Creata la socket TCP. fd = %d\n", tcp_fd);

    if(setsockopt(tcp_fd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("set opzioni socket TCP");
		exit(2);
	} else printf("\t Set opzioni socket TCP ok\n");

    if(bind(tcp_fd,(struct sockaddr *) &server_address, sizeof(server_address)) < 0) {
		perror("bind socket TCP");
		exit(2);
	} else  printf("\t Bind socket TCP ok\n");
    
    if(listen(tcp_fd, 5) < 0) {
		perror("listen");
		exit(2);
	} else printf("\t Listen ok\n");

    /*
     * Socket Datagram (UDP)
    */
    udp_fd = socket(AF_INET, SOCK_DGRAM, 0);
    if (udp_fd < 0) { perror("Errore durante apertura socket UPD"); exit(3); }
    else printf("UDP: Creata la socket UDP, fd = %d", udp_fd);
    
    if(setsockopt(udp_fd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
		perror("Errore set opzioni socket UDP");
		exit(3);
	} else printf("\t Set opzioni socket UDP ok\n");

    if(bind(udp_fd,(struct sockaddr *) &server_address, sizeof(server_address)) < 0) {
		perror("Errore bind socket UDP");
		exit(3);
	} else  printf("\t Bind socket UDP ok\n");


    // Aggancio gestore per evitare figli zombie
	signal(SIGCHLD, gestore);

     // Pulizia e settaggio Maschera dei file descriptor
	FD_ZERO(&rset);
	maxfdp1 = max(tcp_fd, udp_fd) + 1;

	printf("\n\n");

    for(;;)
    {
        FD_SET(tcp_fd, &rset);
        FD_SET(udp_fd, &rset);

        if ((select(maxfdp1, &rset, NULL, NULL, NULL) < 0)) {
            if (errno == EINTR)
                continue;
            else {
                perror("Errore durante SELECT");
                exit(4);
            }
        }

        /*
         * Socket Stream:
         * -> visualizzazione di tutte le calze di un certo tipo e carbone.
        */
        if (FD_ISSET(tcp_fd, &rset)){
            if ((connection_fd = accept(tcp_fd, (struct sockaddr *)&client_address, sizeof(struct sockaddr_in)))< 0){
                if (errno == EINTR) continue;
                else {
                    perror("Errore durante ACCEPT");
                    exit(4);
                }
            }

            //***
        }

        /*
         * Socket Datagram.
         * -> inserimento di una nuova calza.
        */ 
        if (FD_ISSET(udp_fd, &rset)){
            if (recvfrom(udp_fd, &nuova_calza, sizeof(Calza), 0, (struct sockaddr * ) &client_address, sizeof(struct sockaddr_in)) < 0){
                perror("Errore durante RECVFROM");
                continue;
            }

            //***
        }
    } // ciclo infinito
    
    exit(0);
}

// Implementazione funzioni