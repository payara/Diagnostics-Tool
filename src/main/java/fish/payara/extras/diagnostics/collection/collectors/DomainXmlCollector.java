package fish.payara.extras.diagnostics.collection.collectors;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import fish.payara.extras.diagnostics.util.ParamConstants;

public class DomainXmlCollector extends FileCollector {

    @Override
    public int collect() {
        Map<String, String> params = getParams();
        if(params != null) {
            Path outputPath = getPathFromParams(ParamConstants.DIR_PARAM, params);
            Path domainXmlPath = getPathFromParams(ParamConstants.DOMAIN_XML_FILE_PATH, params);
            if(domainXmlPath != null && outputPath != null) {
                setFilePath(domainXmlPath);
                setDestination(outputPath);
                
                return super.collect();
            }
        }

        return 0;
    }

    private Path getPathFromParams(String key, Map<String, String> parameterMap) {
        Map<String, String> params = parameterMap;
        if(params != null) {
            String valueString = params.get(key);
            if(valueString != null) {
                return Paths.get(valueString);
            }
        }

        return null;
    }

    

}
