package pl.edu.agh;

import org.apache.bcel.generic.TargetLostException;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.edu.agh.transformations.BytecodeModifier;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class IntegrationTest {

    private static final String TEST_CLASS_LOCATION = "src/test/resources/nbody";
    private static final String TEST_CLASS_NAME = "IntegrationTestClass";

    private static final int MODIFIED_METHOD_POSITION = 1; //main() method position

    private static BytecodeModifier modifier;

    @BeforeClass
    public static void init() {
        modifier = new BytecodeModifier();
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void modifiedClassRunTest() throws IOException, TargetLostException {
        modifier.modifyBytecode(TEST_CLASS_LOCATION, TEST_CLASS_NAME, MODIFIED_METHOD_POSITION, (short) 1000);
        Runtime runtime = Runtime.getRuntime();
        String command = System.getProperty("java.home") + "\\bin\\java -cp " + TEST_CLASS_LOCATION + " " + TEST_CLASS_NAME + BytecodeModifier.MODIFICATION_SUFFIX;
        try {
            Process process = runtime.exec(command);
            process.waitFor();
            assertEquals(0, process.exitValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void test() throws IOException, TargetLostException {
//        modifier.modifyBytecode(TEST_CLASS_LOCATION, TEST_CLASS_NAME, MODIFIED_METHOD_POSITION);
        modifier.modifyBytecode(TEST_CLASS_LOCATION, TEST_CLASS_NAME, 3, (short) 1000);//MOVE BODIES METHOD
        Runtime runtime = Runtime.getRuntime();
        String command = System.getProperty("java.home") + "\\bin\\java -cp " + TEST_CLASS_LOCATION + " " + TEST_CLASS_NAME + BytecodeModifier.MODIFICATION_SUFFIX;
        try {
            Process process = runtime.exec(command);
            process.waitFor();
            assertEquals(0, process.exitValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
