package pl.edu.agh.transformations;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.junit.Test;
import pl.edu.agh.transformations.util.Constants;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TransformUtilsTests {

    private static final String TEST_1_CLASS_LOCATION = "src/test/resources/Test1.class";

    @Test
    public void injectThreadPoolTest() throws Exception {
        ClassGen testClass = new ClassGen(new ClassParser(TEST_1_CLASS_LOCATION).parse());
        TransformUtils.addThreadPool(testClass);
        Field[] constantFields = testClass.getFields();
        assertEquals(2, constantFields.length);
        assertEquals(Constants.NUMBER_OF_THREADS_CONSTANT_NAME, constantFields[0].getName());
        assertEquals(Constants.EXECUTOR_SERVICE_CONSTANT_NAME, constantFields[1].getName());
    }

    @Test
    public void injectTaskPoolTest() throws Exception {
        ClassGen testClass = new ClassGen(new ClassParser(TEST_1_CLASS_LOCATION).parse());
        MethodGen testMethod = new MethodGen(testClass.getMethodAt(1), testClass.getClassName(), testClass.getConstantPool());
        TransformUtils.addTaskPool(testClass, testMethod);
        assertEquals(2, testMethod.getLocalVariables().length);
        assertEquals(Constants.TASK_POOL_NAME, testMethod.getLocalVariables()[1].getName());
        assertEquals(Type.getType(List.class), testMethod.getLocalVariables()[1].getType());
    }
}
