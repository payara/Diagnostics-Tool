package fish.payara.extras.diagnostics.collection;

import java.nio.file.Path;
import java.util.Map;

import org.glassfish.api.admin.ParameterMap;

public interface Collector {
    public int collect();
    public void setParams(Map<String, String> params);
    public Map<String, String> getParams();
}