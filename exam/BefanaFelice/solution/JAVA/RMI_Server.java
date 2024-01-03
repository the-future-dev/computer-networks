import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMI_Server extends UnicastRemoteObject implements RMI_Interface{
    private static Calza[] calze;
    private static final int NUM_CALZA = 5;

    /*Constructor*/
    private RMI_Server() throws RemoteException { super(); }

    @Override
    public String[] visualizza_lista() throws RemoteException {

        String[] cities = new String[calze.length];
        String[] resultTmp = new String[calze.length];
        String[] result=null;

        System.out.println("Ricevuta richiesta lista");

        for (int i =0; i<NUM_CALZA; i++) cities[i]=calze[i].getCitta();
        for (int i =0; i<calze.length; i++) resultTmp[i]="Void";

        //Remove duplicate
        resultTmp[0]=cities[0];
        for (int i =0; i<cities.length; i++){
            String tempCity=cities[i];
            boolean duplicate=false;
            for(int j=0; j<resultTmp.length; j++){
                if(tempCity.equals(resultTmp[j])) duplicate=true;
            }
            if (!duplicate) {
                for(int j=0; j<resultTmp.length; j++)
                    if (resultTmp[j].equals("Void")) {
                        resultTmp[j]=tempCity;
                        break;
                    }
            }
        }
        //scale back array before send
        for (int i =0; i<resultTmp.length; i++){
            if (resultTmp[i].equals("Void")){
                result = new String[i];
                break;
            }
        }
        System.out.println("Completata richiesta lista");
        /*creation of 'result' array occurs only if there are fewer cities than the total number of 'calze'
        * If there are the same number of calze and cities, the system doesn't create the array so I can
        * return 'resultTmp' array*/
        if(result!=null)
            for (int i =0; i<result.length; i++)
                result[i]=resultTmp[i];
        else return resultTmp;

        return result;
    }

    @Override
    public int visualizza_numero(String citta, String via) throws RemoteException {
        //network problem
        if(citta==null || via == null)
            throw new RemoteException();

        int result=0;
        System.out.println("Ricevuta richiesta numero calze in via "+via+" di "+citta+".");
        for(Calza temp:calze){
            if(temp.getVia().equals(via))
                if (temp.getCitta().equals(citta))
                    result++;
        }
        System.out.println("Completata richiesta numero calze");
        if(result==0){
            System.out.println("Via o Citta inesistente");
            result = -1;
        }

        return result;
    }

    public static void main(String[] args) {
        final int REGISTRYPORT = 1099;
        String registryHost = "localhost";
        String serviceName = "CalzaServer";
        String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/" + serviceName;

        /*State Initialization*/
        calze=new Calza[NUM_CALZA];
        for (int i =0; i<NUM_CALZA; i++) calze[i]=new Calza();

        /*State Filling*/
        calze[0].setId("MariaBianchi1");
        calze[0].setTipo(Calza.Allergic.Celiaco);
        calze[0].setCarbone(true);
        calze[0].setCitta("Bologna");
        calze[0].setVia("Larga");
        calze[0].setMessaggio("Cattiva Maria!");

        calze[1].setId("MarioRossi1");
        calze[1].setTipo(Calza.Allergic.Normale);
        calze[1].setCarbone(false);
        calze[1].setCitta("Roma");
        calze[1].setVia("Saragozza");
        calze[1].setMessaggio("Bravo Mario!");

        calze[2].setId("PinoBianchi1");
        calze[2].setTipo(Calza.Allergic.Celiaco);
        calze[2].setCarbone(true);
        calze[2].setCitta("Roma");
        calze[2].setVia("Veneto");
        calze[2].setMessaggio("Cattivo Pino!");

        calze[3].setId("PinoBianchi1");
        calze[3].setTipo(Calza.Allergic.Celiaco);
        calze[3].setCarbone(true);
        calze[3].setCitta("Firenze");
        calze[3].setVia("Veneto");
        calze[3].setMessaggio("Cattivo Pino!");

        calze[4].setId("PinoBianchi1");
        calze[4].setTipo(Calza.Allergic.Celiaco);
        calze[4].setCarbone(true);
        calze[4].setCitta("Bologna");
        calze[4].setVia("Larga");
        calze[4].setMessaggio("Cattiva Maria!");

        /*State printing*/
        for (int i =0; i<NUM_CALZA; i++) System.out.println(calze[i].toString());

        try
        {
            /*Registration*/
            RMI_Server serverRMI = new RMI_Server(); Naming.rebind (completeName, serverRMI);
        }
        catch (RemoteException | MalformedURLException ex){
            if (ex instanceof  RemoteException){
                System.out.println("Remote Exception occurred. Exit");
                System.exit(2);
            }
            else if(ex instanceof  MalformedURLException){
                System.out.println("MalformedURLException occurred. Exit");
                System.exit(2);
            }
        }
    }
}
