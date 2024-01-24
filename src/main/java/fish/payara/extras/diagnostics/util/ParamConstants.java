package fish.payara.extras.diagnostics.util;

public final class ParamConstants {
    //Base Params
    public static final String DIR_PARAM = "dir";
    public static final String PROPERTIES_PARAM = "properties";

    //Collect Params
    public static final String SERVER_LOG_PARAM = "serverLogs";
    public static final String DOMAIN_XML_PARAM = "domainXml";
    public static final String INSTANCES_DOMAIN_XML_PARAM = "instanceDomainXml";
    public static final String INSTANCES_LOG_PARAM = "instanceLogXml";
    public static final String DOMAIN_JVM_REPORT_PARAM = "domainJvmReport";
    public static final String INSTANCE_JVM_REPORT_PARAM = "instanceJvmReport";

    //Upload Params
    public static final String USERNAME_PARAM = "username";
    public static final String PASSWORD_PARAM = "password";
    public static final String UPLOAD_DESTINATION_PARAM = "destination";
    public static final String TICKET_NUM_PARAM = "ticket";

    //Option Params
    public static final String DOMAIN_NAME = "DomainName";
    public static final String DOMAIN_XML_FILE_PATH = "DomainXMLFilePath";
    public static final String LOGS_PATH = "LogPath";
    public static final String INSTANCES_DOMAIN_XML_PATH = "InstancesDomainXmlPath";
    public static final String INSTANCES_LOG_PATH = "InstancesLogPath";
    public static final String INSTANCES_NAMES = "InstancesNames";

    public static final String NEXUS = "nexus";
    public static final String ZENDESK = "zendesk";

    private ParamConstants(){};
}
