package fish.payara.extras.diagnostics.collection.collectors;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

import org.glassfish.api.admin.ParameterMap;

public class DomainXmlCollector extends FileCollector {
    Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public int collect() {
        ParameterMap params = getParams();
        if(params != null) {
            Path outputPath = getPathFromParams("outputDir", params);
            Path domainXmlPath = getPathFromParams("DomainXMLFilePath", params);
            if(domainXmlPath != null && outputPath != null) {
                setFilePath(domainXmlPath);
                setDestination(outputPath);
                return super.collect();
            }
        }

        return 1;
    }

    private Path getPathFromParams(String key, ParameterMap parameterMap) {
        ParameterMap params = parameterMap;
        if(params != null) {
            String valueString = params.getOne(key);
            if(valueString != null) {
                Path path = Path.of(valueString);
                if(Files.exists(path)) {
                    return path;
                }
            }
        }

        return null;
    }

    

}
