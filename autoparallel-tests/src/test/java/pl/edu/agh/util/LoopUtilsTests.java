package pl.edu.agh.util;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.generic.*;
import org.junit.Test;
import pl.edu.agh.transformations.util.LoopUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoopUtilsTests {

    private static final String TEST_2_CLASS_LOCATION = "src/test/resources/Test2.class";

    private static final int FIRST_ITEM_INDEX = 0;

    private static final int MAIN_METHOD_INDEX = 1;
    private static final int TRASH_METHOD_INDEX = 2;
    private static final int SUBTASK_METHOD_INDEX = 3;

    private static final int EXPECTED_LOOP_START_POSITION_MAIN_METHOD = 3;
    private static final int EXPECTED_LOOP_END_POSITION_MAIN_METHOD = 21;
    private static final int EXPECTED_LOOP_START_POSITION_SUBTASK_METHOD = 0;
    private static final int EXPECTED_LOOP_END_POSITION_SUBTASK_METHOD = 28;
    private static final int EXPECTED_LOOP_VARIABLE_INDEX = 2;
    private static final int EXPECTED_LOOP_START_CONDITION_INDEX = 99;

    @Test
    @SuppressWarnings("Duplicates")
    public void shouldGetLoopFromMainMethod() throws Exception {
        ClassGen testClass = new ClassGen(new ClassParser(TEST_2_CLASS_LOCATION).parse());
        MethodGen testMethod = new MethodGen(testClass.getMethodAt(MAIN_METHOD_INDEX), testClass.getClassName(), testClass.getConstantPool());
        List<InstructionHandle> forLoop = Arrays.asList(LoopUtils.getForLoop(testMethod));
        assertEquals(EXPECTED_LOOP_START_POSITION_MAIN_METHOD, getFirstInstructionPosition(forLoop));
        assertEquals(EXPECTED_LOOP_END_POSITION_MAIN_METHOD, getLastInstructionPosition(forLoop));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void shouldGetLoopFromSubTaskMethod() throws Exception {
        ClassGen testClass = new ClassGen(new ClassParser(TEST_2_CLASS_LOCATION).parse());
        MethodGen testMethod = new MethodGen(testClass.getMethodAt(SUBTASK_METHOD_INDEX), testClass.getClassName(), testClass.getConstantPool());
        List<InstructionHandle> forLoop = Arrays.asList(LoopUtils.getForLoop(testMethod));
        assertEquals(EXPECTED_LOOP_START_POSITION_SUBTASK_METHOD, getFirstInstructionPosition(forLoop));
        assertEquals(EXPECTED_LOOP_END_POSITION_SUBTASK_METHOD, getLastInstructionPosition(forLoop));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoLoopFound() throws Exception {
        ClassGen testClass = new ClassGen(new ClassParser(TEST_2_CLASS_LOCATION).parse());
        MethodGen testMethod = new MethodGen(testClass.getMethodAt(TRASH_METHOD_INDEX), testClass.getClassName(), testClass.getConstantPool());
        List<InstructionHandle> forLoop = Arrays.asList(LoopUtils.getForLoop(testMethod));
    }

    @Test
    public void shouldReturnTwoAsLoopVariableIndex() throws Exception {
        ClassGen testClass = new ClassGen(new ClassParser(TEST_2_CLASS_LOCATION).parse());
        MethodGen testMethod = new MethodGen(testClass.getMethodAt(MAIN_METHOD_INDEX), testClass.getClassName(), testClass.getConstantPool());
        InstructionHandle[] loopInstructions = LoopUtils.getForLoop(testMethod);
        int forLoopVariableIndex = LoopUtils.getForLoopVariableIndex(loopInstructions);
        assertEquals(EXPECTED_LOOP_VARIABLE_INDEX, forLoopVariableIndex);
    }

    @Test
    public void shouldUpdateLoopVariableTargetSlot() throws Exception {
        ClassGen testClass = new ClassGen(new ClassParser(TEST_2_CLASS_LOCATION).parse());
        MethodGen testMethod = new MethodGen(testClass.getMethodAt(MAIN_METHOD_INDEX), testClass.getClassName(), testClass.getConstantPool());
        InstructionHandle[] loopInstructions = LoopUtils.getForLoop(testMethod);
        int oldLoopVariableIndex = LoopUtils.getForLoopVariableIndex(loopInstructions);
        assertEquals(2, oldLoopVariableIndex);
        LoopUtils.updateLoopVariableIndex(loopInstructions, 1);
        int newLoopVariableIndex = LoopUtils.getForLoopVariableIndex(loopInstructions);
        assertEquals(1, newLoopVariableIndex);
    }

    @Test
    public void shouldChangeComparedVariableInLoop() throws Exception {
        ClassGen testClass = new ClassGen(new ClassParser(TEST_2_CLASS_LOCATION).parse());
        MethodGen testMethod = new MethodGen(testClass.getMethodAt(SUBTASK_METHOD_INDEX), testClass.getClassName(), testClass.getConstantPool());
        InstructionHandle[] loopInstructions = LoopUtils.getForLoop(testMethod);
        assertTrue(loopInstructions[0].getInstruction() instanceof ConstantPushInstruction);
        LoopUtils.updateLoopStartCondition(loopInstructions, EXPECTED_LOOP_START_CONDITION_INDEX);
        assertTrue(loopInstructions[0].getInstruction() instanceof LoadInstruction);
        int loadIndex = ((LoadInstruction) loopInstructions[0].getInstruction()).getIndex();
        assertEquals(EXPECTED_LOOP_START_CONDITION_INDEX, loadIndex);
    }

    private int getFirstInstructionPosition(List<InstructionHandle> forLoop) {
        return forLoop.get(FIRST_ITEM_INDEX).getPosition();
    }

    private int getLastInstructionPosition(List<InstructionHandle> forLoop) {
        return forLoop.get(forLoop.size() - 1).getPosition();
    }
}
