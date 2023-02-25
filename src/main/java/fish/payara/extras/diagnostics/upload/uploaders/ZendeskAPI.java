package fish.payara.extras.diagnostics.upload.uploaders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.logging.Logger;

import org.glassfish.api.logging.LogLevel;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fish.payara.extras.diagnostics.upload.Uploader;
import fish.payara.extras.diagnostics.util.MultiPartBodyPublisher;

public class ZendeskAPI implements Uploader {
    Logger logger = Logger.getLogger(this.getClass().getName());

    private static final String ZENDESK_UPLOAD_URL = "https://payara.zendesk.com/api/v2/uploads";

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_MULTIPART_BOUNDARY = "multipart/form-data; boundary=";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final int SUCCESS_HTTP_RESPONSE_CODE = 201;

    private HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    private File file;
    private String username;
    private String password;
    private String ticketNumber;

    public ZendeskAPI(File file, String username, String password, String ticketNumber) {
        this.file = file;
        this.username = username;
        this.password = password;
        this.ticketNumber = ticketNumber;
    }

    @Override
    public int upload() throws FileNotFoundException {
        MultiPartBodyPublisher publisher = new MultiPartBodyPublisher()
            .addPart("File", file.toPath());

        URI uri = null;
        try {
            uri = new URL(ZENDESK_UPLOAD_URL + "?filename=" + file.getName()).toURI();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .header(CONTENT_TYPE, CONTENT_TYPE_MULTIPART_BOUNDARY + publisher.getBoundary())
            .header(AUTHORIZATION_HEADER, getAuthString(username, password))
            .timeout(Duration.ofMinutes(2))
            .POST(publisher.build())
            .build();

        logger.log(LogLevel.INFO, "Starting upload to {0}", uri.toString());
        
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            logger.log(LogLevel.INFO, "Server Responded with {0}", responseCode);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            ZendeskResponse jsonResponse = objectMapper.readValue(response.body(), ZendeskResponse.class);

            System.out.println(jsonResponse.upload.token);

            if(responseCode != SUCCESS_HTTP_RESPONSE_CODE) {
                return 1;
            }
        
        } catch (IOException e) {
            logger.log(LogLevel.SEVERE, "IOException occured trying to send to {0}", ZENDESK_UPLOAD_URL);
            e.printStackTrace();
        } catch (InterruptedException e) {
            logger.log(LogLevel.SEVERE, "InterruptedException occured trying to send to {0}", ZENDESK_UPLOAD_URL);
            e.printStackTrace();
        }

        return 0;
    }

    private String getAuthString(String username, String password) {
        String authString = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
    }

    private static class ZendeskResponse {
        public Upload upload;

        public static class Upload {
            public String token;
        }
    }
    
}
