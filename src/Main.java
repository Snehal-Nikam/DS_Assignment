//Main Class
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static ServerSocket _serverSocket;
    public static void main(String[] args) {
        if(args.length !=4 || !args[0].equals("-documentRoot") || !args[2].equals("-port")) {
            System.out.println("Please make sure you pass the required arguments in below format\n" +
                    "-documentRoot {root directory} -port {port number}");
            System.exit(-1);
        }
        String rootDirectory = args[1];
        int port = Integer.parseInt(args[3]);

        try{
            _serverSocket = new ServerSocket(port);
            System.out.println("Listening on port "+port+" .....");
            while (true) {
                Socket s = _serverSocket.accept();
                Thread thread = new Thread(new ServerConnectionHandler(s, rootDirectory));
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("Unable to listen on port :"+port);
        }
    }
}