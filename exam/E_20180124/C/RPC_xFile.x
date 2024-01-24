/* Ritossa Andrea 0001020070 */

const MAX_NAME_SIZE = 128;
const MAX_FILES = 6;

struct str{
	char name [MAX_NAME_SIZE];
}

struct Output{
	str files [MAX_FILES];
	int size;
}; 

struct Input{
	char nomeDirettorio[MAX_NAME_SIZE];
	char prefisso[MAX_NAME_SIZE];
};
  
program OPERATION {
	version OPERATIONVERS {         
		Output LISTA_FILE_PREFISSO(Input) = 1;        
        int CONTA_OCCORRENZA_LINEA(str) = 2;
	} = 1;
} = 0x20000013;

