package fish.payara.extras.diagnostics.collection.collectors;

import fish.payara.extras.diagnostics.collection.Collector;

public class LogCollector implements Collector {

    // java.util.logging.config.file
    // com.sun.enterprise.server.logging.GFFileHandler.file
    
    @Override
    public void Collect() {
        
    }

    public Boolean preliminaryCheck() {
        return false;
    }

}
