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

            // Resposta al client:
            pw.println(message);
            pw.flush();

            pw.close();
            os.close();
        } catch (IOException ex) {
            System.out.println("S'ha donat la següent excepció durant la connexió: " + ex.getMessage());
        }
    }

    public String checkFile(String fileName) {
        File file = new File(filesPath + fileName);
        if (file.exists()) {
            System.out.println("El fitxer " + fileName + " existeix al servidor.");
            return "true";
        } else {
            System.out.println("El fitxer " + fileName + " no existeix al servidor.");
            return "false";
        }
    }

    public String listFilesForClient() {
        // Ruta del fitxer/directori compartit:
        File file = new File(filesPath);

        String result = "";
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] archivos = file.listFiles();

                result += "\n-------- LLISTA DE FITXERS: --------\n";
                for (int i = 0; i < archivos.length; i++) {
                    result += "Fitxer " + (i + 1) + " : " + archivos[i].getName();
                    if (i != archivos.length - 1) {
                        result += "\n";
                    }
                }

                System.out.println("Llistant els fitxers...");
            } else {
                result += "El fitxer compartit 'files' no pot ser llistat perqué no és un directori.";
            }
        } else {
            result += "El fitxer compartit 'files' no pot ser llistat perqué no existeix.";
        }

        return result.trim();
    }

    public void downloadFile(String fileName) {
        try {
            FileInputStream fis = new FileInputStream(filesPath + fileName);
            OutputStream os = socket.getOutputStream();

            System.out.println("Descarregant el fitxer " + fileName + "...");
            int bytes, bytesCopied = 0;
            do {
                bytes = fis.read();
                if (bytes != -1) {
                    os.write(bytes);
                    bytesCopied++;
                }

            } while (bytes != -1);
            System.out.println("El fitxer " + fileName + " ha sigut descarregat del servidor correctament. Bytes copiats: " + bytesCopied + ".");

            os.close();
            fis.close();
        } catch (IOException ex) {
            System.out.println("El fitxer " + fileName + " no s'ha pogut descarregar del servidor perqué s'ha donat la següent excepció: " + ex.getMessage());
        }
    }

    public void uploadFile(String fileName) {
        try {
            // Alternativa 1 - Comentar / descomentar la part corresponent al client:
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
            System.out.println("El fitxer " + fileName + " ha sigut pujat al servidor. Bytes copiats: " + bytesCopied + ".");

            fos.close();
            is.close();

            // Alternativa 2 - Comentar / descomentar la part corresponent al client:
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

            System.out.println("El fitxer " + fileName + " ha sigut pujat al servidor.");

            os.close();
            dis.close();*/
        } catch (IOException ex) {
            System.out.println("El fitxer " + fileName + " no s'ha pogut pujar al servidor perqué s'ha donat la següent excepció: " + ex.getMessage());
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
                            System.out.println("S'ha introduït una opció incorrecta.");
                    }
            }
        } catch (IOException ex) {
            System.out.println("S'ha donat la següent excepció durant la connexió: " + ex.getMessage());
        }
    }
}
