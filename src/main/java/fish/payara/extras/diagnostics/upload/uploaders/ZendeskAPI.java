package fish.payara.extras.diagnostics.upload.uploaders;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

import fish.payara.extras.diagnostics.upload.Uploader;

public class ZendeskAPI implements Uploader {
    Logger logger = Logger.getLogger(this.getClass().getName());

    private static final String NEXUS_URL = "https://payara.zendesk.com/api/v2/uploads";

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
    public int upload() throws FileNotFoundException, Exception {

        return 0;
    }
    
}
