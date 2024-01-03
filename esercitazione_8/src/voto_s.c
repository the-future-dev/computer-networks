#include "voto.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <string.h>

static int  inizializzato = 0;
static Candidato lista_candidati[MAX_CANDIDATI];
static ClassificaGiudici classifica_giudici;

void inizializza() {
    if (inizializzato == 1)
        return;
    printf("Inizio inizializzazione.\n");

    int i;

    for (i = 0; i < MAX_CANDIDATI; i++){
        strcpy(lista_candidati[i].nome_candidato, "L");
        strcpy(lista_candidati[i].nome_giudice, "L");
        lista_candidati[i].categoria = 'L';
        strcpy(lista_candidati[i].nome_file, "L");
        lista_candidati[i].fase = 'L';
        lista_candidati[i].voto = -1;
    }

    strcpy(classifica_giudici.classifica[0].nome_giudice, "A");
    classifica_giudici.classifica[0].voti = 0;
    strcpy(classifica_giudici.classifica[1].nome_giudice, "B");
    classifica_giudici.classifica[1].voti = 0;
    strcpy(classifica_giudici.classifica[2].nome_giudice, "C");
    classifica_giudici.classifica[2].voti = 0;
    strcpy(classifica_giudici.classifica[3].nome_giudice, "D");
    classifica_giudici.classifica[3].voti = 0;

    i = 0;
    strcpy(lista_candidati[i].nome_candidato, "Andrea");
    strcpy(lista_candidati[i].nome_giudice, "A");
    lista_candidati[i].categoria = 'U';
    strcpy(lista_candidati[i].nome_file, "andrea");
    lista_candidati[i].fase = 'A';
    lista_candidati[i].voto = 10;
    classifica_giudici.classifica[i].voti += lista_candidati[i].voto;

    i = 2;
    strcpy(lista_candidati[i].nome_candidato, "Paolo");
    strcpy(lista_candidati[i].nome_giudice, "A");
    lista_candidati[i].categoria = 'O';
    strcpy(lista_candidati[i].nome_file, "paolo");
    lista_candidati[i].fase = 'B';
    lista_candidati[i].voto = 5;
    classifica_giudici.classifica[i].voti += lista_candidati[i].voto;

    i = 4;
    strcpy(lista_candidati[i].nome_candidato, "Mario");
    strcpy(lista_candidati[i].nome_giudice, "B");
    lista_candidati[i].categoria = 'B';
    strcpy(lista_candidati[i].nome_file, "mario");
    lista_candidati[i].fase = 'A';
    lista_candidati[i].voto = 4;
    classifica_giudici.classifica[i].voti += lista_candidati[i].voto;

    i = 5;
    strcpy(lista_candidati[i].nome_candidato, "Agata");
    strcpy(lista_candidati[i].nome_giudice, "C");
    lista_candidati[i].categoria = 'D';
    strcpy(lista_candidati[i].nome_file, "agata");
    lista_candidati[i].fase = 'B';
    lista_candidati[i].voto = 19;
    classifica_giudici.classifica[i].voti += lista_candidati[i].voto;

    i = 9;
    strcpy(lista_candidati[i].nome_candidato, "Elisa");
    strcpy(lista_candidati[i].nome_giudice, "D");
    lista_candidati[i].categoria = 'D';
    strcpy(lista_candidati[i].nome_file, "elisa");
    lista_candidati[i].fase = 'S';
    lista_candidati[i].voto = 10;
    classifica_giudici.classifica[i].voti += lista_candidati[i].voto;

    printf("NOME C, NOME G, CATEGORIA, FILE, FASE, VOTO");
    // for (i = 0; i < MAX_CANDIDATI; i++){
    //     printf("Candidato: %s | %s | %c | %s | %c | %d\n",
    //         lista_candidati[i].nome_candidato, lista_candidati[i].nome_giudice, lista_candidati[i].categoria = 'L',
    //         lista_candidati[i].nome_file,lista_candidati[i].fase, lista_candidati[i].voto);
    // }

    for (i = 0; i < NUM_GIUDICI; i++){
        printf("G: %s | %d\n",
            classifica_giudici.classifica[i].nome_giudice,
            classifica_giudici.classifica[i].voti);
    }

    printf("Strutture dati inizializzate con successo.\n");
}

ClassificaGiudici * visualizza_classifica_1_svc(void *in, struct svc_req *rqstp) {
    if (inizializzato == 0)
        inizializza();
    printf("\n> richiesta visualizza classifica\n");
    return (&classifica_giudici);
}

/*InputVoto: nome_candidato, voto_espressione; */
int * esprimi_voto_1_svc(InputVoto *input, struct svc_req *rqstp) {
    static int result = -1;
    int i_c, i_g, t_c, t_g;
    int a_S = 0;
    char * nome_giudice;

    if (inizializzato == 0)
        inizializza();
    printf("\n> richiesta voto %s %c\n", input->nome_candidato, input->voto_espressione);
    
    if ((input->voto_espressione != 'A' && input->voto_espressione != 'S') || (strlen(input->nome_candidato) > MAX_LENGTH_NOME)){
        printf("Errore in Input Voto: {%s, %d}", input->nome_candidato, input->voto_espressione);
        return &result;
    } else {
        if (input->voto_espressione == 'A')
            a_S = +1;
        else
            a_S = -1;
    }

    printf("%c -> %d\n", input->voto_espressione, a_S);

    t_c = 0;
    for (i_c = 0; (i_c < MAX_CANDIDATI && t_c == 0); i_c++){
        if (strcmp(lista_candidati[i_c].nome_candidato, input->nome_candidato) == 0){
            printf("\n>%d Candidato: %s | Giudice: %s| ", i_c, lista_candidati[i_c].nome_candidato, lista_candidati[i_c].nome_giudice);
            nome_giudice = lista_candidati[i_c].nome_giudice;
            t_c = 1;
        }
    }

    printf("\n> %s\n", nome_giudice);

    if (t_c == 0){
        printf("Candidato %s not found in lista.", input->nome_candidato);
        return (&result);
    }

    t_g = 0;
    for (i_g = 0; (i_g < NUM_GIUDICI && t_g == 0); i_g++){
        if (strcmp(classifica_giudici.classifica[i_g].nome_giudice, nome_giudice) == 0){
            t_g = 1;
            lista_candidati[i_c].voto += a_S;
            classifica_giudici.classifica[i_g].voti += a_S;
        }
    }

    if (t_c != 1 || t_g != 1){
        printf("Candidato trovato ma giudice non trovato: INCONSISTENZA INTERNA!!");
        printf("Candidato: %s | Giudice %s", lista_candidati[i_c].nome_candidato, lista_candidati[i_c].nome_giudice);
        return (&result);
    } else {
        result = +1;
    }
    return (&result);
}