package pl.edu.agh.transformations.util;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.generic.FieldOrMethod;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

public class MethodUtils {

    public static Optional<MethodGen> findMethodByName(ClassGen classGen, String methodName) {
        return Arrays.stream(classGen.getMethods())
                .filter(method -> methodName.equals(method.getName()))
                .findFirst()
                .map(method -> new MethodGen(method, classGen.getClassName(), classGen.getConstantPool()));
    }

    public static MethodGen findMethodByNameOrThrow(ClassGen classGen, String methodName) {
        return Arrays.stream(classGen.getMethods())
                .filter(method -> methodName.equals(method.getName()))
                .findFirst()
                .map(method -> new MethodGen(method, classGen.getClassName(), classGen.getConstantPool()))
                .orElseThrow(() -> new IllegalStateException("Method with name " + methodName + " not found."));
    }

    public static void switchConstantRefsToNewClass(ClassGen classGen, Method method) {
        MethodGen methodGen = new MethodGen(method, classGen.getClassName(), classGen.getConstantPool());
        InstructionHandle[] instructionHandles = methodGen.getInstructionList().getInstructionHandles();
        Arrays.stream(instructionHandles)
                .filter(doesReferenceOldClass())
                .map(handle -> (FieldOrMethod) handle.getInstruction())
                .forEach(instr -> switchReference(classGen, instr));
    }

    private static Predicate<InstructionHandle> doesReferenceOldClass() {
        return handle -> handle.getInstruction() instanceof FieldOrMethod;
    }

    private static void switchReference(ClassGen classGen, FieldOrMethod instr) {
        ConstantPoolGen constantPool = classGen.getConstantPool();
        int classNameIndex = classGen.getClassNameIndex();
        ConstantCP fieldref = (ConstantCP) constantPool.getConstant(instr.getIndex());
        ConstantClass constantClass = (ConstantClass) constantPool.getConstant(fieldref.getClassIndex());
        String className = (String) constantClass.getConstantValue(constantPool.getConstantPool());
        if (classGen.getClassName().contains(className)) {
            fieldref.setClassIndex(classNameIndex);
        }
    }
}
