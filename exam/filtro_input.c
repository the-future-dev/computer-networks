printf("Inserisci la nuova riga\n");
while(gets(riga)) { /* la gets legge tutta la riga, separatori inclusi, e trasforma il fine linea in fine stringa */
    riga[strlen(riga) + 1] = '\0'; // aggiungo 0 binario per permettere una corretta strlen()
    riga[strlen(riga)]     = '\n'; // aggiungo il fine linea
    
    [...]

    printf("Inserisci la nuova riga\n");
}