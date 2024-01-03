import java.net.InetAddress;

public class ServerInfo 
{
    InetAddress IP;
    int porta;
    String file;

    public ServerInfo(InetAddress IP, int porta, String file) 
    {
        this.IP = IP;
        this.porta = porta;
        this.file = file;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ServerInfo other = (ServerInfo) obj;

        // Doppia casistica: stesso endpoint o stesso nome file.
        return (this.IP.equals(other.IP) && this.porta == other.porta) || this.file.equals(other.file);
    }

    @Override
    public String toString()
    {
        return file;
    }
}
