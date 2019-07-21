package pl.edu.agh.transformations;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.generic.*;
import org.junit.Test;
import pl.edu.agh.transformations.util.Constants;
import pl.edu.agh.transformations.util.LoopUtils;
import pl.edu.agh.transformations.util.TransformUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransformUtilsTests {

    private static final String TEST_1_CLASS_LOCATION = "src/test/resources/Test1.class";
    private static final String TEST_3_CLASS_LOCATION = "src/test/resources/Test3.class";
    private static final String TEST_4_CLASS_LOCATION = "src/test/resources/Test4.class";

    private static final int NUM_THREADS_FIELD_POSITION = 8;

    private static final int EXPECTED_COUNT = 2;

    @Test
    public void shouldInjectThreadPoolToClass() throws Exception {
        ClassGen testClass = new ClassGen(new ClassParser(TEST_1_CLASS_LOCATION).parse());
        TransformUtils.addThreadPool(testClass);
        Field[] constantFields = testClass.getFields();
        assertEquals(EXPECTED_COUNT, constantFields.length);
        assertEquals(Constants.NUMBER_OF_THREADS_CONSTANT_NAME, constantFields[0].getName());
        assertEquals(Constants.EXECUTOR_SERVICE_CONSTANT_NAME, constantFields[1].getName());
    }

    @Test
    public void shouldInjectRangeField() throws Exception {
        ClassGen testClass = new ClassGen(new ClassParser(TEST_1_CLASS_LOCATION).parse());
        int initialFieldCount = testClass.getFields().length;
        TransformUtils.addRangeFields(testClass);
        assertEquals(initialFieldCount + 2, testClass.getFields().length);
        assertEquals(Constants.START_RANGE_CONSTANT_NAME, testClass.getFields()[initialFieldCount].getName());
        assertEquals(Constants.END_RANGE_CONSTANT_NAME, testClass.getFields()[initialFieldCount + 1].getName());
    }

    @Test
    public void shouldInjectTaskPoolToClass() throws Exception {
        ClassGen testClass = new ClassGen(new ClassParser(TEST_1_CLASS_LOCATION).parse());
        MethodGen testMethod = new MethodGen(testClass.getMethodAt(1), testClass.getClassName(), testClass.getConstantPool());
        TransformUtils.addTaskPool(testClass, testMethod);
        assertEquals(EXPECTED_COUNT, testMethod.getLocalVariables().length);
        assertEquals(Constants.TASK_POOL_NAME, testMethod.getLocalVariables()[1].getName());
        assertEquals(Type.getType(List.class), testMethod.getLocalVariables()[1].getType());
    }

    @Test
    public void shouldCopyLoopToMethod() throws Exception {
        ClassGen testClass = new ClassGen(new ClassParser(TEST_3_CLASS_LOCATION).parse());
        int expectedMethodCount = testClass.getMethods().length + 1;
        MethodGen testMethod = new MethodGen(testClass.getMethodAt(1), testClass.getClassName(), testClass.getConstantPool());
        TransformUtils.copyLoopToMethod(testClass, testMethod);
        assertEquals(expectedMethodCount, testClass.getMethods().length);
    }

    @Test
    public void shouldReplaceLoopIndexWithNumberOfThreads() throws Exception {
        ClassGen testClass = new ClassGen(new ClassParser(TEST_4_CLASS_LOCATION).parse());
        MethodGen testMethod = new MethodGen(testClass.getMethodAt(1), testClass.getClassName(), testClass.getConstantPool());
        TransformUtils.changeLoopLimitToNumberOfThreads(testClass, testMethod);
        InstructionHandle[] forLoop = LoopUtils.getForLoop(testMethod);
        assertTrue(forLoop[3].getInstruction() instanceof GETSTATIC);
        assertEquals(NUM_THREADS_FIELD_POSITION, ((GETSTATIC) (forLoop[3].getInstruction())).getIndex());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowWhenNoNumberOfThreadsFieldIsPresent() throws Exception {
        ClassGen testClass = new ClassGen(new ClassParser(TEST_3_CLASS_LOCATION).parse());
        MethodGen testMethod = new MethodGen(testClass.getMethodAt(1), testClass.getClassName(), testClass.getConstantPool());
        TransformUtils.changeLoopLimitToNumberOfThreads(testClass, testMethod);
    }
}
