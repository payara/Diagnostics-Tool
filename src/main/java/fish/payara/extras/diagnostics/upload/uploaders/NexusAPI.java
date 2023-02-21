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
import fish.payara.extras.diagnostics.upload.util.Maven2Body;
import fish.payara.extras.diagnostics.upload.util.MultiPartBodyPublisher;

public class NexusAPI implements Uploader {

    private static final String NEXUS_URL = "http://localhost:8081/service/rest/v1/components?repository=test-customer";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_MULTIPART_BOUNDARY = "multipart/form-data; boundary=";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private File file;
    private String username;
    private String password;

    private HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    public NexusAPI(File file, String username, String password) {
        this.file = file;
        this.username = username;
        this.password = password;
    }

    @Override
    public int upload() throws IOException, InterruptedException {

        Maven2Body maven2Body = new Maven2Body().newBuilder()
            .asset(file.toPath())
            .extension("zip")
            .groupId("fish.payara.extras")
            .artifactId("diagnostics-tool")
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

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(NEXUS_URL))
            .header(CONTENT_TYPE, CONTENT_TYPE_MULTIPART_BOUNDARY + publisher.getBoundary())
            .header(AUTHORIZATION_HEADER, getAuthString(username, password))
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
