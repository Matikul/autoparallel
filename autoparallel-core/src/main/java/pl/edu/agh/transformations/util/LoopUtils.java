package pl.edu.agh.transformations.util;

import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.StoreInstruction;

import java.util.Arrays;

public class LoopUtils {

    public static InstructionHandle[] getForLoop(MethodGen methodGen) {
        InstructionHandle gotoInstruction = getGoto(methodGen.getInstructionList().getInstructionHandles());
        int startPosition = ((BranchHandle) gotoInstruction).getTarget().getPrev().getPrev().getPosition();
        int endPosition = gotoInstruction.getPosition();
        return getInstructionsBetweenPositions(methodGen.getInstructionList().getInstructionHandles(), startPosition, endPosition);
    }

    private static InstructionHandle getGoto(InstructionHandle[] instructionHandles) {
        return Arrays.stream(instructionHandles)
                .filter(handle -> Constants.GOTO_INSTRUCTION_NAME.equals(handle.getInstruction().getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Method passed to 'getForLoop' does not have for loop."));
    }

    private static InstructionHandle[] getInstructionsBetweenPositions(InstructionHandle[] allInstructions, int start, int end) {
        return Arrays.stream(allInstructions)
                .filter(instr -> isBetweenPositions(instr, start, end))
                .toArray(InstructionHandle[]::new);
    }

    private static boolean isBetweenPositions(InstructionHandle instruction, int start, int end) {
        int instructionPosition = instruction.getPosition();
        return instructionPosition >= start && instructionPosition <= end;
    }

    public static int getForLoopVariableIndex(InstructionHandle[] loopInstructions) {
        return ((StoreInstruction) (loopInstructions[1].getInstruction())).getIndex();
    }

    public static void updateLoopVariableIndex(InstructionHandle[] loopInstructions, int newSlot) {
        StoreInstruction forLoopStoreInstruction = (StoreInstruction) loopInstructions[1].getInstruction();
        forLoopStoreInstruction.setIndex(newSlot);
    }
}
