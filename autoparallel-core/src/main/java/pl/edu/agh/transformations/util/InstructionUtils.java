package pl.edu.agh.transformations.util;

import org.apache.bcel.generic.*;

import java.util.Arrays;

class InstructionUtils {

    static InstructionHandle findByInstruction(Instruction instruction, InstructionHandle[] instructionHandles) {
        return Arrays.stream(instructionHandles)
                .filter(handle -> handle.getInstruction().equals(instruction))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No matching instruction found for instruction handle."));
    }

    static InstructionList getStartInitInstructions(ClassGen modifiedClass, MethodGen methodGen, short dataSize) {
        InstructionList list = new InstructionList();
        int loopIteratorIndex = LocalVariableUtils.findLocalVariableByName(Constants.LOOP_ITERATOR_NAME, methodGen.getLocalVariableTable(modifiedClass.getConstantPool())).getIndex();
        int startVarIndex = LocalVariableUtils.findLocalVariableByName(Constants.START_INDEX_VARIABLE_NAME, methodGen.getLocalVariableTable(modifiedClass.getConstantPool())).getIndex();
        int numThreadsFieldIndex = ConstantPoolUtils.getFieldIndex(modifiedClass, Constants.NUMBER_OF_THREADS_CONSTANT_NAME);
        list.append(new ILOAD(loopIteratorIndex));
        list.append(new SIPUSH(dataSize));//TODO would be nice to get rid of short
        list.append(new GETSTATIC(numThreadsFieldIndex));
        list.append(new IDIV());
        list.append(new IMUL());
        list.append(new ISTORE(startVarIndex));
        return list;
    }

    static InstructionList getEndInitInstructions(ClassGen modifiedClass, MethodGen methodGen, short dataSize) {
        InstructionList list = new InstructionList();
        int loopIteratorIndex = LocalVariableUtils.findLocalVariableByName(Constants.LOOP_ITERATOR_NAME, methodGen.getLocalVariableTable(modifiedClass.getConstantPool())).getIndex();
        int endVarIndex = LocalVariableUtils.findLocalVariableByName(Constants.END_INDEX_VARIABLE_NAME, methodGen.getLocalVariableTable(modifiedClass.getConstantPool())).getIndex();
        int numThreadsFieldIndex = ConstantPoolUtils.getFieldIndex(modifiedClass, Constants.NUMBER_OF_THREADS_CONSTANT_NAME);
        list.append(new ILOAD(loopIteratorIndex));
        list.append(new ICONST(1));
        list.append(new IADD());
        list.append(new SIPUSH(dataSize));//TODO would be nice to get rid of short
        list.append(new GETSTATIC(numThreadsFieldIndex));
        list.append(new IDIV());
        list.append(new IMUL());
        list.append(new ICONST(1));
        list.append(new ISUB());
        list.append(new ISTORE(endVarIndex));
        return list;
    }
}
