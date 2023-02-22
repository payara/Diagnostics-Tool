package fish.payara.extras.diagnostics.collection;

import java.util.Map;

public interface Collector {
    public int collect();
    public void setParams(Map<String, String> params);
    public Map<String, String> getParams();
}