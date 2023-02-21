package fish.payara.extras.diagnostics.upload.util;

import java.nio.file.Path;

public class Maven2Body {
    //          addPart("maven2.asset1", file.toPath())
    //         .addPart("maven2.asset1.extension", "zip")
    //         .addPart("maven2.groupId", "fish.payara.extras")
    //         .addPart("maven2.artifactId", "diagnostics-tool")
    //         .addPart("maven2.version", getClass().getPackage().getImplementationVersion())
    //         .addPart("maven2.packaging", "zip")
    //         .addPart("maven2.generate-pom", "true");

    private Path asset;
    private String extension;
    private String groupId;
    private String artifactId;
    private String version;
    private String packaging;
    private boolean generatePom;

    public final static String MAVEN2_ASSET = "maven2.asset1";
    public final static String MAVEN2_EXTENSION = "maven2.asset1.extension";
    public final static String MAVEN2_GROUPID = "maven2.groupId";
    public final static String MAVEN2_ARTIFACTID = "maven2.artifactId";
    public final static String MAVEN2_VERSION = "maven2.version";
    public final static String MAVEN2_PACKAGING = "maven2.packaging";
    public final static String MAVEN2_GENERATE_POM = "maven2.generate-pom";

    public Path getAsset() {
        return asset;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setAsset(Path asset) {
        this.asset = asset;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public boolean getGeneratePom() {
        return generatePom;
    }

    public String getGeneratePomString() {
        return String.valueOf(generatePom);
    }

    public void setGeneratePom(boolean generatePom) {
        this.generatePom = generatePom;
    }

    public Maven2BodyBuilder newBuilder() {
        return new Maven2BodyBuilder();
    }

    public class Maven2BodyBuilder {
        private Maven2Body maven2Body;

        public Maven2BodyBuilder() {
            this.maven2Body = new Maven2Body();
        }

        public Maven2BodyBuilder asset(Path value) {
            this.maven2Body.setAsset(value);
            return this;
        }

        public Maven2BodyBuilder extension(String value) {
            this.maven2Body.setExtension(value);
            return this;
        }

        public Maven2BodyBuilder groupId(String value) {
            this.maven2Body.setGroupId(value);
            return this;
        }

        public Maven2BodyBuilder artifactId(String value) {
            this.maven2Body.setArtifactId(value);
            return this;
        }

        public Maven2BodyBuilder version(String value) {
            this.maven2Body.setVersion(value);
            return this;
        }

        public Maven2BodyBuilder packaging(String value) {
            this.maven2Body.setPackaging(value);
            return this;
        }

        public Maven2BodyBuilder generatePom(Boolean value) {
            this.maven2Body.setGeneratePom(value);
            return this;
        }

        public Maven2Body build() {
            return this.maven2Body;
        }

    }

}
