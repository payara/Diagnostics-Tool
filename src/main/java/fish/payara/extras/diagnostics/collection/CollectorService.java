package fish.payara.extras.diagnostics.collection;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import fish.payara.extras.diagnostics.collection.collectors.LogCollector;

public class CollectorService {

    private Map<String, Collector> collectorMap;
    private Properties properties;

    public CollectorService(Properties properties) {
        collectorMap = new HashMap<String, Collector>();
        collectorMap.put("Log", new LogCollector());
        this.properties = properties;
    }

    public String Collect() {
        for(Collector c : collectorMap.values()) {
            c.Collect();
        }
        return properties.getProperty("Key");
    }
}
