## Computer Networks - Reti di calcolatori T
Questa repository continene le esercitazioni del corso di "Reti di calcolatori T" appartenente al corso di Laure Triennale in Ingegneria Informatica dell'Alma Mater Studiorum di Bologna.

Autori:
 - Leonetti Nicola
 - Ritossa Andrea
 - Quaranta Alessandro
 - Verzelli Gaia

Consigli:
 - creare un workspace eclipse contentente l'intera cartella e ogni esercitazione è un java project
 - `output.txt` sia usato come nome per l'output in file di testo

- Esecuzione di un file C:
> gcc filename.c name
> name arg1 arg2

- Esecuzione di un file Java:
> javac filename.java       // shortcut: javac *.java
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
    Server: receives the request from the client; verifies the file and the line existance; returns a response to the client: error or line content.
        sequential (mono-process) that executes an endless loop

    **Discovery Server**:
        come Sistema di Nomi supporta
            - viene chiamato dai Client per conoscere il Server appropriato.
            - registrazione **dinamica** dei Server, tramite: (indirizzo, porta e nome_file_txt), con verifica di unicità: unico nome_file e unico endpoint (IP, porta)

    **Swap Server**:
        Applicazione a più processi che permette ai clienti di fare richiesta per scambiare due righe in un file di testo.

    Assunzioni:
    - IP e porta del discovery server è nota a tutti
    - Le comunicazioni avvengono mediante **datagram socket**.

2. ### Socket Java con connessione

    #### Svolta
    - FiltroSemplice: esempio di **filtro** in Java.

    Esempio di socket Java con connessione.

    Il Client
    (nome file) -> 
        1. connette al server
        2. crea stream output sulla connessione
        3. invia nome
        4. invia file
        5. attende risposta
        6. nuovo file?

    Il Server
    (richiesta di connessione) - crea (Stream di Input per ricevere) - (nome file, contenuto file)
        ->
        1. Salva file nel system locale
        2. Invia esito operazione
        3. Chiude la connessione

    => Java server con connessione, sequenziale: `FutFileServerSeq.java`
    => Java server con connessione, concorrente: `FutFileServerCon.java`

    #### Proposta
    Trasferimento dal Client al server di tutti i file di un direttorio, con un vincolo sulla grandezza dei file: deve essere maggiore di una soglia specificata dall'utente.

    Client:
    (nome direttorio) -> per ogni (file) ->
    1. invio nome
    2. risposta server:
    - "attiva"
        3a. invia lunghezza file e poi tutto il file
    - "salta"
        3b. next file.

        Server:
    Attende richiesta di connessione (ServerSocket). Usa (Socket) connessa per stream input (riceve nomi dei file e contenuto) e stream output (invia comando di attivazione / salta trasferimento).

    Realizzazione sia sequenziale sia concorrente per la gestione delle richieste: N.B. la stessa connessione e socket viene usata per tutti i file del direttorio di una richiesta.


3. ### Socket C
    #### LongerWord: senza connessione.
        aka longest-word
    - Client: fa una richiesta con il nome di un file di testo (stringa) e riceve in risposta il numero di carateri della parola più lunga del file (int).
    - Server: riceve il nome del file, se esiste lo analizza per identificare la parola formata dal maggior numero di lettere e rispondere con questo al cliente.
    
    N.B: essendo la **socket senza connessione** utilizziamo la socket UPD (User Datagram Protocol).
    > sd = socket(AF_INET, SOCK_DGRAM, 0);
    > sendto(...)
    > recvfrom(...)

    #### RowDeletion: con connessione.
        aka del_line
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

    N.B: esssendo la **socket con connessione** utilizziamo TCP (Transmission Control Protocol).
    > listen_sd = socket(AF_INET, SOCK_STREAM, 0);
    > if ((connection_sd = accept(listen_sd, (struct sockaddr_in *)&client_address, &len)) < 0) {...}
    > read(connection_sd, &req, sizeof(req));
    > write(connection_sd, &ch, sizeof(char));
    > dup(connection_sd);
    > close(connection_sd);

    ##### Soluzione proposta
    Nella [soluzione proposta](./esercitazione_3/srcProp/) il file appartiene al lato client. Questo implica che:
    - il file sia aperto dal client che lo legge,
    - lo invia al server,
    - riceve il nuovo contenuto dal server
    - salva il nuovo file passato dal server.


4. ### Server Multiservizio: Socket C con select
    Applicazione C/S che fornisce due servizi:
        a. Eliminare tutte le occorrenze di una parola in un file del server remoto.
            - socket datagram
        b. Restituire tutti i nomi dei file (sul server) presenti nei direttori di secondo livello rispetto ad un direttorio indicato da utente.
            - socket stream

    Bisogna quindi sviluppare **due Clients**:
        a. Eliminazione parola in un file: 
            - ciclicamente chiede in input: nome del file e parola da filtrare
            - manda la richiesta di eliminazione
            - riceve e mostra la risposta con l'esito dell'operazione, ovvero il numero di volte che la parola è stata eliminata dal file.
        b. Visione del direttorio:
            - ciclicamente chiede in input: nome del direttorio
            - manda la richiesta
            - riceve e mostra la risposta con la lista di nomi di file remoti.

    Il **Server**:
        - discrimina le richieste usando **select**
        a. Eliminazione:
            Gestisce le richieste in maniera **sequenziale** usando una **socket datagram**.
            - Prende in input (nome_file e parola);
            - se il file esiste, elimina le occorrenze
            - invia al client l'intero positivo corrispondente alle eliminazioni eseguite.
        b. Visione del direttorio:
            Gestisce le richieste in maniera **concorrente** con un processo per ogni client (**multiprocesso**) socket stream.
            - in input riceve il nome del direttorio
            - in output:
                -- se direttorio esiste: restituisce il nome dei file contenuti in tutti i direttori di secondo livello
                -- in caso di errore: notificare l'errore
            N.B: il server chiude la connessione solamente alla ricezione del fine file dal client
        
        Esistono due protocolli per il servizio stream:
            - schema 1: Una connessione per ogni richiesta.
            - schema 2: una sola connessione per tutta la sessione cliente.

5. ### Java RMI
    #### Svolta
    Gestione delle registrazioni ad un congresso.

    Due operazioni:
    a. registrare uno speaker
    b. visionare il programma

    Client:
    - processo ciclico per richieste sincrone fino a fine richieste utente
    - gestisce condizioni anomale: (registrazione a sessione inesistente // sessione piena)

    Server:
    - mantiene i programmi delle 3 giornate del congresso, con 12 sessioni per giornata; per ciascuna sessione, vengono memorizzati gli speaker.

    #### Proposta
    Operazioni:
    a. **conta_righe**:     (fileDiTesto, threshold) => return #righe con un #parole maggiore di threshold.
    solleva eccezione remota ex. se il file non è presente o non è un file di testo.

    b. **elimina_riga**:    (fileDiTest, indexRiga) => elimina riga[indexRiga] e return esito operazione.
    solleva eccezione remota se il file non siste o non è un file di testo o se ha meno righe della riga richiesta.


#### Progetto RMI
- interfaccia remota: definisce i metodi invocabili dal Client
- classe di appoggio per la struttura dati (classe)
- classe di implementazione Server
- classe Client

**RMI Registry**:
> rmiregistry
> Sintassi: ServerImpl [registryPort]
> Client RegistryHost [registryPort]

Il **Registry** deve essere in esecuzione su un host concordato, noto a tutti i clienti, e in ascolto sulla porta eventualmente specificata.
Il Server deve registrare il riferimento remoto sul registry alla locazione corretta.
Il Client deve recuparare dal registry il riferimento all'oggetto remoto `ServerImpl` di cui invoca i metodi.


6. ### Java RMI e Riferimenti Remoti
    #### Svolta
    Servizio di nomi RegistryRemoto (**Server RMI**): che permetta
    - ai Servitori di registrare la propria disponibilità di servizio; tenendo traccia di (nome del servizio, locazione di deployment)
        a. (nome logico, riferimento) => aggiunta di server remoto
        b. (nome logico) => eliminazione 1a entry
        c. (nome logico) => eliminazione tutte le entry
        d. () => lista di coppie nome logico / riferimento.

    - ai Clienti di ottenere i riferimenti remoti necessari per il servizio che richiedono.
        a. (nome logico) => return primo riferimento a server remoto
        b. (nome logico) => return tutti i riferimenti ai server remoti con stesso nome.

    I Clienti potranno poi richiedere metodi remoti ai servitori.

    #### Proposta
    Espansione del RegistryRemoto: associazione di Tag per descrivere il servizio.
    - Servitori: funzionalià per aggiungere i tag.
    - Clienti: ricerca tramite tag.  


7. ### Remote Procedure Call (RPC)
    The RPC system relies on a service called portmapper (or rpcbind) to assign ports for RPC services.
    To install it:
    > sudo apt-get update && sudo apt-get upgrade libtirpc

    > sudo apt-get install rpcbind

    To start it:The RPC system relies on a service called portmapper (or rpcbind) to assign ports for RPC services.
    To install it:
    > sudo apt-get update && sudo apt-get upgrade libtirpc

    > sudo apt-get install rpcbind

    To start it:
    > sudo service rpcbind start 
    > sudo service rpcbind start 


    #### Svolta
    ##### Es 1: **Operazioni**: effettua somma e prodotto tra interi in remoto.
    - Client:
        1. invocato da cmd con argomenti (operazione, int_1, int_2).
        2. esegue Remote Procedure Call passando dalla struttura dati Operandi.
        3. stampa il risultato dell'operazione (int) ricevuto dalla procedura.
    - Server:
        1. esegue somma o prodotto tra i due parametri e restituisce il risultato.

    Compilazione **file.x** per ottenere file header, file per conversioni xdr e gli stub:
    > rpcgen operazioni.x

    Produce i file:
    - operazioni.h
    - operazioni_xdr.c      [contiene la routine di conversione xdr]
        FILE XDR: Insiemi di funzioni per la trasformazione dei dati dalla rappresentazione local alla standard (XDR) e viceversa.

    - operazioni_cInt.c     [stub del client]
    - operazioni_svc.c      [stube del server]

    Non ci dobbiamo occupare della memoria -> gestita automaticamente dal supporto RPC.
    Si accede ai parametri tramite un puntatore che li racchiude tutti e ne permette l’accesso. Il risultato è una variabile statica.

    Compilazione per l'eseguibile client:
    > gcc -o operazioni operazioni_client.c operazioni_clnt.c operazioni_xdr.c
    -lnsl -ltirpc

    Compilazione per l'eseguibile server:
    > gcc -o operazioni_server operazioni_proc.c operazioni_svc.c operazioni_xdr.c
    --
    > gcc -o operazioni_server operazioni_proc.c operazioni_svc.c operazioni_xdr.c -lnsl -ltirpc
    Esecuzione:
    > operazioni_server
    > operazioni serverhost somma op1 op2

    ##### Es 2: **Echo** di una stringa invocando una procedura remota.
    Le stringhe richiedono azioni sulla memoria da allocare e hanno impatto su quello che la parte applicativa deve fare.

    > gcc echo_client.c echo_clt.c -o remote_echo
    > gcc echo_proc.c echo_svc.c -o echo_server

#### XDR - Definizione dei tipi di dato:

- dati al primo livello: passati direttamente alle funzioni possono essere passati solo per valore e NON complessi.

- dati al secondo livello (definiti all'interno di altre strutture dati): possono usare anche strutture complesse come array e puntatori.

8. ### Inizializzazione strutture dati sul server
    #### Svolta
        Servizio di prenotazioni delle postazioni di una sala lettura, organizzata in file e colonne.
        Tipo di prenotazione {B: bibliotecari, P: progessori, D: dottorandi}.
        **Server** operazioni:
        - prenota_postazione: (tipo {B, P, o D}, fila, colonna) => aggiornamento, return {success: 0, failure: -1}
        - visualizza_stato: () => return struct Sala

        **Client**: interazione con l'utente proponendo ciclicamente i servizi RPC, stampando a video gli esiti.

        La struttura dati Sala va inizializzata prima della partenza del server: stato "L", libero.

    #### Possibilità per inizializzare i dati:
        1. Inserire una procedura locale di inizializzazione nel server che specifica le rpc (nel server) -> se l'inizializzazione non è avvenuta l'invocazione di una qualsiaso procedura remota invocata dal client provoca l'invocazione di tale procedura.
        
        2. Inserire all'interfaccia del server una procedura remota di inizializzazione. Chiamata esplicitamente dal client prima di qualsiasi altra procedura.

        3. Inserire il codice di inizializzazione direttamente all'interno del main (stub del server).

        4. Inserire la procedura di inizializzazione nel server (localmente) che specifica le procedure remote. Tale procedura deve essere invocata all'interno del main èrima dell'invocazione della procedura svc_run(), la quale mette il server in ascolto di nuove richieste. -> Necessita di dichiarare la procedura di inizializzazione come extern all'interno del file dello stub contenente stub e main (server).

    ### Proposta
        VotaFattoreX: votazione in talent show. Competizione tra giudici, a capo di un gruppo di candidati.

        Ogni candidato appartiene a categoria {U: Uomini, D: Donne, O: Over25, B: Band}.
        Ad ogni candidato appartiene una fase: {A: Audizioni, B: BootCamp, S: Show}.

        **Server**:
        Struttura dati: max N candidati; per ogni candidato (chiave della tabella unica):  {giudice, categoria, nome file unico, fase, voti}. "L": valore libero di default.
        
        Operazioni:
        - view classifica dei giudici () => {nomi giudici: voti}.
            N.b: ogni giudice può avere + candidati: somma punteggi.
        - esprimi_voto: ( nome candidato, "aggiunta"/"sottrazione") => voto candidato: +1 / -1.

        **Client**: interazione ciclica a filtro con utente. 
