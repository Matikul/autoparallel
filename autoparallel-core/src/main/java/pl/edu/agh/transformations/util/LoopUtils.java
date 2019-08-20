package pl.edu.agh.transformations.util;

import org.apache.bcel.generic.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoopUtils {

    private static final int START_CONDITION_INSTRUCTION_INDEX = 0;
    private static final int END_CONDITION_INSTRUCTION_INDEX = 3;

    public static InstructionHandle[] getForLoop(MethodGen methodGen) {
        InstructionHandle gotoInstruction = getGoto(methodGen.getInstructionList().getInstructionHandles());
        int startPosition = ((BranchHandle) gotoInstruction).getTarget().getPrev().getPrev().getPosition();
        int endPosition = gotoInstruction.getPosition();
        return getInstructionsBetweenPositions(methodGen.getInstructionList().getInstructionHandles(), startPosition, endPosition);
    }

    static InstructionHandle getGoto(InstructionHandle[] instructionHandles) {
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
        int oldSlot = forLoopStoreInstruction.getIndex();
        Arrays.stream(loopInstructions)
                .map(InstructionHandle::getInstruction)
                .forEach(instr -> updateSingleInstructionILoopVariableIndex(instr, oldSlot, newSlot));
    }

    private static void updateSingleInstructionILoopVariableIndex(Instruction instruction, int oldSlot, int newSlot) {
        if (instruction instanceof LocalVariableInstruction) {
            LocalVariableInstruction localVariableInstruction = (LocalVariableInstruction) instruction;
            if (localVariableInstruction.getIndex() == oldSlot) {
                localVariableInstruction.setIndex(newSlot);
            }
        }
    }

    static void broadenCompareCondition(InstructionHandle[] loopInstructions) {
        List<InstructionHandle> handles = Arrays.stream(loopInstructions)
                .filter(BranchHandle.class::isInstance)
                .filter(handle -> !(handle.getInstruction() instanceof GOTO))
                .collect(Collectors.toList());
        if (handles.size() != 1) {
            throw new IllegalStateException("Number of compare instructions in loop condition is different than 1.");
        }
        BranchHandle branchHandle = (BranchHandle) handles.get(0);
        BranchInstruction branchInstruction = (BranchInstruction) branchHandle.getInstruction();
        if (branchInstruction instanceof IF_ICMPGE) {
            branchHandle.setInstruction(new IF_ICMPGT(branchHandle.getTarget()));
        }
        if (branchInstruction instanceof IF_ICMPLE) {
            branchHandle.setInstruction(new IF_ICMPLT(branchHandle.getTarget()));
        }
    }

    public static void updateLoopStartCondition(InstructionHandle[] loopInstructions, int startVariableIndex) {
        loopInstructions[START_CONDITION_INSTRUCTION_INDEX].setInstruction(new ILOAD(startVariableIndex));
    }

    public static void updateLoopEndCondition(InstructionHandle[] loopInstructions, int endVariableIndex) {
        loopInstructions[END_CONDITION_INSTRUCTION_INDEX].setInstruction(new ILOAD(endVariableIndex));
    }

    public static InstructionHandle[] emptyLoop(InstructionHandle[] loopInstructions) {
        InstructionHandle[] conditionInstructions = Arrays.copyOf(loopInstructions, 5);
        InstructionHandle[] closingInstructions = Arrays.copyOfRange(loopInstructions, loopInstructions.length - 2, loopInstructions.length);
        return Stream.concat(Arrays.stream(conditionInstructions), Arrays.stream(closingInstructions))
                .toArray(InstructionHandle[]::new);
    }

    static void emptyMethodLoop(MethodGen methodGen, InstructionHandle[] forLoop) {
        InstructionList modifiedInstructionList = new InstructionList();
        appendInstructionsUntilLoopStart(modifiedInstructionList, methodGen.getInstructionList().getInstructionHandles(), forLoop[0]);
        appendAll(modifiedInstructionList, emptyLoop(forLoop));
        appendInstructionsAfterLoop(modifiedInstructionList, methodGen.getInstructionList().getInstructionHandles(), forLoop[forLoop.length - 1]);
        retargetEmptyLoopEndCondition(modifiedInstructionList);
        methodGen.setInstructionList(modifiedInstructionList);
        adjustLocalVariableTable(methodGen);
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        methodGen.removeLineNumbers();
    }

    private static void appendInstructionsUntilLoopStart(InstructionList instructionList, InstructionHandle[] allInstructions, InstructionHandle loopStartInstruction) {
        for (InstructionHandle handle : allInstructions) {
            if (handle == loopStartInstruction) {
                break;
            } else {
                appendSingle(instructionList, handle);
            }
        }
    }

    private static void appendSingle(InstructionList instructionList, InstructionHandle handle) {
        if (handle instanceof BranchHandle) {
            BranchHandle branch = (BranchHandle) handle;
            instructionList.append((BranchInstruction) branch.getInstruction().copy());
        } else {
            instructionList.append(handle.getInstruction().copy());
        }
    }

    private static void appendAll(InstructionList instructionList, InstructionHandle[] allInstructions) {
        for (InstructionHandle handle : allInstructions) {
            appendSingle(instructionList, handle);
        }
    }

    private static void appendInstructionsAfterLoop(InstructionList instructionList, InstructionHandle[] allInstructions, InstructionHandle loopEndInstruction) {
        int lastLoopInstructionPosition = Arrays.asList(allInstructions).indexOf(loopEndInstruction);
        if (lastLoopInstructionPosition == allInstructions.length - 1) {
            return;
        }
        for (int i = lastLoopInstructionPosition + 1; i < allInstructions.length; i++) {
            appendSingle(instructionList, allInstructions[i]);
        }
    }

    private static void retargetEmptyLoopEndCondition(InstructionList modifiedInstructionList) {
        InstructionHandle[] instructionHandles = modifiedInstructionList.getInstructionHandles();
        InstructionHandle gotoHandle = LoopUtils.getGoto(instructionHandles);
        InstructionHandle firstHandleAfterLoop = gotoHandle.getNext();
        InstructionHandle lastLoopHandle = gotoHandle.getPrev().getPrev();
        if (lastLoopHandle instanceof BranchHandle) {
            ((BranchHandle) lastLoopHandle).setTarget(firstHandleAfterLoop);
        } else {
            throw new IllegalStateException("Branch handle is not last instruction of for loop");
        }
    }

    private static void adjustLocalVariableTable(MethodGen methodGen) {
        InstructionHandle[] instructionHandles = methodGen.getInstructionList().getInstructionHandles();
        Arrays.stream(methodGen.getLocalVariables())
                .forEach(localVariableGen -> adjustLength(localVariableGen, instructionHandles));
    }

    private static void adjustLength(LocalVariableGen localVariableGen, InstructionHandle[] instructionHandles) {
        InstructionHandle start = InstructionUtils.findByInstruction(localVariableGen.getStart().getInstruction(), instructionHandles);
        InstructionHandle end = InstructionUtils.findByInstruction(localVariableGen.getEnd().getInstruction(), instructionHandles);
        localVariableGen.setStart(start);
        localVariableGen.setEnd(end);
    }

    public static void retargetLoopInInstructionsToFirstAfterLoop(MethodGen methodGen) {
        InstructionHandle[] forLoop = getForLoop(methodGen);
        InstructionHandle firstInstructionAfterLoop = getGoto(methodGen.getInstructionList().getInstructionHandles()).getNext();
        retargetLoopToInstruction(forLoop, firstInstructionAfterLoop);
    }

    private static void retargetLoopToInstruction(InstructionHandle[] forLoop, InstructionHandle firstInstructionAfterLoop) {
        Arrays.stream(forLoop)
                .filter(handle -> handle.getInstruction() instanceof IfInstruction)
                .map(BranchHandle.class::cast)
                .forEach(handle -> handle.setTarget(firstInstructionAfterLoop));
    }
}
