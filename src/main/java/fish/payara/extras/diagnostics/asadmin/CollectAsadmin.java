package fish.payara.extras.diagnostics.asadmin;

import java.util.Map;
import java.util.logging.Logger;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import static java.util.Map.entry;

import fish.payara.extras.diagnostics.collection.Collector;
import fish.payara.extras.diagnostics.collection.CollectorService;
import fish.payara.extras.diagnostics.collection.collectors.DomainXmlCollector;
import fish.payara.extras.diagnostics.collection.collectors.LogCollector;

@Service(name = "collect")
@PerLookup
public class CollectAsadmin extends BaseAsadmin {
    Logger logger = Logger.getLogger(this.getClass().getName());

    private static final String[] PARAMETER_OPTIONS = {"serverLog", "domainXml", "outputDir"};
    private static final Map<String, Collector> COLLECTORS = Map.ofEntries(
    entry(PARAMETER_OPTIONS[0], new LogCollector()),
    entry(PARAMETER_OPTIONS[1], new DomainXmlCollector())
    );

    private static final String LOGGING_CONFIG_FILE_SYS_PROP = "java.util.logging.config.file";

    @Param(name = "serverLog", shortName = "s", optional = true, defaultValue = "true")
    private boolean collectServerLog;

    @Param(name = "domainXml", shortName = "d", optional = true, defaultValue = "true")
    private boolean collectDomainXml;

    @Param(name = "outputDir", shortName = "o", optional = false, defaultValue = "/output")
    private String outputDir;

    private CollectorService collectorService;

    private ParameterMap parameterMap;

    @Override
    protected int executeCommand() throws CommandException {
        parameterMap = populateParameters(new ParameterMap());
        
        collectorService = new CollectorService(parameterMap, PARAMETER_OPTIONS, COLLECTORS);

        return collectorService.executCollection();
    }

    private ParameterMap populateParameters(ParameterMap params) {
        for(String opt : PARAMETER_OPTIONS) {
            params.add(opt, getOption(opt));
        }

        params.add("DomainXMLFilePath", getDomainXml().getAbsolutePath());
        params.add("DomainName", getDomainName());

        params.add(LOGGING_CONFIG_FILE_SYS_PROP, getLoggingConfigFilePath());

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
