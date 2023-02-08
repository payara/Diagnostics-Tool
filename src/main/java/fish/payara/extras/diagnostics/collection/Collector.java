package fish.payara.extras.diagnostics.collection;

import java.nio.file.Path;

import org.glassfish.api.admin.ParameterMap;

public interface Collector {
    public int collect();
    public void setParams(ParameterMap params);
    public ParameterMap getParams();
}