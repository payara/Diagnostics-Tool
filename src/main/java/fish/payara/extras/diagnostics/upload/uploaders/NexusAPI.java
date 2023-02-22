package fish.payara.extras.diagnostics.upload.uploaders;

import java.io.File;
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

import fish.payara.extras.diagnostics.upload.Uploader;
import fish.payara.extras.diagnostics.util.Maven2Body;
import fish.payara.extras.diagnostics.util.MultiPartBodyPublisher;

public class NexusAPI implements Uploader {
    Logger logger = Logger.getLogger(this.getClass().getName());

    private static final String NEXUS_URL = "https://nexus.payara.fish/service/rest/v1/components";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_MULTIPART_BOUNDARY = "multipart/form-data; boundary=";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String REPO_SYS_PROP = System.getProperty("fish.payara.diagnostics.repo");

    private static final int SUCCESS_HTTP_RESPONSE_CODE = 204;

    private File file;
    private String username;
    private String password;

    private HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    /**
     * Will upload specified file to Payara Nexus, using specified username and password for authentication.
     * The repository that it will be uploaded to depends on {@value #REPO_SYS_PROP}
     * 
     * @param file
     * @param username
     * @param password
     */
    public NexusAPI(File file, String username, String password) {
        this.file = file;
        this.username = username;
        this.password = password;
    }

    /** 
     * Constructs a Maven2 body as requested by Nexus, then uses a {@link MultiPartBodyPublisher} to upload to the provided nexus URL, using multipart/form-data.
     * 
     * @return int
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public int upload() throws IOException, InterruptedException {

        Maven2Body maven2Body = new Maven2Body().newBuilder()
            .asset(file.toPath())
            .extension("zip")
            .groupId("fish.payara.extras")
            .artifactId(file.getName().substring(0, file.getName().lastIndexOf(".")))
            .version(getClass().getPackage().getImplementationVersion())
            .packaging("zip")
            .generatePom(true)
            .build();

        MultiPartBodyPublisher publisher = new MultiPartBodyPublisher()
            .addPart(Maven2Body.MAVEN2_ASSET, maven2Body.getAsset())
            .addPart(Maven2Body.MAVEN2_EXTENSION, maven2Body.getExtension())
            .addPart(Maven2Body.MAVEN2_GROUPID, maven2Body.getGroupId())
            .addPart(Maven2Body.MAVEN2_ARTIFACTID, maven2Body.getArtifactId())
            .addPart(Maven2Body.MAVEN2_VERSION, maven2Body.getVersion())
            .addPart(Maven2Body.MAVEN2_PACKAGING, maven2Body.getPackaging())
            .addPart(Maven2Body.MAVEN2_GENERATE_POM, maven2Body.getGeneratePomString());

        URI uri = resolveNexusURI();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .header(CONTENT_TYPE, CONTENT_TYPE_MULTIPART_BOUNDARY + publisher.getBoundary())
            .header(AUTHORIZATION_HEADER, getAuthString(username, password))
            .timeout(Duration.ofMinutes(5))
            .POST(publisher.build())
            .build();

        logger.log(LogLevel.INFO, "Starting upload to {0}", uri.toString());
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int responseCode = response.statusCode();
        logger.log(LogLevel.INFO, "Server Responded with {0}", responseCode);

        if(responseCode != SUCCESS_HTTP_RESPONSE_CODE) {
            return 1;
        }

        return 0;
    }
    
    /** 
     *  Returns BASIC UTF-8 encoded string from username and password arguments.
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
     * Returns the URI required to upload to a repository.
     * 
     * Will use username as the repository target, or {@value #REPO_SYS_PROP} system property if not null.
     * 
     * @return URI
     */
    private URI resolveNexusURI() {
        StringBuilder builder = new StringBuilder(NEXUS_URL).append("?repository=");
        if(REPO_SYS_PROP != null) {
            builder.append(REPO_SYS_PROP);
        } else if(username != null) {
            builder.append(username);
        }

        try {
            URL url = new URL(builder.toString());
            return new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
        } catch (MalformedURLException e) {
            logger.log(LogLevel.SEVERE, "URL" + builder.toString() + " is malformed.");
            e.printStackTrace();
        } catch (URISyntaxException e) {
            logger.log(LogLevel.SEVERE, "URI" + builder.toString() + " is malformed.");
            e.printStackTrace();
        }

        return URI.create(builder.toString());
    }
    
}
