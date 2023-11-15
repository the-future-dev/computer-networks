## Computer Networks - Reti di calcolatori T
Questa repository continene le esercitazioni del corso di "Reti di calcolatori T" appartenente al corso di Laure Triennale in Ingegneria Informatica dell'Alma Mater Studiorum di Bologna.

Autori:
 - Nicola Leonetti @
 - Andrea Ritossa @the-future-dev
 - Alessandro Quaranta @
 - Gaia Verzelli @

Consigli:
 - creare un workspace eclipse contentente l'intera cartella e ogni esercitazione è un java project
 - `output.txt` sia usato come nome per l'output in file di testo

- Esecuzione di un file C:
> gcc filename.c name
> name arg1 arg2

- Esecuzione di un file Java:
> javac filename.java       // shortcut javac *.java
> java filename arg1 arg2

0. ### Lettura e Scrittura di File in Java e C
    a. Initialization

    Server - Produttore: crea il file; chiede all'utente quante righe vuole scrivere; scrive il contenuto digitato dall'utente **riga per riga**.
    Client - Consumatore (filtro): apre il file passato come argomento; legge il contenuto fino a `EOF`.

    b. Changes
    - Server: non venga chiesto il numero di righe da scrivere, ma si legga fino a quando l'utente inserisce `EOF`.
    - Client: implementare un filtro di elaborazione tra la lettura e la stampa a video che tagli i caratteri passati come argomento.
    - Client: ridirezione in ingresso:
        possa prendere il contenuto sia da input
        sia da un file passato come argomento

    c. Extension
    - Il produttore non chiede all’utente quante righe scrivere, ma è un programma sequenziale che legge fino a quando l’utente immette EOF (end of file) per terminare l’inserimento (è un filtro).
    - l consumatore agisce in modo concorrente, lanciando diversi processi figli che lavorano in modo indipendente, ciascuno su uno dei file passati come argomenti in ordine Il processo padre controlla gli argomenti e genera i figli per il processing degli argomenti correttamente ricevuti (cioè file esistenti e presenti nel direttorio locale), quindi termina Ciascun processo figlio è un filtro che legge il file fino a EOF (end of file). Si noti che gli output generati non devono essere scritti su standard output, ma rispettivamente scritti sui singoli file di testo passati come argomenti all’invocazione (ovviamente cambiandone il contenuto)

1. ### Socket Java senza connessione
Client (filtro): invia al server pacchetti contententi "nome di file di testo" e "numero di linea nel file".
    unique request as it is without connection
Server: receives the request from the client; verifies the file and the line existance; returns a response to the client: error or line content.
    sequential (mono-process) that executes an endless loop


3. ### Socket C
    #### LongerWord: senza connessione.
    - Client: fa una richiesta con il nome di un file di testo (stringa) e riceve in risposta il numero di carateri della parola più lunga del file (int).
    - Server: riceve il nome del file, se esiste lo analizza per identificare la parola formata dal maggior numero di lettere e rispondere con questo al cliente.
    
    N.B: essendo la socket senza connessione utilizziamo la socket UPD (User Datagram Protocol).
    > sd = socket(AF_INET, SOCK_DGRAM, 0);
    > sendto(...)
    > recvfrom(...)

    #### RowDeletion: con connessione.
    - Client:
        user input: nome file (stringa) e numero di linea (int).
        invia la richiesta al server
        riceve come risposta il nuovo contenuto del file
        lo inserisce nel file system, stampandolo a video. 

    - Server:
        gestisce in **modo parallelo** la funzionalità di eliminazione della linea.
        per ogni richiesta il processo figlio:
            - riceve il nome file (stringa) e numero di linea (int)
            - effettua l'eliminazione
            - restituisce il risultato al client.

    N.B: esssendo la socket con connessione
    > listen_sd = socket(AF_INET, SOCK_STREAM, 0);
    > if ((connection_sd = accept(listen_sd, (struct sockaddr_in *)&client_address, &len)) < 0) {...}
