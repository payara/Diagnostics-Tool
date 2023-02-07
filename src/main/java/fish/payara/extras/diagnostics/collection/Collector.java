package fish.payara.extras.diagnostics.collection;

import org.glassfish.api.admin.ParameterMap;

public interface Collector {
    public int collect();
    public void setFilePath(String filePath);
    public void setDestination(String path);
    public void setParams(ParameterMap params);
    public void getFilePath();
    public void getDestination();
    public ParameterMap getParams();
}