/* server (select) */


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <errno.h>
#include <fcntl.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

#define DIM_BUFF 256
#define MAX_CALZE 5
#define max(a,b) ((a) > (b) ? (a) : (b))


void gestore(int signo) {
	int stato;
	printf("Esecuzione gestore di SIGCHLD\n");
	wait(&stato);
}


typedef struct Calza {
	char identificatore[DIM_BUFF];
	char tipo[DIM_BUFF];
	char carbone;
	char citta[DIM_BUFF];
	char via[DIM_BUFF];
	char messaggio[DIM_BUFF];
} Calza;


void stampa_tabella(Calza* arr_calze);


int main(int argc, char **argv){        // argv[1] : serverPort
	struct sockaddr_in cliaddr, servaddr;
	int  listenfd, connfd, udpfd, nready, maxfdp1;
	fd_set rset;
	const int on = 1;
	char buff[DIM_BUFF], tipo[DIM_BUFF], carbone, next_carbone;
	int len, nread, ris, port, i, valid, found_free, result_datagram, length_tipo, length_next, dim_result, error;
	Calza arr_calze[MAX_CALZE];
	Calza nuova_calza;


     // Controllo argomenti
     if(argc != 2) {
		printf("Errore argomenti. Usage: %s <serverPort>  \n", argv[0]);
		exit(1);
	}

     // Verifica che argomento sia intero
     nread = 0;
     while(argv[1][nread] != '\0') {
          if((argv[1][nread] < '0') || (argv[1][nread] > '9')) {
               printf("Secondo argomento non intero\n");
               exit(2);
          }
          nread++;
     }
     port = atoi(argv[1]);

     if(port < 1024 || port > 65535) {
     	printf("Error: %s port\n", argv[0]);
     	printf("1024 <= port <= 65535\n");
     	exit(2);
     }
     
     
     // Inizializzazione struttura dati
    for(i = 0; i < MAX_CALZE; i++) {
	strcpy(arr_calze[i].identificatore, "L");
	strcpy(arr_calze[i].tipo, "L");
	arr_calze[i].carbone = 'L';
	strcpy(arr_calze[i].citta, "L");
	strcpy(arr_calze[i].via, "L");
	strcpy(arr_calze[i].messaggio, "L");
     }

	strcpy(arr_calze[0].identificatore, "MarioRossi1");
	strcpy(arr_calze[0].tipo, "Normale");
	arr_calze[0].carbone = 'N';
	strcpy(arr_calze[0].citta, "Bologna");
	strcpy(arr_calze[0].via, "Saragozza");
	strcpy(arr_calze[0].messaggio, "Bravo Mario!");

	strcpy(arr_calze[1].identificatore, "MarioBianchi1");
	strcpy(arr_calze[1].tipo, "Celiaco");
	arr_calze[1].carbone = 'S';
	strcpy(arr_calze[1].citta, "Roma");
	strcpy(arr_calze[1].via, "Veneto");
	strcpy(arr_calze[1].messaggio, "Mario sei birichino");

	strcpy(arr_calze[2].identificatore, "MariaRossi12");
	strcpy(arr_calze[2].tipo, "Normale");
	arr_calze[2].carbone = 'S';
	strcpy(arr_calze[2].citta, "Firenze");
	strcpy(arr_calze[2].via, "Larga");
	strcpy(arr_calze[2].messaggio, "Maria comportati meglio");
     
     stampa_tabella(arr_calze);
     

     // Inizializzazione indirizzo Server e bind
	memset ((char *)&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = INADDR_ANY;
	servaddr.sin_port = htons(port);
	printf("Server avviato\n");


	// Creazione e settaggio Socket TCP
	listenfd=socket(AF_INET, SOCK_STREAM, 0);
	if(listenfd < 0) {
		perror("Errore apertura socket TCP ");
		exit(1);
	}
	printf("Creata la socket TCP d'ascolto, fd = %d\n", listenfd);

	if(setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
		perror("set opzioni socket TCP");
		exit(2);
	}
	printf("Set opzioni socket TCP ok\n");

	if(bind(listenfd,(struct sockaddr *) &servaddr, sizeof(servaddr)) < 0) {
		perror("bind socket TCP");
		exit(3);
	}
	printf("Bind socket TCP ok\n");

	if(listen(listenfd, 5) < 0) {
		perror("listen");
		exit(4);
	}
	printf("Listen ok\n");

	// Creazione Socket UDP
	udpfd = socket(AF_INET, SOCK_DGRAM, 0);
	if(udpfd < 0) {perror("apertura socket UDP");
		exit(5);
	}
	printf("Creata la socket UDP, fd=%d\n", udpfd);

	if(setsockopt(udpfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
		perror("Errore set opzioni socket UDP");
		exit(6);
	}
	printf("Set opzioni socket UDP ok\n");

	if(bind(udpfd,(struct sockaddr *) &servaddr, sizeof(servaddr)) < 0) {
		perror("Errore bind socket UDP");
		exit(7);
	}
	printf("Bind socket UDP ok\n");

     // Aggancio gestore per evitare figli zombie
	signal(SIGCHLD, gestore);

     // Pulizia e settaggio Maschera dei file descriptor
	FD_ZERO(&rset);
	maxfdp1 = max(listenfd, udpfd) + 1;

	printf("\n");

// INIZIO ESECUZIONE

    // Cicli richieste
    while(1) {

		FD_SET(listenfd, &rset);
		FD_SET(udpfd, &rset);

		if((nready = select(maxfdp1, &rset, NULL, NULL, NULL)) < 0) {
			if(errno == EINTR)
                    continue;
			else {
                    perror("Errore select ");
                    exit(8);
            }
		}


		// STREAM: gestione richieste (tipo, carbone)
		if(FD_ISSET(listenfd, &rset)) {
			len = sizeof(struct sockaddr_in);
			if((connfd = accept(listenfd, (struct sockaddr *)&cliaddr, &len)) < 0) {
				if(errno == EINTR)
                         continue;
				else {
                         perror("Errore accept");
                         exit(9);
                    }
			}

			if(fork() == 0) { // Figlio
				close(listenfd);

				while(read(connfd, &length_tipo, sizeof(length_tipo)) > 0) {			
					
					// Lettura lunghezza tipo
					length_tipo = ntohl(length_tipo);

					// Lettura tipo
					if(read(connfd, tipo, sizeof(char) * length_tipo) < 0) {		
		                    		perror("Errore read ");
		                   	 	break;
		               		}

					// Lettura presenza carbone
					if(read(connfd, &carbone, sizeof(carbone)) < 0) {		
		                    		perror("Errore read ");
		                    		break;
		               		}


					printf("\nRichiesti: %s, %c\n\n", tipo, carbone);

					dim_result = 0;
					for(i = 0; i < MAX_CALZE; i++) {
						if(strcmp(arr_calze[i].identificatore, "L") != 0 && strcmp(arr_calze[i].tipo, tipo) == 0 && arr_calze[i].carbone == carbone) {
							dim_result++;
						}
					}

					printf("\nCalze corrispondenti trovate: %d\n", dim_result);
					dim_result = htonl(dim_result);
					if(write(connfd, &dim_result, sizeof(dim_result)) < 0) {
						perror("Errore write ");
						continue;
					}

					error = 0;
					for(i = 0; i < MAX_CALZE && !error; i++) {
						if(strcmp(arr_calze[i].identificatore, "L") != 0 && strcmp(arr_calze[i].tipo, tipo) == 0 &&
						arr_calze[i].carbone == carbone) {
						
							length_next = strlen(arr_calze[i].identificatore) + 1;
							length_next = htonl(length_next);
							if(write(connfd, &length_next, sizeof(length_next)) < 0) {
								perror("Errore write ");
								error = 1;
								continue;
							}

							strcpy(buff, arr_calze[i].identificatore);
							if(write(connfd, buff, sizeof(char) * ntohl(length_next)) < 0) {
								perror("Errore write ");
								error = 1;
								continue;
							}
						}	
					}
					
					printf("\nInvio terminato\n");
					
				} // while
				
				// Figlio chiude la sua Socket
				printf("\n\nFiglio %i: termino\n\n", getpid());
				close(connfd);
				exit(0);

			} // Fine figlio

			close(connfd);
		}



// DATAGRAM: gestione richieste inserimento
		if(FD_ISSET(udpfd, &rset)) {
			printf("(DATAGRAM) Ricevuta richiesta inserimento\n");

			len = sizeof(struct sockaddr_in);
			if(recvfrom(udpfd, &nuova_calza, sizeof(Calza), 0, (struct sockaddr *)&cliaddr, &len) < 0) {
		            perror("Errore recvfrom ");
		            continue;
		       	}

			printf("Identificatore nuova calza: %s\n", nuova_calza.identificatore);
		
			valid = 1;
			if((strcmp(nuova_calza.tipo, "Normale") != 0 && strcmp(nuova_calza.tipo, "Celiaco") != 0) ||
			   (nuova_calza.carbone != 'N' && nuova_calza.carbone != 'S')) {
				valid = 0;
				printf("Problema tipo\n");
			}
			if(valid) {

				for(i = 0; i < MAX_CALZE && valid; i++) {
					if(strcmp(arr_calze[i].identificatore, nuova_calza.identificatore) == 0) {
						valid = 0;
						printf("Identificatore gia presente\n");
					}
				}
			}
		

			if(valid) {
				found_free = 0;
				for(i = 0; i < MAX_CALZE && !found_free; i++) {
					if(strcmp(arr_calze[i].identificatore, "L") == 0) {
						found_free = 1;
						strcpy(arr_calze[i].identificatore, nuova_calza.identificatore);
						strcpy(arr_calze[i].tipo, nuova_calza.tipo);
						arr_calze[i].carbone = nuova_calza.carbone;
						strcpy(arr_calze[i].citta, nuova_calza.citta);
						strcpy(arr_calze[i].via, nuova_calza.via);
						strcpy(arr_calze[i].messaggio, nuova_calza.messaggio);
					}
				}
			}

			if(valid && found_free)
				result_datagram = 0;
			else
				result_datagram = -1;

			printf("Invio risultato: %d\n", result_datagram);
			result_datagram = htonl(result_datagram);
			if(sendto(udpfd, &result_datagram, sizeof(result_datagram), 0, (struct sockaddr *)&cliaddr, len) < 0) {
		            perror("Errore sendto ");
		            continue;
		       }

			stampa_tabella(arr_calze);
		       
		       printf("\nInvio avvenuto\n");
		}

	}


	exit(0);
}


void stampa_tabella(Calza* arr_calze) {
	int i;
	
	for(i = 0; i < MAX_CALZE; i++) {
		printf("%s\t", arr_calze[i].identificatore);
		printf("%s\t", arr_calze[i].tipo);
		printf("%s\t", arr_calze[i].citta);
		printf("%s\t", arr_calze[i].via);
		printf("%s\t", arr_calze[i].messaggio);
		printf("%c\t", arr_calze[i].carbone);
		printf("\n");
	}

}

