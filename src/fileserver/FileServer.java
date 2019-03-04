package fileserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Manuel Espinosa Torres
 */
public class FileServer {

    public void listen() throws IOException {
        System.out.println("Iniciant el servidor...");

        ServerSocket listener;
        int srvPort = 9889;
        try {
            listener = new ServerSocket(srvPort);
        } catch (IOException ex) {
            System.out.println("El port " + srvPort + " està ocupat o és inaccessible.");
            return;
        }

        while (true) {
            Socket socket = listener.accept();

            System.out.println("\nS'ha rebut una connexio. Atenent peticio...");

            FileServerWorker fileServerWorker = new FileServerWorker(socket);
            Thread generadorThread = new Thread(fileServerWorker);
            generadorThread.start();
        }
    }

    public static void main(String[] args) throws IOException {
        FileServer fileServer = new FileServer();
        fileServer.listen();
    }
}
