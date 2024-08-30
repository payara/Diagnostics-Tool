package fish.payara.extras.diagnostics.collection.collectors;

import fish.payara.extras.diagnostics.util.ObfuscateDomainXml;
import fish.payara.extras.diagnostics.util.ParamConstants;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Logger;

public class DomainXmlCollector extends FileCollector {

    private Path path;
    private String dirSuffix;
    private Logger LOGGER = Logger.getLogger(DomainXmlCollector.class.getName());
    private boolean obfuscateDomainXml;
    private final int COLLECTED_OKAY = 0;
    private final ObfuscateDomainXml obfuscate = new ObfuscateDomainXml();

    public DomainXmlCollector(Path path, boolean obfuscateDomainXml) {
        this.path = path;
        this.obfuscateDomainXml = obfuscateDomainXml;
    }

    public DomainXmlCollector(Path path, String instanceName, String dirSuffix, boolean obfuscateDomainXml) {
        this.path = path;
        super.setInstanceName(instanceName);
        this.dirSuffix = dirSuffix;
        this.obfuscateDomainXml = obfuscateDomainXml;
    }

    @Override
    public int collect() {
        int domainXmlCollected = COLLECTED_OKAY;
        Map<String, Object> params = getParams();
        if (params != null) {
            Path outputPath = getPathFromParams(ParamConstants.DIR_PARAM, params);
            if (path != null && outputPath != null) {
                setFilePath(path);
                setDestination(Paths.get(outputPath.toString(), dirSuffix != null ? dirSuffix : ""));
                LOGGER.info("Collecting domain.xml from " + (getInstanceName() != null ? getInstanceName() : "server"));
                domainXmlCollected = super.collect();
                if (domainXmlCollected == COLLECTED_OKAY && obfuscateDomainXml) {
                    obfuscate.obfuscateDomainXml(resolveDestinationFile().toFile());
                }
            }
        }
        return domainXmlCollected;
    }

    private Path getPathFromParams(String key, Map<String, Object> parameterMap) {
        Map<String, Object> params = parameterMap;
        if (params != null) {
            String valueString = (String) params.get(key);
            if (valueString != null) {
                return Paths.get(valueString);
            }
        }
        return null;
    }
}
