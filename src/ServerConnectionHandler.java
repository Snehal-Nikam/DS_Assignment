import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class ServerConnectionHandler extends Thread{
    private static final String _indexFile = "index.html";
    
    private final Socket _socket;
    private final String _documentRoot;
    public ServerConnectionHandler(Socket socket, String documentRoot){
        this._socket = socket;
        this._documentRoot=documentRoot;
    }

    public void run() {
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            PrintStream out = new PrintStream(new BufferedOutputStream(_socket.getOutputStream()));

            String line = in.readLine();
            //Bad Request exception
            if(!(line.startsWith("GET") || line.startsWith ("HEAD")) && (line.endsWith("HTTP/1.1") || line.endsWith("HTTP/1.1"))) {
                out.println("HTTP/1.1 400 Bad Request\r\n" +
                        "Content-type: text/html\r\n\r\n" +
                        "<html><h1> Malformed Request</h1><br /><br /><br /><br /></html>\n");
                out.close();
                _socket.close();
                return;
            }

            String fileName = "";
            StringTokenizer tokenizer = new StringTokenizer(line);
            try {

                // Parse the filename from the GET command
                if (tokenizer.hasMoreElements() && tokenizer.nextToken().equalsIgnoreCase("GET")
                        && tokenizer.hasMoreElements())
                    fileName = tokenizer.nextToken();
                else {
                    throw new FileNotFoundException();
                }
                // Handling "/"
                if (fileName.endsWith("/"))
                    fileName += _indexFile;

                while (fileName.indexOf("/") == 0)
                    fileName = fileName.substring(1);

                // If a directory is requested and the trailing / is missing,
                // send the client an HTTP request to append it. (necessary for relative links to work correctly in the client).
                if (new File(fileName).isDirectory()) {
                    fileName = fileName.replace('\\', '/');
                    out.print("HTTP/1.1 301 Moved Permanently\r\n" +
                            "Location: /" + fileName + "/\r\n\r\n");
                    out.close();
                    _socket.close();
                    return;
                }

                // Convert relative file path to absolute file path
                fileName = _documentRoot + "/" + fileName;

                System.out.println("Current Thread ID: " + Thread.currentThread().getId() + " | " + line);

                InputStream fileStream = new FileInputStream(fileName);

                String contentType = "text/plain";
                if (fileName.endsWith(".html"))
                    contentType = "text/html";
                else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
                    contentType = "image/jpeg";
                else if (fileName.endsWith(".gif"))
                    contentType = "image/gif";
                else if (fileName.endsWith(".css"))
                    contentType = "text/css";
                out.print("HTTP/1.1 200 OK\r\n" +
                        "Content-type: " + contentType + "\r\nContent-Length: "+ new File(fileName).length() + "\r\n" +
                        "Date: " + getServerTime() + "\r\n\r\n");

                // Create byte array of length equal to the file size and read file into it
                byte[] fileBytes = new byte[(int) new File(fileName).length()];
                int n;
                while ((n = fileStream.read(fileBytes)) > 0)
                    out.write(fileBytes, 0, n);
                out.close();
                _socket.close();
            } catch (FileNotFoundException ex) {
                if (!ex.getMessage().contains("Permission denied")) {
                    out.println("HTTP/1.1 404 Not Found\r\n" +
                            "Content-type: text/html\r\n\r\n" +
                            "<html><h1>" + fileName + " not found</h1><h4>Server Message: " + ex + "</h4><br /><br /><br /><br /></html>\n");
                    out.close();
                    _socket.close();
                } else {
                    out.println("HTTP/1.1 403 Permission Denied\r\n" +
                            "Content-type: text/html\r\n\r\n" +
                            "<html><h1>" + fileName + " Not Allowed</h1><h4>Server Message: " + ex + "</h4><br /><br /><br /><br /></html>\n");
                    out.close();
                    _socket.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }
}

