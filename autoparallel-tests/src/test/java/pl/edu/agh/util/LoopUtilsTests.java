package pl.edu.agh.util;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LoopUtilsTests {

    private static final String TEST_2_CLASS_LOCATION = "src/test/resources/Test2.class";

    private static final int EXPECTED_FOR_LOOP_START_POSITION = 4;
    private static final int EXPECTED_FOR_LOOP_END_POSITION = 22;

    @Test
    public void getForLoopTest() throws Exception{
        ClassGen testClass = new ClassGen(new ClassParser(TEST_2_CLASS_LOCATION).parse());
        MethodGen testMethod = new MethodGen(testClass.getMethodAt(1), testClass.getClassName(), testClass.getConstantPool());
        List<InstructionHandle> forLoop = Arrays.asList(LoopUtils.getForLoop(testMethod));
        assertEquals(EXPECTED_FOR_LOOP_START_POSITION, forLoop.get(0).getPosition());
        assertEquals(EXPECTED_FOR_LOOP_END_POSITION, forLoop.get(forLoop.size() - 1).getPosition());
    }
}
