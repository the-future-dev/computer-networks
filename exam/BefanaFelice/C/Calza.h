#ifndef MY_STRUCTURE
# define MY_STRUCTURE

#define DIM_BUFF 256
#define MAX_CALZE 5
#define max(a, b) ((a) > (b) ? (a) : (b))

typedef struct Calza {
	char identificatore[DIM_BUFF];
	char tipo[DIM_BUFF];
	char carbone;
	char citta[DIM_BUFF];
	char via[DIM_BUFF];
	char messaggio[DIM_BUFF];
} Calza;

#endif