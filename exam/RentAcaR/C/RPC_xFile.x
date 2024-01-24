/* 
 * sala.x
 *	+ definizione Input e struttura della sala.
 * 	+ definizione metodi e tipi richiesti/restituiti
 */

const MAX_VIEW=6;

struct InputView{
	char tipo[6];
};

struct Prenotazione{
	char targa[7];
	char patente [5];
	char tipo[6];
	char folder[12];
};

struct OutputView {
	Prenotazione prenotazioni[MAX_VIEW];
	int size;
};

struct InputAggiornamento {
	char targa[7];
	char new_patente[5];
};

program XFILES {
	version XFILESVERS{
		OutputView VISUALIZZA_PRENOTAZIONI(InputView) = 1;
		int AGGIORNA_LICENZA(InputAggiornamento) = 2;
	} = 1;
} = 0x20000013;