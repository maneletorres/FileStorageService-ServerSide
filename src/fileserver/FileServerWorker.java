package fileserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Manuel Espinosa Torres
 */
public class FileServerWorker implements Runnable {

    private final Socket socket;
    private final String filesPath = "./files/";

    FileServerWorker(Socket socket) {
        this.socket = socket;
    }

    public void writeMsgIntoSocket(String message) {
        try {
            OutputStream os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os);

            // Response to the client:
            pw.println(message);
            pw.flush();

            pw.close();
            os.close();
        } catch (IOException ex) {
            System.out.println("The following exception occurred while connecting: " + ex.getMessage());
        }
    }

    public String checkFile(String fileName) {
        File file = new File(filesPath + fileName);
        if (file.exists()) {
            System.out.println("The file " + fileName + " exists on server.");
            return "true";
        } else {
            System.out.println("The file " + fileName + " does not exist on server.");
            return "false";
        }
    }

    public String listFilesForClient() {
        // Path of the shared directory:
        File file = new File(filesPath);

        String result = "";
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();

                result += "\n-------- FILE LIST: --------\n";
                for (int i = 0; i < files.length; i++) {
                    result += "File " + (i + 1) + " : " + files[i].getName();
                    if (i != files.length - 1) {
                        result += "\n";
                    }
                }

                System.out.println("Listing the files...");
            } else {
                result += "The file 'files' can not be listed because it is not a directory.";
            }
        } else {
            result += "The directory 'files' can not be listed because it does not exist.";
        }

        return result.trim();
    }

    public void downloadFile(String fileName) {
        try {
            FileInputStream fis = new FileInputStream(filesPath + fileName);
            OutputStream os = socket.getOutputStream();

            System.out.println("Downloading the file " + fileName + "...");
            int bytes, bytesCopied = 0;
            do {
                bytes = fis.read();
                if (bytes != -1) {
                    os.write(bytes);
                    bytesCopied++;
                }

            } while (bytes != -1);
            System.out.println("The file " + fileName + "  has been downloaded successfully from the server. Copied bytes: " + bytesCopied + ".");

            os.close();
            fis.close();
        } catch (IOException ex) {
            System.out.println("The file " + fileName + " could not be downloaded from the server because the following exception was given: " + ex.getMessage());
        }
    }

    public void uploadFile(String fileName) {
        try {
            // Alternative 1 - Comment / uncomment the part corresponding to the client:
            InputStream is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream(filesPath + fileName);

            int bytes, bytesCopied = 0;
            do {
                bytes = is.read();
                if (bytes != -1) {
                    fos.write(bytes);
                    bytesCopied++;
                }
            } while (bytes != -1);
            System.out.println("The file " + fileName + " has been uploaded to the server. Copied bytes: " + bytesCopied + ".");

            fos.close();
            is.close();

            // Alternative 2 - Comment / uncomment the part corresponding to the client:
            /*int bytesRead;

            DataInputStream dis = new DataInputStream(socket.getInputStream());
            OutputStream os = new FileOutputStream((filesPath + fileName));

            fileName = dis.readUTF();
            long size = dis.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                os.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }

            System.out.println("The file " + fileName + "  has been uploaded to the server.");

            os.close();
            dis.close();*/
        } catch (IOException ex) {
            System.out.println("The file " + fileName + " could not be uploaded to the server because the following exception was given: " + ex.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String message = br.readLine();
            System.out.println("Message: " + message);

            switch (message) {
                case "list":
                    writeMsgIntoSocket(listFilesForClient());
                    break;
                case "GET connection status":
                    writeMsgIntoSocket("true");
                    break;
                default:
                    String[] messageSplitted = message.split(" ");
                    switch (messageSplitted[0]) {
                        case "check":
                            writeMsgIntoSocket(checkFile(messageSplitted[1]));
                            break;
                        case "put":
                            uploadFile(messageSplitted[1]);
                            break;
                        case "get":
                            downloadFile(messageSplitted[1]);
                            break;
                        default:
                            System.out.println("An incorrect option was entered.");
                    }
            }
        } catch (IOException ex) {
            System.out.println("The following exception occurred while connecting: " + ex.getMessage());
        }
    }
}
