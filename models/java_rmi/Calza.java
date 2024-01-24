/* Ritossa Andrea 0001020070 */

public class Calza {
	private String id;
	private Tipo tipo;
	private Boolean carbone;
	private String città;
	private String via;
	private String messaggio;
	
	public Calza (String id, Tipo type, boolean carbone, String città, String via, String messaggio) throws Exception {
		if (messaggio.length() > 256) {
			throw new Exception("Messaggio out of bounds");
		}
		this.id = id;
		this.tipo = type;
		this.carbone = carbone;
		this.città = città;
		this.via = via;
		this.messaggio = messaggio; 
	}
	
	@Override
	public String toString() {
	    return "Calza{" +
	        "idBambino='" + this.getId() + '\'' +
	        ", tipo='" + this.getTipo() + '\'' +
	        ", carbone='" + this.getCarbone() + '\'' +
	        ", citta='" + this.getCittà() + '\'' +
	        ", via='" + this.getVia() + '\'' +
	        ", messaggio='" + this.getMessaggio()+ '\'' +
	        '}';
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	public Boolean getCarbone() {
		return carbone;
	}

	public void setCarbone(Boolean carbone) {
		this.carbone = carbone;
	}

	public String getCittà() {
		return città;
	}

	public void setCittà(String città) {
		this.città = città;
	}

	public String getVia() {
		return via;
	}

	public void setVia(String via) {
		this.via = via;
	}

	public String getMessaggio() {
		return messaggio;
	}

	public void setMessaggio(String messaggio) throws Exception {
		if (messaggio.length() > 256) {
			throw new Exception("Messaggio out of bounds");
		} else {
			this.messaggio = messaggio;
		}
	}
}