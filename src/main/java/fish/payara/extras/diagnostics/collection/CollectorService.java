package fish.payara.extras.diagnostics.collection;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.api.admin.ParameterMap;


public class CollectorService {

    private Map<String, Collector> collectorMap;
    private ParameterMap parameterMap;

    public CollectorService(ParameterMap params) {
        this.parameterMap = params;
        System.out.println(params);

        collectorMap = new HashMap<String, Collector>();
    }

    public void registerCollector(Collector collector, String key) {
        this.collectorMap.put(key, collector);
    }
}
