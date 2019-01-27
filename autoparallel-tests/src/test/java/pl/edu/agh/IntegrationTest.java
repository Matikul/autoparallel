package pl.edu.agh;

import org.junit.BeforeClass;
import org.junit.Test;
import pl.edu.agh.transformations.BytecodeModifier;

import java.io.IOException;

public class IntegrationTest {

    private static final String TEST_CLASS_LOCATION = "src/test/resources";
    private static final String TEST_CLASS_NAME = "Test1";

    private static BytecodeModifier modifier;

    @BeforeClass
    public static void init() {
        modifier = new BytecodeModifier();
    }

    @Test
    public void modifiedClassRunTest() throws IOException {
        modifier.modifyBytecode(TEST_CLASS_LOCATION, TEST_CLASS_NAME);
        Runtime runtime = Runtime.getRuntime();
        String command = System.getProperty("java.home") + "\\bin\\java -cp " + TEST_CLASS_LOCATION + " " + TEST_CLASS_NAME + BytecodeModifier.MODIFICATION_SUFFIX;
        try {
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
