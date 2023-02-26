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
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.glassfish.api.logging.LogLevel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fish.payara.extras.diagnostics.upload.Uploader;

public class ZendeskAPI implements Uploader {
    Logger logger = Logger.getLogger(this.getClass().getName());

    private static final String ZENDESK_UPLOAD_URL = "https://payara.zendesk.com/api/v2/uploads";
    private static final int ZENDESK_UPLOAD_SUCCESS = 201;

    private static final String ZENDESK_ATTACH_URL = "https://payara.zendesk.com/api/v2/requests";
    private static final int ZENDESK_ATTACH_SUCCESS = 200;

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String COMMENT = "Attachment uploaded using the diagnostics tool.";
    private static final String NON_SUCCESS_MESSAGE = "was not successful. Upload will not continue.";

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

    
    /** 
     * Uploads and Attaches file to a ticket comment.
     * 
     * 0 - Success
     * 1 - Failure
     * 
     * @return int
     * @throws FileNotFoundException
     */
    @Override
    public int upload() throws FileNotFoundException {
        Map<String, String> headers = new HashMap<>();
        String authString = getAuthString(username, password);
        ObjectMapper objectMapper = new ObjectMapper();
        String token = "";
        
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String uploadUrl = ZENDESK_UPLOAD_URL + "?filename=" + file.getName();
        headers.put(AUTHORIZATION_HEADER, authString);
        headers.put(CONTENT_TYPE, "application/zip");

        HttpRequest uploadRequest = prepareRequest(uploadUrl, headers, "POST", BodyPublishers.ofFile(file.toPath()));
        if(uploadRequest == null) {
            logger.log(LogLevel.SEVERE, "Upload request " + NON_SUCCESS_MESSAGE);
            return 1;
        }

        logger.log(LogLevel.INFO, "Starting upload to {0}", uploadUrl);
        
        HttpResponse<String> uploadResponse = processRequest(uploadRequest, ZENDESK_UPLOAD_SUCCESS);
        if(uploadResponse == null) {
            logger.log(LogLevel.SEVERE, "Upload response " + NON_SUCCESS_MESSAGE);
            return 1;
        }

        try {
            token = objectMapper.readValue(uploadResponse.body(), ZendeskResponse.class).getToken();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        String attachUrl = ZENDESK_ATTACH_URL + "/" + ticketNumber;        
        headers.replace(CONTENT_TYPE, "application/json");

        String jsonPayloadString = "";
        try {
            ZendeskRequest jsonPayloadObject = new ZendeskRequest();
            jsonPayloadObject.setComment(COMMENT);
            jsonPayloadObject.addUpload(token);
            
            jsonPayloadString = objectMapper.writeValueAsString(jsonPayloadObject);
        } catch (JsonProcessingException e) {
            logger.log(LogLevel.SEVERE, "Could not prepare JSON Payload for attach request. Upload will not continue");
            e.printStackTrace();
            return 1;
        }

        HttpRequest attachRequest = prepareRequest(attachUrl, headers, "PUT", BodyPublishers.ofString(jsonPayloadString));
        if(attachRequest == null) {
            logger.log(LogLevel.SEVERE, "Attach request " + NON_SUCCESS_MESSAGE);
            return 1;
        }

        logger.log(LogLevel.INFO, "Attaching file to ticket: {0}", ticketNumber);

        HttpResponse<String> attachResponse = processRequest(attachRequest, ZENDESK_ATTACH_SUCCESS);
        if(attachResponse == null) {
            logger.log(LogLevel.SEVERE, "Attach response " + NON_SUCCESS_MESSAGE);
            return 1;
        }

        return 0;
    }

    
    /** 
     * Returns a BASIC authentication string from username and password.
     * 
     * @param username
     * @param password
     * @return String
     */
    private String getAuthString(String username, String password) {
        String authString = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
    }

    /** 
     * Prepares a HTTPRequest using specified attributes. Handles exceptions.
     * 
     * Only supports POST and PUT.
     * 
     * Returns null if request could not be built.
     * 
     * @param url
     * @param headers
     * @param httpMethod
     * @param publisher
     * @return HttpRequest
     */
    private HttpRequest prepareRequest(String url, Map<String, String> headers, String httpMethod, BodyPublisher publisher) {
        if(url == null || headers == null || httpMethod == null || publisher == null) {
            return null;
        }

        httpMethod = httpMethod.trim().toUpperCase();

        Builder httpRequestBuilder = HttpRequest.newBuilder()
            .timeout(Duration.ofMinutes(2));

        for(String key : headers.keySet()) {
            String value = headers.get(key);
            if(value != null) {
                httpRequestBuilder.setHeader(key, value);
            }
        }

        try {
            URI uri = new URL(url).toURI();
            httpRequestBuilder.uri(uri);
        } catch (MalformedURLException | URISyntaxException e) {
            logger.log(LogLevel.SEVERE, "URI was not valid and cannot be used: {0}", url);
            e.printStackTrace();
            return null;
        }

        switch(httpMethod){
            case "PUT":
                httpRequestBuilder.PUT(publisher);
                break;
            case "POST":
                httpRequestBuilder.POST(publisher);
                break;
            default:
                logger.log(LogLevel.SEVERE, "HTTP Method: {0} is not configured to be used with this service.", httpMethod);
                return null;
        }

        return httpRequestBuilder.build();
    }

    
    /** 
     * Sends the HTTP Request and handles the response. Returns null if successCode is not the one expected.
     * 
     * @param request
     * @param successCode
     * @return HttpResponse<String>
     */
    private HttpResponse<String> processRequest(HttpRequest request, int successCode) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int httpStatus = response.statusCode();

            logger.log(LogLevel.INFO, "Server Responded with {0}", httpStatus);
            if(httpStatus != successCode) {
                logger.log(LogLevel.SEVERE, "Upload did not succeed with HTTP code: {0}", successCode);
                return null;
            }

            return response;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static class ZendeskResponse {
        public Upload upload;

        public String getToken() {
            if(upload != null) {
                return upload.token;
            }
            return "";
        }

        public static class Upload {
            public String token;
        }
    }

    private static class ZendeskRequest {
        public Request request;

        public ZendeskRequest() {
            this.request = new Request();
        }

        public void addUpload(String value) {
            if(request != null) {
                request.comment.uploads.add(value);
            }
        }

        public void setComment(String value) {
            if(request != null) {
                request.comment.body = value;
            }
        }

        public static class Request {
            public Comment comment;

            public Request() {
                this.comment = new Comment();
            }

            public static class Comment {
                public String body;
                public List<String> uploads;

                public Comment() {
                    this.uploads = new ArrayList<>();
                }
            }
        }

    }
    
}
