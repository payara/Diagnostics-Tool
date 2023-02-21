package fish.payara.extras.diagnostics.asadmin;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.glassfish.api.ExecutionContext;
import org.glassfish.api.Param;
import org.glassfish.api.ParamDefaultCalculator;

import com.sun.enterprise.admin.servermgmt.cli.LocalDomainCommand;

public abstract class BaseAsadmin extends LocalDomainCommand {
    protected static final String DIR_PARAM = "dir";
    protected static final String DIR_NAME = "/output-" + System.currentTimeMillis();

    @Param(name = DIR_PARAM, shortName = "d", optional = true, defaultCalculator = DefaultOutputDirParam.class)
    protected String dir;

    protected Map<String, String> parameterMap;

    protected Map<String, String> resolveDir(Map<String, String> params) {
        if(params == null) {
            return params;
        }

        if(getOption(DIR_PARAM) == null) {
            if(dir != null) {
                dir = dir + DIR_NAME;
                if(params.containsKey(DIR_PARAM)) {
                    params.replace(DIR_PARAM, dir);
                } else {
                    params.put(DIR_PARAM, dir);
                }
            }
        }

        return params;
    }

    protected Map<String, String> populateParameters(Map<String, String> params, String[] paramOptions) {
        for(String opt : paramOptions) {
            params.put(opt, getOption(opt));
        }

        return params;
    }

    public static class DefaultOutputDirParam extends ParamDefaultCalculator {
        private static final String OUTPUT_DIR_PARAM_SYS_PROP = "fish.payara.diagnostics.path";
        private static final String JAVA_TEMP_DIR_SYS_PROP = "user.home";

        @Override
        public String defaultValue(ExecutionContext context) {
            return System.getProperty(OUTPUT_DIR_PARAM_SYS_PROP, System.getProperty(JAVA_TEMP_DIR_SYS_PROP));
        }
    }
}