import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMI_Server extends UnicastRemoteObject implements RMI_InterfaceFile{
	private static final long serialVersionUID = 1L;
	
	private RMI_Server() throws RemoteException{ super(); }
	
	
	public static void main(String[] args) {
		final int PORT_REGISTRY = 1099;
		String registryHost = "localhost";
		String serviceName = "FileService2";
		String completeName = "//"+ registryHost + ":" + PORT_REGISTRY + "/" + serviceName;
		
		try {
			/*
			 * RMI registration
			 */
			RMI_Server serverRMI = new RMI_Server();
			Naming.rebind(completeName, serverRMI);
			System.out.println("");
			System.out.println("CompleteName: " + completeName);
		} catch (RemoteException | MalformedURLException ex){
            if (ex instanceof  RemoteException){
                System.out.println("Remote Exception occurred. Exit");
                ex.printStackTrace();
                System.exit(2);
            }
            else if(ex instanceof  MalformedURLException){
                System.out.println("MalformedURLException occurred. Exit");
                System.exit(2);
            }
        } catch(Exception e) {
			System.out.print(e.getMessage());
			System.exit(1);
		}
		
	}


	@Override
	public String[] lista_filetesto(String nomeDir) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int visualizza_numero(String nomeFile) throws RemoteException {
		// TODO Auto-generated method stub
		return -1;
	}
}
