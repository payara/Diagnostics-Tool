package fish.payara.extras.diagnostics.upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HTTPFileUpload {
    private final static String BOUNDARY = "===" + Long.toString(System.currentTimeMillis()) + "===";
    private final static String LINE_SEP = System.lineSeparator();
    private final static String CHAR_SET = "UTF-8";


    public HttpURLConnection configureConnection(HttpURLConnection theConnection) {
        if(theConnection == null) {
            return theConnection;
        }

        theConnection.setUseCaches(false);
        theConnection.setDoOutput(true);
        theConnection.setDoInput(true);
        theConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        theConnection.setRequestProperty("User-Agent", "Payara");

        return theConnection;
    }
    
    public void addField(PrintWriter writer, String fieldName, String fieldValue) {
        
        writer.append(BOUNDARY).append(LINE_SEP);
        writer.append("Content-Disposition: form-data; name\"" + fieldName + "\"").append(LINE_SEP);
        writer.append("Content-Type: text/plain; charset=" + CHAR_SET).append(LINE_SEP);
        writer.append(LINE_SEP);
        writer.append(fieldValue).append(LINE_SEP);
        writer.flush();
    }

    public void addFilePart(PrintWriter writer, OutputStream outputStream, String fieldName, File uploadFile) throws IOException {
        String fileName = uploadFile.getName();

        writer.append(BOUNDARY).append(LINE_SEP);
        writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE_SEP);
        writer.append("Content-Type: multipart/form-data").append(LINE_SEP);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();
         
        writer.append(LINE_SEP);
        writer.flush();

    }

    public void addHeaderField(PrintWriter writer, String key, String value) {
        writer.append(key + ": " + value).append(LINE_SEP);
        writer.flush();
    }

    public List<String> upload(PrintWriter writer, HttpURLConnection connection) throws IOException {
        List<String> response = new ArrayList<String>();

        writer.append(LINE_SEP);
        writer.append(BOUNDARY).append(LINE_SEP);
        writer.close();

        int status = connection.getResponseCode();
        System.out.println(status);
        if(status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            while((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();
            connection.disconnect();
        } 

        return response;
    }

}
