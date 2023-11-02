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

    public boolean equals(ServerInfo serverInfo)
    {
        return this.IP.getHostAddress()
                .equalsIgnoreCase(serverInfo.IP.getHostAddress())
            && this.file.equalsIgnoreCase(serverInfo.file)
            && this.porta == serverInfo.porta;
    }

    @Override
    public String toString()
    {
        return file;
    }
}
