package fish.payara.extras.diagnostics.util;

import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest.BodyPublisher;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import org.junit.Test;

public class MultiPartBodyPublisherTest {
    
    @Test
    public void multiPartDataValidPathTest() {
        MultiPartBodyPublisher multiPartBodyPublisher = new MultiPartBodyPublisher();
        Path path = Path.of("test/path");
        MultiPartBodyPublisher bodyPublisherReturn = multiPartBodyPublisher.addPart("test", path);
        assertNotNull(bodyPublisherReturn);

        BodyPublisher bodyPublisher = bodyPublisherReturn.build();
        assertNotNull(bodyPublisher);
    }

    @Test
    public void multiPartDataValidStringTest() {
        MultiPartBodyPublisher multiPartBodyPublisher = new MultiPartBodyPublisher();
        MultiPartBodyPublisher bodyPublisherReturn = multiPartBodyPublisher.addPart("test", "test");
        assertNotNull(bodyPublisherReturn);

        BodyPublisher bodyPublisher = bodyPublisherReturn.build();
        assertNotNull(bodyPublisher);
    }

    @Test
    public void multiPartDataValidStreamTest() {
        MultiPartBodyPublisher multiPartBodyPublisher = new MultiPartBodyPublisher();
        Supplier<InputStream> inputStream = () -> {
            try {
                Path path = Files.createTempDirectory("inputStreamTest", null);
                return new FileInputStream(path.toFile());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };
        MultiPartBodyPublisher bodyPublisherReturn = multiPartBodyPublisher.addPart("test", inputStream, "inputStreamTest", "application/zip");
        assertNotNull(bodyPublisherReturn);

        BodyPublisher bodyPublisher = bodyPublisherReturn.build();
        assertNotNull(bodyPublisher);
    }

}
