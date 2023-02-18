package fish.payara.extras.diagnostics.asadmin;

import java.util.Map;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import fish.payara.extras.diagnostics.collection.CollectorService;

@Service(name = "collect")
@PerLookup
public class CollectAsadmin extends BaseAsadmin {
    private static final String SERVER_LOG_PARAM = "serverLogs";
    private static final String DOMAIN_XML_PARAM = "domainXml";
    private static final String OUTPUT_DIR_PARAM = "outputDir";

    private static final String[] PARAMETER_OPTIONS = {SERVER_LOG_PARAM, DOMAIN_XML_PARAM, OUTPUT_DIR_PARAM};

    private static final String LOGGING_CONFIG_FILE_SYS_PROP = "java.util.logging.config.file";
    private static final String DOMAIN_NAME = "DomainName";
    private static final String DOMAIN_XML_FILE_PATH = "DomainXMLFilePath";
    private static final String LOGS_PATH = "LogPath";

    @Param(name = SERVER_LOG_PARAM, shortName = "s", optional = true, defaultValue = "true")
    private boolean collectServerLog;

    @Param(name = DOMAIN_XML_PARAM, shortName = "d", optional = true, defaultValue = "true")
    private boolean collectDomainXml;

    @Param(name = OUTPUT_DIR_PARAM, shortName = "o", optional = false)
    private String outputDir;

    CollectorService collectorService;

    @Override
    protected int executeCommand() throws CommandException {
        parameterMap = populateParameters(new ParameterMap());
        
        collectorService = new CollectorService(parameterMap, PARAMETER_OPTIONS);

        return collectorService.executCollection();
    }

    private ParameterMap populateParameters(ParameterMap params) throws CommandException {
        for(String opt : PARAMETER_OPTIONS) {
            params.add(opt, getOption(opt));
        }

        params.add(DOMAIN_XML_FILE_PATH, getDomainXml().getAbsolutePath());
        params.add(DOMAIN_NAME, getDomainName());

        params.add(LOGGING_CONFIG_FILE_SYS_PROP, getLoggingConfigFilePath());
        params.add(LOGS_PATH, getDomainRootDir().getPath() + "/logs");

        return params;
    }

    private String getLoggingConfigFilePath() {
        String loggingConfigFile = getSystemProperty(LOGGING_CONFIG_FILE_SYS_PROP);
        if(loggingConfigFile != null) {
            return getSystemProperty(LOGGING_CONFIG_FILE_SYS_PROP);
        }
        return "";
    }
}
