import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class RMI_Client {
    public static void main(String[] args) {
        final int REGISTRYPORT = 1099;
        String registryHost;
        String serviceName = "CalzaServer";

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        /*CHECK PARAM*/
        if (args.length != 1) {
            System.out.println("Usage: ClientName <Host> ");
            System.exit(1);
        }
        registryHost = args[0];
        String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/" + serviceName;

        try {
            // REMOTE RMI CONN
            RMI_Interface serverRMI = (RMI_Interface) Naming.lookup(completeName);
            System.out.println("\nRichieste fino a EOF...");
            System.out.print("Servizio (L=Lista, N=Numero): ");
            String service;

            //USER INTERACTION
            try {
                while ((service = stdIn.readLine()) != null) {
                    if (service.equals("L")){
                        for(String tempCity : serverRMI.visualizza_lista())
                            if (!tempCity.equals("Void") && !tempCity.equals("L"))
                                System.out.println(tempCity);

                        System.out.print("Servizio (L=Lista, N=Numero): ");
                    }
                    else if (service.equals("N")){
                        int result;
                        System.out.print("Inserire via: ");
                        String via = stdIn.readLine();

                        System.out.print("Inserire citta: ");
                        String citta = stdIn.readLine();

                        result=serverRMI.visualizza_numero(citta,via);
                        if(result!=-1)
                            System.out.println("Numero di calze da consegnare in via "+via+" di "
                                    +citta +" = "+result);
                        else
                            System.out.print("");
                        System.out.print("Servizio (L=Lista, N=Numero): ");

                    }
                    else System.out.println("Operation not permitted.");

                    System.out.print("Servizio (L=Lista, N=Numero): ");
                }
            }
            catch(IOException ex){
                System.out.println("IOException occurred. Exit");
                System.exit(2);
            }

        }
        catch (NotBoundException | MalformedURLException | RemoteException ex){
            if(ex instanceof  MalformedURLException){
                System.out.println("MalformedURLException occurred. Exit");
                System.exit(2);
            }
            else if (ex instanceof  RemoteException){
                System.out.println("Remote Exception occurred. Exit");
                System.exit(2);
            }
        }
    }
}
