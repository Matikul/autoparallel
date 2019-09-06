package pl.edu.agh.transformations.util;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ClassGen;

import java.util.Arrays;
import java.util.Objects;

class ConstantPoolUtils {

    static int getFieldIndex(ClassGen classGen, String constantName) {
        ConstantPool constantPool = classGen.getConstantPool().getConstantPool();
        ConstantFieldref numThreadsField = Arrays.stream(constantPool.getConstantPool())
                .filter(ConstantFieldref.class::isInstance)
                .map(ConstantFieldref.class::cast)
                .filter((constant -> constantName.equals(getConstantName(constantPool, constant))))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Wrong state - constant " + constantName + " cannot be found."));
        for (int i = 1; i < constantPool.getConstantPool().length; i++) {
            if (Objects.equals(constantPool.getConstantPool()[i], numThreadsField)) {
                return i;
            }
        }
        return -1;
    }

    private static String getConstantName(ConstantPool constantPool, ConstantCP constant) {
        ConstantNameAndType constantNameAndType = (ConstantNameAndType) constantPool.getConstantPool()[constant.getNameAndTypeIndex()];
        return constantNameAndType.getName(constantPool);
    }

    static int getSubTaskMethodIndexInConstants(ClassGen classGen) {
        ConstantPool constantPool = classGen.getConstantPool().getConstantPool();
        ConstantMethodref subTaskMethod = Arrays.stream(constantPool.getConstantPool())
                .filter(ConstantMethodref.class::isInstance)
                .map(ConstantMethodref.class::cast)
                .filter(method -> Constants.SUBTASK_METHOD_NAME.equals(getConstantName(constantPool, method)))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No subTask method found."));
        for (int i = 1; i < constantPool.getConstantPool().length; i++) {
            if (Objects.equals(constantPool.getConstantPool()[i], subTaskMethod)) {
                return i;
            }
        }
        return -1;
    }

    static int getInnerClassNameIndex(ClassGen classGen, String innerClassName) {
//        ConstantPool constantPool = classGen.getConstantPool().getConstantPool();
//        Arrays.stream(constantPool.getConstantPool())
//                .filter()
        return 0;
    }
}
