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
        System.out.println("Starting the server...");

        ServerSocket listener;
        int srvPort = 9889;
        try {
            listener = new ServerSocket(srvPort);
        } catch (IOException ex) {
            System.out.println("The port " + srvPort + " is busy or inaccessible. Exception: " + ex);
            return;
        }

        while (true) {
            Socket socket = listener.accept();

            System.out.println("\nConnection received. Taking care of petition...");

            FileServerWorker fileServerWorker = new FileServerWorker(socket);
            Thread threadGenerator = new Thread(fileServerWorker);
            threadGenerator.start();
        }
    }

    public static void main(String[] args) throws IOException {
        FileServer fileServer = new FileServer();
        fileServer.listen();
    }
}
