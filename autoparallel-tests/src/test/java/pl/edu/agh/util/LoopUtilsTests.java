package pl.edu.agh.util;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.junit.Test;
import pl.edu.agh.transformations.util.LoopUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LoopUtilsTests {

    private static final String TEST_2_CLASS_LOCATION = "src/test/resources/Test2.class";

    private static final int FIRST_ITEM_INDEX = 0;

    private static final int EXPECTED_LOOP_START_POSITION_MAIN_METHOD = 3;
    private static final int EXPECTED_LOOP_END_POSITION_MAIN_METHOD = 21;
    private static final int EXPECTED_LOOP_START_POSITION_SUBTASK_METHOD = 0;
    private static final int EXPECTED_LOOP_END_POSITION_SUBTASK_METHOD = 28;

    @Test
    public void shouldGetLoopFromMainMethod() throws Exception{
        ClassGen testClass = new ClassGen(new ClassParser(TEST_2_CLASS_LOCATION).parse());
        MethodGen testMethod = new MethodGen(testClass.getMethodAt(1), testClass.getClassName(), testClass.getConstantPool());
        List<InstructionHandle> forLoop = Arrays.asList(LoopUtils.getForLoop(testMethod));
        assertEquals(EXPECTED_LOOP_START_POSITION_MAIN_METHOD, getFirstInstructionPosition(forLoop));
        assertEquals(EXPECTED_LOOP_END_POSITION_MAIN_METHOD, getLastInstructionPosition(forLoop));
    }

    @Test
    public void shouldGetLoopFromSubtaskMethod() throws Exception{
        ClassGen testClass = new ClassGen(new ClassParser(TEST_2_CLASS_LOCATION).parse());
        MethodGen testMethod = new MethodGen(testClass.getMethodAt(3), testClass.getClassName(), testClass.getConstantPool());
        List<InstructionHandle> forLoop = Arrays.asList(LoopUtils.getForLoop(testMethod));
        assertEquals(EXPECTED_LOOP_START_POSITION_SUBTASK_METHOD, getFirstInstructionPosition(forLoop));
        assertEquals(EXPECTED_LOOP_END_POSITION_SUBTASK_METHOD, getLastInstructionPosition(forLoop));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoLoopFound() throws Exception{
        ClassGen testClass = new ClassGen(new ClassParser(TEST_2_CLASS_LOCATION).parse());
        MethodGen testMethod = new MethodGen(testClass.getMethodAt(2), testClass.getClassName(), testClass.getConstantPool());
        List<InstructionHandle> forLoop = Arrays.asList(LoopUtils.getForLoop(testMethod));
    }

    private int getFirstInstructionPosition(List<InstructionHandle> forLoop) {
        return forLoop.get(FIRST_ITEM_INDEX).getPosition();
    }

    private int getLastInstructionPosition(List<InstructionHandle> forLoop) {
        return forLoop.get(forLoop.size() - 1).getPosition();
    }
}
