package fish.payara.extras.diagnostics.upload;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPFileUpload {
    private static final String boundary = Long.toString(System.currentTimeMillis());
    
    private URL url;
    private File uploadFile;
    private HttpURLConnection connection;

    private final static String LINE_SEP = System.lineSeparator();
    private final static String CHAR_SET = "UTF-8";
    
    public HTTPFileUpload(URL url, File uploadFile) throws IOException {
        this.url = url;
        this.uploadFile = uploadFile;

        this.connection = (HttpURLConnection) url.openConnection();
    }

    private HttpURLConnection configureConnection(HttpURLConnection theConnection) {
        if(connection == null) {
            return theConnection;
        }

        theConnection.setUseCaches(false);
        theConnection.setDoOutput(true);
        theConnection.setDoInput(true);
        theConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        theConnection.setRequestProperty("User-Agent", "Payara");

        return theConnection;
    }
    
    // TODO Should make this public - add fields depending on needs from the destination being uploaded too. Since e.g. Nexus api upload is a form data field upload.
    private void addField(PrintWriter writer, String key, String value) {
        
        writer.append(boundary).append(LINE_SEP);
        writer.append("Content-Disposition: form-data; name\"" + key + "\"").append(LINE_SEP);
        writer.append("Content-Type: text/plain; charset=" + CHAR_SET).append(LINE_SEP);
        writer.append(LINE_SEP);
        writer.append(value).append(LINE_SEP);
        writer.flush();

    }

}
