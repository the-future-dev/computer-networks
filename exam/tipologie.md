Da preparare
### C
- [x] lettura | scrittura file in C
- [ ] redirezione output su un file | verso il client
- [x] socket con connessione: BefanaFelice
- [x] socket senza connessione: BefanaFelice
- [x] server multiservizio: primitiva select: BefanaFelice

### JAVA
- [x] socket con connessione
- [x] socket senza connessione
- [x] java RMI




# C
## Funzioni I/O:
- ### open()
Apre il file specificato e restituisce il suo file descriptor (fd); Crea una nuova entry nella tabella dei file aperti di sistema (nuovo I/O pointer). Fd è l'indice dell'elemento che rappresenta il file aperto nella tabella dei files aperti del processo (contenuta nella user structure del processo).
Possibili diversi flag di apertura, combinabili con OR bit a bit (operatore |).

- ### close()
chiude il file aperto.
libera il file descriptor nella tabella dei file descriptors aperti del processo.

- ### read()
read(fd, buff, n) legge al più n bytes a partire dalla posizione dell'I/O pointer e li memorizza in buff.
Restituisce il numero di byte effettivamente letti:
- >0: OK
- 0 per end-of-file
- -1 in caso di errore
- (perror e errno per sapere quale)

> ssize_t read(int fd, void *buf, size_t count);
`read() attempts to read up to count bytes from file descriptor fd into the buffer starting at buf.`

- ### write()
write(fd, buff, n) scrive al più n bytes dal buffer buff nel file a partire dalla posizione dell'I/O pointer.
Restituisce il numero di byte effettivamente scritti o -1 in caso di errore.

- ### lseek()
seek(fd, offset, origine) sposta l’I/O pointer di offset posizioni rispetto all’origine. 
Possibili valori per origine:
- 0 per inizio del file (SEEK_SET)
- 1 per posizione corrente (SEEK_CUR)
- 2 per fine del file (SEEK_END)