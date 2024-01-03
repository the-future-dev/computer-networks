/* client datagram */


#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <string.h>

#define DIM_BUFF 256


typedef struct Calza {
	char identificatore[DIM_BUFF];
	char tipo[DIM_BUFF];
	char carbone;
	char citta[DIM_BUFF];
	char via[DIM_BUFF];
	char messaggio[DIM_BUFF];
} Calza;


int main(int argc, char* argv[]) {           // argv[1] : indirizzo server, argv[2] : port server
     struct hostent *host;
     struct sockaddr_in clientaddr, servaddr;
     int  port, sd, len, count, ok, num1, result;
     char carbone;
     char buff[DIM_BUFF];
     Calza nuova_calza;
     

// INIZIALIZZAZIONE
    // Controllo argomenti
    if(argc != 3) {
        printf("Errore numero argomenti. Usage: %s <IPServer> <PortaServer>\n", argv[0]);
        exit(1);
    }


     // Inizializzazione indirizzo Client e Server
    memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));		// Azzeramento indirizzo client
	clientaddr.sin_family = AF_INET;
	clientaddr.sin_addr.s_addr = INADDR_ANY;
    clientaddr.sin_port = 0;      // Aggancio a qualsiasi porta libera

	memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
	servaddr.sin_family = AF_INET;
	host = gethostbyname (argv[1]);


	// Verifica argv[2] sia intero
	num1 = 0;
	while(argv[2][num1]!= '\0') {
		if((argv[2][num1] < '0') || (argv[2][num1] > '9')) {
			printf("Secondo argomento non intero\n");
			printf("Error:%s serverAddress serverPort\n", argv[0]);
			exit(2);
		}
		num1++;
	}

	// Estrazione e verifica Port e Host
     port = atoi(argv[2]);

	if(port < 1024 || port > 65535) {
		printf("%s = porta scorretta\n", argv[2]);
		exit(2);
	}

	if(host == NULL) {
		printf("%s not found in /etc/hosts\n", argv[1]);
		exit(2);
	}
     else {
          servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr)) -> s_addr;
		servaddr.sin_port = htons(port);
	}


     // Creazione socket
     sd = socket(AF_INET, SOCK_DGRAM, 0);
	if(sd < 0) {
          perror("Errorre apertura socket: ");
          exit(1);
     }
	printf("Client: creata la socket sd=%d\n", sd);


     // Bind Socket a porta scelta dal sistema
     if(bind(sd,(struct sockaddr *)&clientaddr, sizeof(clientaddr)) < 0) {
          perror("Errore bind socket ");
          exit(1);
     }
	printf("Client: bind socket ok, alla porta %i\n", clientaddr.sin_port);



// INIZIO ESECUZIONE

     printf("\nInserire identificatore calza: (ctrl+d per terminare): ");
     while(gets(nuova_calza.identificatore)) {

	printf("Inseriere tipo ('Normale', 'Celiaco'): ");
	gets(buff);
	while(strcmp(buff, "Normale") != 0 && strcmp(buff, "Celiaco") != 0) {
		printf("Errore inserimento tipo, riprovare ('Normale', 'Celiaco'): ");
		gets(buff);
	}
	strcpy(nuova_calza.tipo, buff);
	
	printf("Inseriere carbone ('S', 'N'): ");
	gets(buff);
	while(buff == NULL || strlen(buff) != 1 || (strcmp(buff, "S") != 0 && strcmp(buff, "N") != 0)) {
		printf("Errore inserimento, riprovare: ");
		gets(buff);
	}
	nuova_calza.carbone = buff[0];

	printf("Inseriere citta: ");
	gets(nuova_calza.citta);

	printf("Inseriere via: ");
	gets(nuova_calza.via);

	printf("Inserire messaggio: ");
	gets(nuova_calza.messaggio);
		
          // Invio richiesta a Server
          len = sizeof(servaddr);
	if(sendto(sd, &nuova_calza, sizeof(Calza), 0, (struct sockaddr *)&servaddr, len) < 0) {
            perror("Errore sendto: ");
            printf("\nInserire identificatore calza: (ctrl+d per terminare): ");
            continue;
	}

          // Ricezione risultato
	printf("\nAttesa del risultato...\n\n");
	if(recvfrom(sd, &result, sizeof(result), 0, (struct sockaddr *)&servaddr, &len) < 0) {
		perror("Errore recvfrom: ");
              	printf("\nInserire identificatore calza: (ctrl+d per terminare): ");
               	continue;
      	}
	result = ntohl(result);

	if(result == 0) {
		printf("Inserimento avvenuto\n\n");
	}
	else {
		printf("Errore inserimento\n\n");
	}


	printf("\nInserire identificatore calza: (ctrl+d per terminare): ");	
     }


// TERMINAZIONE

     close(sd);
     printf("\nCLIENT: Terminazione\n");
     exit(0);
}
