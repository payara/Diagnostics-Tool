package fish.payara.extras.diagnostics.util;

public enum JvmCollectionType {
    JVM_REPORT("summary"), THREAD_DUMP("thread");
    public String value;

    JvmCollectionType(String value) {
        this.value = value;
    }
}
