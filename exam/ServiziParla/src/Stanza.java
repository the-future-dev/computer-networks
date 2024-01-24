import java.util.Arrays;

public class Stanza {
	public final static int MAX_UTENTI = 5;
	
	private String nome;
	private String utenti [];
	private char stato;
	private boolean s;
	
	public Stanza() {
		super();
		this.nome = "L";
		this.stato = 'L';
		this.s = false;
		this.utenti = new String[Stanza.MAX_UTENTI];
		for (int i = 0; i < MAX_UTENTI; i++)
			utenti [i] = "L";
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String[] getUtenti() {
		return utenti;
	}

	public void setUtenti(String[] utenti) {
		this.utenti = utenti;
	}
	
	public void setUtenteIdx(String utente, int index) {
		this.utenti[index] = utente;
	}
	
	public char getStato() {
		return stato;
	}

	public void setStato(char stato) {
		this.stato = stato;
	}

	public boolean isS() {
		return s;
	}

	public void setS(boolean s) {
		this.s = s;
	}

	@Override
	public String toString() {
		return "Stanza [nome=" + nome + ", utenti=" + Arrays.toString(utenti) + ", stato=" + (s ? (stato+" ") : ('S'+stato+"")) + "]";
	}
}
