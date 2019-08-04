package pl.edu.agh.transformations.util;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ClassGen;

import java.util.Arrays;

class ConstantPoolUtils {

    static int getNumThreadsFieldIndex(ClassGen classGen) {
        ConstantPool constantPool = classGen.getConstantPool().getConstantPool();
        ConstantFieldref numThreadsField = Arrays.stream(constantPool.getConstantPool())
                .filter(ConstantFieldref.class::isInstance)
                .map(ConstantFieldref.class::cast)
                .filter((constant -> Constants.NUMBER_OF_THREADS_CONSTANT_NAME.equals(getConstantName(constantPool, constant))))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Wrong state - constant NUM_THREADS cannot be found."));
        for (int i = 1; i < constantPool.getConstantPool().length; i++) {
            if (constantPool.getConstantPool()[i].equals(numThreadsField)) {
                return i;
            }
        }
        return -1;
    }

    private static String getConstantName(ConstantPool constantPool, ConstantCP constant) {
        ConstantNameAndType constantNameAndType = (ConstantNameAndType) constantPool.getConstantPool()[constant.getNameAndTypeIndex()];
        return constantNameAndType.getName(constantPool);
    }

    static int getSubTaskMethodIndexInConstants(ClassGen modifiedClass) {
        ConstantPool constantPool = modifiedClass.getConstantPool().getConstantPool();
        ConstantMethodref subTaskMethod = Arrays.stream(constantPool.getConstantPool())
                .filter(ConstantMethodref.class::isInstance)
                .map(ConstantMethodref.class::cast)
                .filter(method -> Constants.SUBTASK_METHOD_NAME.equals(getConstantName(constantPool, method)))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No subTask method found."));
        for (int i = 1; i < constantPool.getConstantPool().length; i++) {
            if (constantPool.getConstantPool()[i].equals(subTaskMethod)) {
                return i;
            }
        }
        return -1;
    }
}
