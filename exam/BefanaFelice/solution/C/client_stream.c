/* client stream */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <string.h>


#define DIM_BUFF 256


int main(int argc, char* argv[]) {      // argv[1] : IP Server, argv[2] : Porta Server
    	int sd, port, nread;
	struct hostent *host;
	struct sockaddr_in servaddr;
     	char tipo[DIM_BUFF], buff[DIM_BUFF], buff_next[DIM_BUFF], carbone, next_char;
	int i, j, length_tipo, length_next, dim_result, error;


     // INIZIALIZZAZIONE

     // Controllo argomenti
     if(argc != 3) {
		printf("Errore numero argomenti. Usage: %s <IPServer> <PortaServer> \n", argv[0]);
		exit(1);
	}


     // Inizializzazione indirizzo Server
	memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
	servaddr.sin_family = AF_INET;
	host = gethostbyname(argv[1]);

	// Verifica che argomento sia intero
	nread = 0;
	while(argv[2][nread] != '\0') {
		if((argv[2][nread] < '0') || (argv[2][nread] > '9')) {
			printf("Secondo argomento non intero\n");
			exit(2);
		}
		nread++;
	}

	port = atoi(argv[2]);

	// Verifica Port e Host
	if(port < 1024 || port > 65535) {
		printf("%s = porta scorretta\n", argv[2]);
		exit(2);
	}
	if(host == NULL) {
		printf("%s not found in /etc/hosts\n", argv[1]);
		exit(2);
	}

     servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
	servaddr.sin_port = htons(port);


	// Creazione Socket
     sd = socket(AF_INET, SOCK_STREAM, 0);
	if(sd < 0){
		perror("Errore apertura socket ");
		exit(3);
	}
	printf("Creata la socket: %d\n", sd);

    	// Connect (bind implicito)
     if(connect(sd, (struct sockaddr *)&servaddr, sizeof(struct sockaddr)) < 0) {
		perror("Errore connect ");
		exit(4);
	}
	printf("Connect ok\n");


	// INIZIO ESECUZIONE

     printf("\nInserire tipo calza ("Normale", "Celiaco"): (ctrl+d per terminare): ");
     while(gets(tipo)) {

		printf("Inserire carbone ('S', 'N'): ");
		gets(buff);
		while(buff == NULL || strlen(buff) != 1 || (strcmp(buff, "S") != 0 && strcmp(buff, "N") != 0)) {
			printf("Errore inserimento, riprovare ('S', 'N'): ");
			gets(buff);
		}
		carbone = buff[0];

		length_tipo = strlen(tipo) + 1;
		length_tipo = htonl(length_tipo);

		// Invio lunghezza tipo
		if(write(sd, &length_tipo, sizeof(length_tipo)) < 0) {
			perror("Errore write ");
			printf("\nInserire tipo calza: (ctrl+d per terminare): ");
			continue;
		}

		// Invio tipo
		if(write(sd, tipo, sizeof(char) * ntohl(length_tipo)) < 0) {
				perror("Errore write ");
				printf("\nInserire tipo calza: (ctrl+d per terminare): ");
		continue;
		}

		// Invio presenza carbone
		if(write(sd, &carbone, sizeof(carbone)) < 0) {
					perror("Errore write ");
					printf("\nInserire tipo calza: (ctrl+d per terminare): ");
			continue;
		}

		printf("\nRichiesta inviata\n");

			// Ricezione numero entry trovate
			if(read(sd, &dim_result, sizeof(dim_result)) < 0) {
			perror("Errore read ");
			printf("\nInserire tipo calza: (ctrl+d per terminare): ");
			continue;
		}
		dim_result = ntohl(dim_result);

		if(dim_result == 0) {          // Nessuna calza trovata
			printf("\nNessuna calza trovata\n");
		}
		else {
			printf("\nNumero calze trovate: %d\n", dim_result);

			error = 0;
			for(i = 0; i < dim_result && !error; i++) {
					
				if(read(sd, &length_next, sizeof(length_next)) < 0) {
					perror("Errore read ");
					error = 1;
					continue;
				}
				length_next = ntohl(length_next);
						
				if(read(sd, buff_next, sizeof(char) * length_next) < 0) {
					perror("Errore read ");
					error = 1;
					continue;
				}
				printf("%s\t", buff_next);
			}

			printf("\nFine ricezione\n");
		}

		printf("\nInserire tipo calza: (ctrl+d per terminare): ");

    } //while

     close(sd);
     printf("\nCLIENT: Terminazione\n");
     exit(0);
}
