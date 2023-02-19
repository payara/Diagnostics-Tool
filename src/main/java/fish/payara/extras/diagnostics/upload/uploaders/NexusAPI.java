package fish.payara.extras.diagnostics.upload.uploaders;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.api.admin.ParameterMap;

import fish.payara.extras.diagnostics.upload.HTTPFileUpload;
import fish.payara.extras.diagnostics.upload.Uploader;

public class NexusAPI implements Uploader {

    private final static String BOUNDARY = "===" + Long.toString(System.currentTimeMillis()) + "===";
    private final static String LINE_SEP = System.lineSeparator();

    private static final String NEXUS_URL = "http://localhost:8081/service/rest/v1/components";

    private File file;
    private String username;
    private String password;

    /*curl -v -u admin:admin123 
    -F "maven2.generate-pom=false" 
    -F "maven2.asset1=@/absolute/path/to/the/local/file/pom.xml" 
    -F "maven2.asset1.extension=pom" 
    -F "maven2.asset2=@/absolute/path/to/the/local/file/product-1.0.0.jar;type=application/java-archive" 
    -F "maven2.asset2.extension=jar" "http://localhost:8081/service/rest/v1/components?repository=maven-releases"*/

    /*
     * POST parameters, you need to have r=releases, hasPom=true (or false if you're uploading a POM with it), 
     * e for the extension of the artifact, 
     * g, a, v and p for the coordinates (groupId, artifactId, version and packaging) and 
     * finally file for the file to deploy.
     */


     //hasPom=false -F e=jar -F g=com.test -F a=project -F v=1.0 -F p=jar -F file=@project-1.0.jar

    public NexusAPI(File file, String username, String password) {
        this.file = file;
        this.username = username;
        //this.password = password;
        this.password = "admin";
    }

    @Override
    public int upload() {
        // Map<String, Object> payload;
        
        // if(Files.exists(Path.of(file.getPath()))) {
        //     payload = preparePayload();
        // } else {
        //     throw new FileNotFoundException("File could not be found at given path");
        // }

        // try {
        //     byte[] encodedPayload = endcodePayload(payload, "UTF-8");
            
        //     HttpURLConnection connection = (HttpURLConnection) new URL(NEXUS_URL + "?repository=" + username).openConnection();
        //     connection.setRequestMethod("POST");
        //     connection.setDoOutput(true);
        //     connection.setDoInput(true);
        //     connection.setRequestProperty("Authorization", getAuthString(username, password));

        //     connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY + LINE_SEP);
        //     connection.setRequestProperty("Content-Length", String.valueOf(encodedPayload.length));

        //     DataOutputStream request = new DataOutputStream(connection.getOutputStream());


        //     request.write(encodedPayload);

        // } catch (UnsupportedEncodingException e) {
        //     e.printStackTrace();
        // } catch (MalformedURLException e) {
        //     e.printStackTrace();
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
            
            /// /home/alanroth/Projects/DC3010/Output.zip
        // try {
        //     URL url = new URL(NEXUS_URL + "?repository=test-customer");
        //     HTTPFileUpload httpFileUpload = new HTTPFileUpload();

        //     HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //     connection = httpFileUpload.configureConnection(connection);
            
        //     //connection.addRequestProperty("repository", "test-customer");

        //     PrintWriter writer = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"), true);

        //     httpFileUpload.addField(writer, "maven2.asset1", file.getAbsolutePath());
        //     httpFileUpload.addField(writer, "maven2.asset1.extension", "zip");
        //     httpFileUpload.addField(writer, "maven2.groupId", "fish.payara.extras");
        //     httpFileUpload.addField(writer, "maven2.artifactId", "diagnostics-tool");
        //     httpFileUpload.addField(writer, "maven2.version", "0.1");
        //     httpFileUpload.addField(writer, "maven2.packaging", "zip");
        //     httpFileUpload.addField(writer, "maven2.generate-pom", "true");

        //     //httpFileUpload.addHeaderField(writer, "Authorization", getAuthString(username, password));

        //     //httpFileUpload.addFilePart(writer, connection.getOutputStream(), "fileUpload", file);

        //     List<String> reponse = httpFileUpload.upload(writer, connection);

        //     connection.disconnect();

        // } catch (MalformedURLException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // } catch (IOException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }

            URL url = null;
            HttpURLConnection connection = null;
            
            try { // Change
                url = new URL("http://localhost:8081/repository/test-customer/fish/payara/extras/diagnostics/0.1/diagnostics-0.1.zip");
                connection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if(url == null || connection == null) {
                return 1;
            }

            try {
                connection.setRequestMethod("POST");
            } catch (ProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);

            //connection.setRequestProperty("Authorization", getAuthString(username, password));
            connection.setRequestProperty("Content-Type", "application/zip");

            try {
                InputStream inputStream = new FileInputStream(file);
                OutputStream outputStream = connection.getOutputStream();

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                inputStream.close();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    System.out.println("Upload successful.");
                } else {
                    System.out.println("Upload failed. Response code: " + responseCode);
                }

                System.out.println(responseCode);

                connection.disconnect();
            } catch(IOException ie) {
                ie.printStackTrace();
            }

        return 0;
    }

    // private Map<String, Object> preparePayload() {
    //     Map<String, Object> payloadParams = new LinkedHashMap<>();
    //     payloadParams.put("e", "zip");
    //     payloadParams.put("g", "fish.payara.extras");
    //     payloadParams.put("a", "diagnostics");

    //     String version = getClass().getPackage().getImplementationVersion();
    //     if(version != null) {
    //         payloadParams.put("v", version);
    //     } else {
    //         payloadParams.put("v", BOUNDARY);
    //     }

    //     if(file != null) {
    //         payloadParams.put("file", this.file.getAbsolutePath());
    //     }
        
    //     return payloadParams;
    // }

    // private byte[] endcodePayload(Map<String, Object> payload, String encoding) throws UnsupportedEncodingException {
    //     StringBuilder stringBuilder = new StringBuilder();

    //     for(Map.Entry<String,Object> entry : payload.entrySet()) {
    //         if(stringBuilder.length() != 0) {
    //             stringBuilder.append(BOUNDARY);
    //         }

    //         stringBuilder.append(URLEncoder.encode(entry.getKey(), encoding));
    //         stringBuilder.append('=');
    //         stringBuilder.append(URLEncoder.encode(String.valueOf(entry.getValue()), encoding));
    //     }
        
    //     byte[] byteEncodedPayload = stringBuilder.toString().getBytes(encoding);
        
    //     return byteEncodedPayload;
    // }

    private String getAuthString(String username, String password) {
        String authString = username + ":" + password;
        return "Basic" + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
    }
    
}
