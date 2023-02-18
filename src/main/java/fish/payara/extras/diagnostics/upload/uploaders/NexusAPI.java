package fish.payara.extras.diagnostics.upload.uploaders;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import fish.payara.extras.diagnostics.upload.Uploader;

public class NexusAPI implements Uploader {

    private final static long boundary = System.currentTimeMillis();

    private HttpURLConnection connection;

    /*curl -v -u admin:admin123 
    -F "maven2.generate-pom=false" 
    -F "maven2.asset1=@/absolute/path/to/the/local/file/pom.xml" 
    -F "maven2.asset1.extension=pom" 
    -F "maven2.asset2=@/absolute/path/to/the/local/file/product-1.0.0.jar;type=application/java-archive" 
    -F "maven2.asset2.extension=jar" "http://localhost:8081/service/rest/v1/components?repository=maven-releases"*/

    @Override
    public int upload() {
        
        return 0;
    }
    
}
