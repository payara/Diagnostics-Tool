package fish.payara.extras.diagnostics.collection.collectors;

import fish.payara.extras.diagnostics.collection.Collector;
import fish.payara.extras.diagnostics.util.StringBuilderNewLineAppender;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@TargetType({CommandTarget.DAS})
@ExecuteOn(value = {RuntimeType.INSTANCE}, ifNeverStarted = FailurePolicy.Error)
public class DomainJVMCollector implements Collector {

    private MBeanServer mBeanServer;
    private final String secretProperty = "module.core.status";

    private Map<String, String> params;

    @Override
    public int collect() {
        prepare();
        System.out.println(getSummaryReport());
        return 0;
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public void setParams(Map<String, String> params) {
        if (params != null) {
            this.params = params;
        }
    }

    private synchronized void prepare() {
        mBeanServer = ManagementFactory.getPlatformMBeanServer();
    }

    private String getSummaryReport() throws RuntimeException {
        try {
            final StringBuilderNewLineAppender stringBuilder = new StringBuilderNewLineAppender(new StringBuilder());

            final OperatingSystemMXBean os = ManagementFactory.newPlatformMXBeanProxy(mBeanServer,
                    ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
            stringBuilder.append(getOSInfo(os));
            final RuntimeMXBean rt = ManagementFactory.newPlatformMXBeanProxy(mBeanServer,
                    ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
            stringBuilder.append(getArguments(rt));
            stringBuilder.append(getVMInfo(rt));
            return ( stringBuilder.toString(secretProperty) );
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getOSInfo(final OperatingSystemMXBean os) {
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        sb.append("Operating System Information:");
        sb.append(String.format("Name of the Operating System: %s", os.getName()));
        sb.append(String.format("Binary Architecture name of the Operating System: %s, Version: %s", os.getArch(),  os.getVersion()));
        sb.append(String.format("Number of processors available on the Operating System: %s", os.getAvailableProcessors()));
        sb.append(String.format("System load on the available processors for the last minute: %s. (Sum of running and queued runnable entities per minute)", getSystemLoad(os)));
        return ( sb.toString() );
    }

    private String getSystemLoad(OperatingSystemMXBean os) {
        //available only on 1.6
        String info = "NOT_AVAILABLE";
        try {
            String METHOD = "getSystemLoadAverage";
            Method m = os.getClass().getMethod(METHOD, (Class[]) null);
            if (m != null) {
                Object ret = m.invoke(os, (Object[])null);
                return ( ret.toString() );
            }
        } catch(Exception e) {

        }
        return ( info );
    }

    private String getArguments(RuntimeMXBean rt) {
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        List<String> arguments = rt.getInputArguments();
        if ((arguments.size() > 0)) {
            sb.append("List of JVM Arguments");
            for (String argument : arguments) {
                sb.append(argument);
            }
        }
        return sb.toString();
    }

    private String getVMInfo(final RuntimeMXBean rt) {
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        sb.append(String.format("General Java Runtime Environment Information for the VM: %s", rt.getName()));
        sb.append(String.format("JRE ClassPath: %s", rt.getClassPath()));
        sb.append(String.format("JRE Native Library Path: %s", rt.getLibraryPath()));
        sb.append(String.format("JRE name: %s Vendor: %s Version: %s", rt.getVmName(), rt.getVmVendor(), rt.getVmVersion()));
        sb.append(getProperties(rt));
        return ( sb.toString() );
    }

    private String getProperties(final RuntimeMXBean rt) {

        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        final Map<String, String> unsorted = rt.getSystemProperties();
        // I decided to sort this for better readability -- 27 Feb 2006
        final TreeMap<String, String> props = new TreeMap<String, String>(unsorted);
        sb.append("");
        sb.append("List of System Properties for the Java Virtual Machine:");
        for (Map.Entry<String, String> entry : props.entrySet()) {
            sb.append(entry.getKey()+" = "+filterForbidden(entry.getKey(), entry.getValue()));
        }
        return ( sb.toString() );
    }

    private String filterForbidden(String key, String value) {
        if(ok(key) && key.startsWith("javax.net.ssl.") && key.indexOf("password") >= 0)
            return "********";
        else
            return value;
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0 && !s.equals("null");
    }
}
