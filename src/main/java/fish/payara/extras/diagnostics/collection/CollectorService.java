package fish.payara.extras.diagnostics.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.glassfish.api.admin.ParameterMap;


public class CollectorService {
    Logger logger = Logger.getLogger(this.getClass().getName());

    ParameterMap parameterMap;
    String[] parameterOptions;
    Map<String, Collector> collectors;

    public CollectorService(ParameterMap params, String[] parameterOptions, Map<String, Collector> collectors) {
        this.parameterMap = params;
        this.parameterOptions = parameterOptions;
        this.collectors = collectors;
    }

    public int executCollection() {
        List<Collector> activeCollectors = getActiveCollectors(parameterMap, parameterOptions, collectors);

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
