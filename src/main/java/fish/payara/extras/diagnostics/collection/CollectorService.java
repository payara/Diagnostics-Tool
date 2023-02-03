package fish.payara.extras.diagnostics.collection;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.glassfish.api.admin.ParameterMap;

import fish.payara.extras.diagnostics.collection.collectors.LogCollector;

public class CollectorService {

    private Map<String, Collector> collectorMap;
    private ParameterMap parameterMap;

    public CollectorService(ParameterMap params) {
        this.parameterMap = params;

        collectorMap = new HashMap<String, Collector>();
    }

    public void registerCollector(Collector collector, String key) {
        this.collectorMap.put(key, collector);
    }
}
