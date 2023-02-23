package fish.payara.extras.diagnostics.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PropertiesFileTest {
    
    private static final Path TEST_FILE_PATH = Path.of(System.getProperty("java.io.tmpdir")).resolve(".testproperties");
    PropertiesFile props;

    @Before
    public void initialisePropertiesFile() {
        props = new PropertiesFile(TEST_FILE_PATH);
    }

    @Test
    public void propertiesValidStoreTest() {
        assertNotNull(props);
        props.store("test", "test");
        assertTrue(Files.exists(TEST_FILE_PATH));
        assertEquals("test", props.get("test"));
    }

    @Test
    public void propertiesConsecutiveValidStoreTest() {
        assertNotNull(props);
        props.store("test", "test");
        assertTrue(Files.exists(TEST_FILE_PATH));
        assertEquals("test", props.get("test"));

        props.store("test2", "test");
        assertTrue(Files.exists(TEST_FILE_PATH));
        assertEquals("test", props.get("test"));
        assertEquals("test", props.get("test2"));
    }

    @Test
    public void propertiesOverwriteExistingKeyTest() {
        assertNotNull(props);
        props.store("test", "test");
        assertTrue(Files.exists(TEST_FILE_PATH));
        assertEquals("test", props.get("test"));

        props.store("test", "test1");
        assertTrue(Files.exists(TEST_FILE_PATH));
        assertEquals("test1", props.get("test"));
    }

    @Test
    public void propertiesInvalidGetTest() {
        assertNotNull(props);
        props.store("test", "test");
        assertTrue(Files.exists(TEST_FILE_PATH));
        assertEquals(null, props.get("non-existant-key"));
    }

    @Test
    public void propertiesGetBeforeStoreTest() {
        assertNotNull(props);
        assertFalse(Files.exists(TEST_FILE_PATH));
        assertEquals(null, props.get("test"));
    }

    @Test
    public void propertiesNonExistantPathTest() {
        Path nonPath = Path.of("this/path/does/not/exist");
        props = new PropertiesFile(nonPath);
        assertNotNull(props);
        props.store("test", "test");
        assertFalse(Files.exists(nonPath));
        assertEquals(null, props.get("test"));
    }

    @After
    public void cleanUp() {
        if(Files.exists(TEST_FILE_PATH)) {
            TEST_FILE_PATH.toFile().delete();
        }
    }

}
