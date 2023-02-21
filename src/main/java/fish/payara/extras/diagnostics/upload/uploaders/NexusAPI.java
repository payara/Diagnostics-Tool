package fish.payara.extras.diagnostics.upload.uploaders;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import fish.payara.extras.diagnostics.upload.Uploader;
import fish.payara.extras.diagnostics.upload.util.MultiPartBodyPublisher;

public class NexusAPI implements Uploader {

    private static final String NEXUS_URL = "http://localhost:8081/service/rest/v1/components?repository=test-customer";

    private File file;
    private String username;
    private String password;

    private HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    public NexusAPI(File file, String username, String password) {
        this.file = file;
        this.username = username;
        //this.password = password;
        this.password = "admin";
    }

    @Override
    public int upload() throws IOException, InterruptedException {

        MultiPartBodyPublisher publisher = new MultiPartBodyPublisher()
            .addPart("maven2.asset1", file.toPath())
            .addPart("maven2.asset1.extension", "zip")
            .addPart("maven2.groupId", "fish.payara.extras")
            .addPart("maven2.artifactId", "diagnostics-tool")
            .addPart("maven2.version", getClass().getPackage().getImplementationVersion())
            .addPart("maven2.packaging", "zip")
            .addPart("maven2.generate-pom", "true");
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(NEXUS_URL))
            .header("Content-Type", "multipart/form-data; boundary=" + publisher.getBoundary())
            .header("Authorization", getAuthString(username, password))
            .timeout(Duration.ofMinutes(1))
            .POST(publisher.build())
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.statusCode());
        System.out.println(response.body());

        return 0;
    }

    private String getAuthString(String username, String password) {
        String authString = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
    }
    
}
