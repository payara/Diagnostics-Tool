package fish.payara.extras.diagnostics.collection.collectors;

public class LogCollector extends FileCollector {
    
    @Override
    public int collect() {
        System.out.println("Collect: " + this.getClass().getName());
        return 0;
    }

    private String getLogPathFromDomainXml(String domainXmlPath) {

        return null;
    }

}
