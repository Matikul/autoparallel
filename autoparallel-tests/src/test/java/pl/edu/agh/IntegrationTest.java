package pl.edu.agh;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.edu.agh.transformations.BytecodeModifier;
import pl.edu.agh.transformations.util.LoopUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class IntegrationTest {

    private static final String TEST_CLASS_LOCATION = "src/test/resources";
    private static final String TEST_CLASS_NAME = "IntegrationTestClass";

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
            Process process = runtime.exec(command);
            process.waitFor();
            assertEquals(0, process.exitValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() throws IOException {
        JavaClass analyzedClass = new ClassParser(TEST_CLASS_LOCATION + "\\" + "Test2.class").parse();
        ClassGen classGen = new ClassGen(analyzedClass);
        MethodGen methodGen = new MethodGen(analyzedClass.getMethods()[1], classGen.getClassName(), classGen.getConstantPool());
        LoopUtils.getForLoop(methodGen);
    }
}
