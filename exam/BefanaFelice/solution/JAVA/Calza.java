public class Calza {
    private String id;
    private String citta;
    private String via;
    private String messaggio;
    private Boolean carbone;
    private Allergic tipo;
    public enum Allergic { Normale, Celiaco, L };

    public Calza (){
        this.id="L";
        this.citta="L";
        this.via="L";
        this.messaggio="L";

        this.carbone=null;
        this.tipo=Allergic.L;
    }

    public String toString() {
        return "Calza ID = "+this.getId() + "; tipo = "+this.getTipo()+"; carbone = "+this.getCarbone()
                + "; città = "+this.getCitta()+"; via = "+this.getVia()+"; messaggio = "+this.getMessaggio();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        this.citta = citta;
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

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    public Boolean getCarbone() {
        return carbone;
    }

    public void setCarbone(Boolean carbone) {
        this.carbone = carbone;
    }

    public Allergic getTipo() {
        return tipo;
    }

    public void setTipo(Allergic tipo) {
        this.tipo = tipo;
    }


    /*TEST MAIN*/
    /*
    public static void main(String [] args)
    {
        System.out.println("START TEST...");
        Calza test=new Calza();
        System.out.println(test.toString());
        System.out.println("...END TEST. BYE");
        System.exit(0);
    }
    */
}
