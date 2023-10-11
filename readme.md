## Computer Networks - Reti di calcolatori T
Questa repository continene le esercitazioni del corso di "Reti di calcolatori T" appartenente al corso di Laure Triennale in Ingegneria Informatica dell'Alma Mater Studiorum di Bologna.

Autori:
 - Nicola Leonetti @
 - Andrea Ritossa @the-future-dev
 - Alessandro Quaranta @
 - Gaia Verzelli @

Consigli:
 - in eclipse creare un workspace contentente l'intera cartella

0. ### Lettura e Scrittura di File in Java e C
Server - Produttore: crea il file; chiede all'utente quante righe vuole scrivere; scrive il contenuto digitato dall'utente **riga per riga**.
Client - Consumatore (filtro): apre il file passato come argomento; legge il contenuto fino a EOF.

1. ### Socket Java senza connessione
Client (filtro): invia al server pacchetti contententi "nome di file di testo" e "numero della linea del file.
    unique request as it is without connection
Server: receives the request from the client; verifies the file and the line existance; returns a response to the client: error or line content.
    sequential (mono-process) that executes an endless loop
 
