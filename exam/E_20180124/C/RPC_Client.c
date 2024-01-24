/* Ritossa Andrea 0001020070 */

/*
 * xfactor_c.c
 */

#include "RPC_xFile.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char *argv[]) {
    char   *host; // nome host
    CLIENT *cl;   // gestore del trasporto

    int    *ris;
    char    c;
    Output * lista_file;
    char    ok[2], nl[2], nomeDirettorio[MAX_NAME_SIZE], prefisso[MAX_NAME_SIZE];
    static Input   input;
    linea   l;

    // Controllo degli argomenti
    if (argc != 2) {
        printf("usage: %s server_host\n", argv[0]);
        exit(1);
    }
    host = argv[1];

    // Creazione gestore del trasporto
    cl = clnt_create(host, OPERATION, OPERATIONVERS, "udp");
    if (cl == NULL) {
        clnt_pcreateerror(host);
        exit(1);
    }

    // Interazione con l'utente
    printf("Inserire:\n1\tLista dei file con un prefisso\n2\tOccorrenze di linea in file\n^D\tper terminare: ");

    while (scanf("%s", ok) == 1) {
	// Consuma fine linea
	gets(&nl);

        /********* 1 - lista file con un prefisso nel nome *********/
        if (strcmp(ok, "1") == 0) {

            printf("\nNome del direttorio: ");
            gets(input.nomeDirettorio);
            printf("\nPrefisso da controllare: ");
            gets(input.prefisso);

            printf("CALL: %s, %s", input.nomeDirettorio, input.prefisso);

            // Invocazione remota
            lista_file = lista_file_prefisso_1(&input, cl);
            
            // Controllo del risultato
            if (ris == NULL) {
                // Errore di RPC
                clnt_perror(cl, host);
                printf("B");
                exit(1);
            } else if (lista_file->size == 0){
                printf("D");
                printf("Nessun file trovato con quel prefisso");
            } else {
                printf("C");
                for (int i = 0; i < lista_file->size; i++) {
                    printf("File [%d]: %s\n", (i), lista_file->files[i]);
                }
            }
        }

        /********* 2 - Inserisci Voto *********/
        else if (strcmp(ok, "2") == 0)
        {
            printf("WIP;");
        }
        
        printf("Inserire:\n1\tGiudice in testa\n2\tEsprimi voto\n^D\tper terminare: ");
    } // while

    // Libero le risorse, distruggendo il gestore di trasporto
    clnt_destroy(cl);
    exit(0);
} // main
