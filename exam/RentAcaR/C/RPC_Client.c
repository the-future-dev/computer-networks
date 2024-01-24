#include "RPC_xFile.h"
#include <rpc/rpc.h>
#include <stdio.h>

int main(int argc, char *argv[])
{
    CLIENT      *cl;
    char        *host;
    char        choice;

    
    InputAggiornamento      input_aggiornamento;
    int                     *output_aggiornamento;
    InputView               input_view;
    OutputView              *output_view;


    if (argc != 2){
        printf("Usage: %s server_host\n", argv[0]);
        exit(1);
    }
    host = argv[1];

    cl = clnt_create(host, XFILES, XFILESVERS, "udp");
    if (cl == NULL){
        clnt_pcreateerror(host);
        exit(1);
    }

    printf("MENU:\n\tC: visualizza la classifica dei giudici\n\tV: vota per un partecipante\n\t^D per terminare\n>");
    while (gets(&choice))
    {
        switch (choice)
        {
        case ('C'):
            /*
             * VISUALIZZA CLASSIFICA
            */
            printf("Inserisci il tipo di prenotazione"):
            gets(input_view.tipo);

            if ((output_view = visualizza_prenotazioni_1(&input_view, cl)) == NULL){
                clnt_perror(cl, host);
                exit(1);
            }
            printf("\nClassifica\n");
            for (int i = 0; i < output_view->size; i++){
                printf("\tPrenotazione [i]",output_view->classifica[i].nome_giudice, classifica->classifica[i].voti);                
            }
            break;
        
        case ('V'):
            /*
             * VOTA
            */
            printf("\nVota:\n");
            printf("Nome partecipante: ");
            gets(input_voto.nome_candidato);
            printf("Aggiungi o Sottrai il tuo voto (A || S)?");
            gets(&choice);
            input_voto.voto_espressione = choice;

            if (input_voto.voto_espressione != 'A' && input_voto.voto_espressione != 'S'){
                printf("VOTO NON VALIDO");
            } else {
                res_voto = esprimi_voto_1(&input_voto, cl);
                if (res_voto == NULL){
                    clnt_perror(cl, host);
                    exit(1);
                }

                printf("Risultato Voto: %d", *res_voto);
            }
            break;

        default:
            printf("Scegli tra {C, V, Ctrl+D}.\n");
            break;
        } // switch

        printf("MENU:\n\tC: visualizza la classifica dei giudici\n\tV: vota per un partecipante\n\t^D per terminare\n>");
    }// while
    
    clnt_destroy(cl);
    exit(0);
}