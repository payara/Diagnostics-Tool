package fish.payara.extras.diagnostics.collection;

import java.util.List;
import java.util.Map;

import org.glassfish.api.admin.ParameterMap;
import org.junit.Before;
import org.junit.Test;

import fish.payara.extras.diagnostics.collection.collectors.LogCollector;

import static java.util.Map.entry;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CollectorServiceTest {
    private CollectorService collectorService;

    private static final String[] PARAMETER_OPTIONS = {"Para1","Para2","Para3","Para4","Para5","Para6","Para7","Para8","Para9","Para10","Para11"};
    private static final Map<String, Collector> COLLECTORS = Map.ofEntries(
        entry(PARAMETER_OPTIONS[0], new LogCollector()),
        entry(PARAMETER_OPTIONS[1], new LogCollector()),
        entry(PARAMETER_OPTIONS[2], new LogCollector()),
        entry(PARAMETER_OPTIONS[3], new LogCollector()),
        entry(PARAMETER_OPTIONS[4], new LogCollector()),
        entry(PARAMETER_OPTIONS[5], new LogCollector()),
        entry(PARAMETER_OPTIONS[6], new LogCollector()),
        entry(PARAMETER_OPTIONS[7], new LogCollector()),
        entry(PARAMETER_OPTIONS[8], new LogCollector()),
        entry(PARAMETER_OPTIONS[9], new LogCollector()),
        entry(PARAMETER_OPTIONS[10], new LogCollector())
    );
    
    @Test
    public void getActiveCollectorsAllFalseTest()
    {
        ParameterMap params = new ParameterMap();

        for(String parameter : PARAMETER_OPTIONS) {
            params.add(parameter, "false");
        }

        CollectorService collectorService = new CollectorService(params, PARAMETER_OPTIONS, COLLECTORS);
        List<Collector> activeCollectors = collectorService.getActiveCollectors(params, PARAMETER_OPTIONS, COLLECTORS);
        assertNotNull(activeCollectors);
        assertTrue(activeCollectors.size() == 0);
    }

    @Test
    public void getActiveCollectorsAllTrueTest()
    {
        ParameterMap params = new ParameterMap();

        for(String parameter : PARAMETER_OPTIONS) {
            params.add(parameter, "true");
        }

        CollectorService collectorService = new CollectorService(params, PARAMETER_OPTIONS, COLLECTORS);
        List<Collector> activeCollectors = collectorService.getActiveCollectors(params, PARAMETER_OPTIONS, COLLECTORS);
        assertNotNull(activeCollectors);
        assertTrue(activeCollectors.size() == 11);
    }

    @Test
    public void getActiveCollectorsMixedTest()
    {
        ParameterMap params = new ParameterMap();

        for(int i = 0; i < PARAMETER_OPTIONS.length/2; i++) {
            params.add(PARAMETER_OPTIONS[i], "true");
        }

        for(int i = PARAMETER_OPTIONS.length/2; i < PARAMETER_OPTIONS.length; i++) {
            params.add(PARAMETER_OPTIONS[i], "false");
        }

        CollectorService collectorService = new CollectorService(params, PARAMETER_OPTIONS, COLLECTORS);
        List<Collector> activeCollectors = collectorService.getActiveCollectors(params, PARAMETER_OPTIONS, COLLECTORS);
        assertNotNull(activeCollectors);
        assertTrue(activeCollectors.size() == 5);
    }

    @Test
    public void getActiveCollectorsNullTest() {
        CollectorService collectorService = new CollectorService(null, null, null);
        List<Collector> activeCollectors = collectorService.getActiveCollectors(null, null, null);
        assertNotNull(activeCollectors);
        assertTrue(activeCollectors.size() == 0);
    }

    public void getActiveCollectorsIncompleteTest() {

    }
}
