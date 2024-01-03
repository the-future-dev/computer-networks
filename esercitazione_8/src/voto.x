/* 
 * sala.x
 *	+ definizione Input e struttura della sala.
 * 	+ definizione metodi e tipi richiesti/restituiti
 */

const NUM_GIUDICI=4;
const MAX_LENGTH_NOME=10;
const MAX_CANDIDATI=10;

struct InputVoto{
	char nome_candidato[MAX_LENGTH_NOME];
    char voto_espressione; /* A: aggiunta +1 | S: sottrazione -1 */
};

struct VotoGiudice {
    char nome_giudice[MAX_LENGTH_NOME];
    int voti;
};

struct ClassificaGiudici{
    VotoGiudice classifica[NUM_GIUDICI];
};

struct Candidato {
    char nome_candidato[MAX_LENGTH_NOME];
    char nome_giudice[MAX_LENGTH_NOME];
    char categoria;
    char nome_file[MAX_LENGTH_NOME];
    char fase;
    int voto;
};

program VOTO {
	version VOTOVERS{
		ClassificaGiudici VISUALIZZA_CLASSIFICA(void) = 1;
		int ESPRIMI_VOTO(InputVoto) = 2;
	} = 1;
} = 0x20000013;
