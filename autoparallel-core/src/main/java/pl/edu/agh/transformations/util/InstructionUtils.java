package pl.edu.agh.transformations.util;

import org.apache.bcel.generic.*;

import java.util.Arrays;

class InstructionUtils {

    private static final short DATA_SIZE = 16;//TODO HARDCODED, AS SHORT - need to read it somehow/get by parameter

    static InstructionHandle findByInstruction(Instruction instruction, InstructionHandle[] instructionHandles) {
        return Arrays.stream(instructionHandles)
                .filter(handle -> handle.getInstruction().equals(instruction))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No matching instruction found for instruction handle."));
    }

    static InstructionList getStartInitInstructions(ClassGen modifiedClass, MethodGen methodGen) {
        InstructionList list = new InstructionList();
        int loopIteratorIndex = LocalVariableUtils.findLocalVariableByName(Constants.LOOP_ITERATOR_NAME, methodGen.getLocalVariableTable(modifiedClass.getConstantPool())).getIndex();
        int startVarIndex = LocalVariableUtils.findLocalVariableByName(Constants.START_INDEX_VARIABLE_NAME, methodGen.getLocalVariableTable(modifiedClass.getConstantPool())).getIndex();
        int numThreadsFieldIndex = ConstantPoolUtils.getNumThreadsFieldIndex(modifiedClass);
        list.append(new ILOAD(loopIteratorIndex));
        list.append(new SIPUSH(DATA_SIZE));
        list.append(new GETSTATIC(numThreadsFieldIndex));
        list.append(new IDIV());
        list.append(new IMUL());
        list.append(new ISTORE(startVarIndex));
        return list;
    }

    static InstructionList getEndInitInstructions(ClassGen modifiedClass, MethodGen methodGen) {
        InstructionList list = new InstructionList();
        int loopIteratorIndex = LocalVariableUtils.findLocalVariableByName(Constants.LOOP_ITERATOR_NAME, methodGen.getLocalVariableTable(modifiedClass.getConstantPool())).getIndex();
        int endVarIndex = LocalVariableUtils.findLocalVariableByName(Constants.END_INDEX_VARIABLE_NAME, methodGen.getLocalVariableTable(modifiedClass.getConstantPool())).getIndex();
        int numThreadsFieldIndex = ConstantPoolUtils.getNumThreadsFieldIndex(modifiedClass);
        list.append(new ILOAD(loopIteratorIndex));
        list.append(new ICONST(1));
        list.append(new IADD());
        list.append(new SIPUSH(DATA_SIZE));
        list.append(new GETSTATIC(numThreadsFieldIndex));
        list.append(new IDIV());
        list.append(new IMUL());
        list.append(new ICONST(1));
        list.append(new ISUB());
        list.append(new ISTORE(endVarIndex));
        return list;
    }

    static InstructionList getSubtaskCallInstructions(ClassGen modifiedClass, MethodGen methodGen) {
        InstructionList list = new InstructionList();
        int startVarIndex = LocalVariableUtils.findLocalVariableByName(Constants.START_INDEX_VARIABLE_NAME, methodGen.getLocalVariableTable(modifiedClass.getConstantPool())).getIndex();
        int endVarIndex = LocalVariableUtils.findLocalVariableByName(Constants.END_INDEX_VARIABLE_NAME, methodGen.getLocalVariableTable(modifiedClass.getConstantPool())).getIndex();
        int subTaskMethodIndex = ConstantPoolUtils.getSubTaskMethodIndexInConstants(modifiedClass);
        list.append(new ILOAD(startVarIndex));
        list.append(new ILOAD(endVarIndex));
        list.append(new INVOKESTATIC(subTaskMethodIndex));
        return list;
    }
}
