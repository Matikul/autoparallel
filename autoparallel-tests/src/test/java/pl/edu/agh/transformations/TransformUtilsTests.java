package pl.edu.agh.transformations;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.generic.ClassGen;
import org.junit.Test;
import pl.edu.agh.util.Constants;

import static org.junit.Assert.assertEquals;

public class TransformUtilsTests {

    private static final String TEST_CLASS_LOCATION = "src/test/resources/Test1.class";

    @Test
    public void injectThreadPoolTest() throws Exception{
        ClassGen testClass = new ClassGen(new ClassParser(TEST_CLASS_LOCATION).parse());
        TransformUtils.addThreadPool(testClass);
        Field[] constantFields = testClass.getFields();
        assertEquals(2, constantFields.length);
        assertEquals(Constants.NUMBER_OF_THREADS_CONSTANT_NAME, constantFields[0].getName());
        assertEquals(Constants.EXECUTOR_SERVICE_CONSTANT_NAME, constantFields[1].getName());
    }
}
