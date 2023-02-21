package fish.payara.extras.diagnostics.collection.collectors;

import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

public class DomainXmlCollector extends FileCollector {
    Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public int collect() {
        Map<String, String> params = getParams();
        if(params != null) {
            Path outputPath = getPathFromParams("dir", params);
            Path domainXmlPath = getPathFromParams("DomainXMLFilePath", params);
            if(domainXmlPath != null && outputPath != null) {
                setFilePath(domainXmlPath);
                setDestination(outputPath);
                
                return super.collect();
            }
        }

        return 1;
    }

    private Path getPathFromParams(String key, Map<String, String> parameterMap) {
        Map<String, String> params = parameterMap;
        if(params != null) {
            String valueString = params.get(key);
            if(valueString != null) {
                Path path = Path.of(valueString);
                return path;
            }
        }

        return null;
    }

    

}
