#include "RPC_xFile.h"

#define MAX_ENTRIES 20

/* STATO SERVER */
static Prenotazione all_prenotazioni[MAX_ENTRIES];
static int  inizializzato = 0;

void inizializza() {
    int i;
    for (i = 0; i < MAX_ENTRIES; i++){
        strcpy(all_prenotazioni[i].targa, "L");
        strcpy(all_prenotazioni[i].patente, "0");
        strcpy(all_prenotazioni[i].tipo, "L");
        strcpy(all_prenotazioni[i].folder, "L");
    }

    i = 0;
    strcpy(all_prenotazioni[i].targa, "AN745NL");
    strcpy(all_prenotazioni[i].patente, "00003");
    strcpy(all_prenotazioni[i].tipo, "auto");
    strcpy(all_prenotazioni[i].folder, "AN745NL_img/");

    i = 1;
    strcpy(all_prenotazioni[i].targa, "FE457GF");
    strcpy(all_prenotazioni[i].patente, "50006");
    strcpy(all_prenotazioni[i].tipo, "camper");
    strcpy(all_prenotazioni[i].folder, "FE457GF_img/");
}

OutputView * visualizza_prenotazioni_1_svc(InputView * input, struct svc_req *rqstp){
    OutputView * out;
    int size = 0;

    for (int i = 0; i < MAX_ENTRIES && size < MAX_VIEW; i++){
        if (strcmp(all_prenotazioni[i].targa, "L") != 0){
            if (strcmp(all_prenotazioni[i].tipo, input->tipo) == 0){
                out->prenotazioni[size] = all_prenotazioni[i];
                out->size++;
            }
        }
    }

    return out;
}


int * aggiorna_licenza_1_svc(InputAggiornamento * input, struct svc_req *rqstp){
    int i, result;

    if (strlen(input->targa) != 7){
        result = -2;
        return &result;
    }

    for (i = 0; i < strlen(input->targa); i++){
        if (input->targa[i] < '0' || input->targa[i] > '9'){
            result = -1;
            return &result;
        }
    }

    result = 0;
    for (i = 0; i < MAX_ENTRIES && result == 0; i++){
        if (strcmp(input->targa, all_prenotazioni[i].targa) == 0){
            result = 1;
            strcpy(all_prenotazioni[i].patente, input->new_patente);
        }
    }
    
    return &result;
}

