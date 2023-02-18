package fish.payara.extras.diagnostics.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;

import org.glassfish.api.admin.ParameterMap;

import fish.payara.extras.diagnostics.collection.collectors.DomainXmlCollector;
import fish.payara.extras.diagnostics.collection.collectors.LogCollector;


public class CollectorService {
    
    private static final Map<String, Collector> COLLECTORS = Map.ofEntries(
        entry("serverLogs", new LogCollector()),
        entry("domainXml", new DomainXmlCollector())
    );

    ParameterMap parameterMap;
    String[] parameterOptions;

    public CollectorService(ParameterMap params, String[] parameterOptions) {
        this.parameterMap = params;
        this.parameterOptions = parameterOptions;
    }

    public int executCollection() {
        List<Collector> activeCollectors = getActiveCollectors(parameterMap, parameterOptions, COLLECTORS);

        if(activeCollectors.size() != 0) {
            for(Collector collector : activeCollectors) {
                collector.setParams(parameterMap);
                collector.collect();
            }
        }

        return 0;
    }

    public List<Collector> getActiveCollectors(ParameterMap parameterMap, String[] parameterOptions, Map<String, Collector> collectors) {
        List<Collector> activeCollectors = new ArrayList<>();

        if(parameterMap == null || parameterOptions == null || collectors == null) {
            return activeCollectors;
        }

        for (String parameter : parameterOptions) {
            String parameterValue = parameterMap.getOne(parameter);
            Boolean collectorValue = Boolean.valueOf(parameterValue);
            if(collectorValue) {
                activeCollectors.add(collectors.get(parameter));
            }
        }
        return activeCollectors;
    }

}
