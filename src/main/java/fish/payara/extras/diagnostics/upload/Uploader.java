package fish.payara.extras.diagnostics.upload;

import java.io.FileNotFoundException;

public interface Uploader {
    public int upload() throws FileNotFoundException;
}
