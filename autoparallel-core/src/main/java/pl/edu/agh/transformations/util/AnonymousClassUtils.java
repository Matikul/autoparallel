package pl.edu.agh.transformations.util;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class AnonymousClassUtils {

    public static void addCallableCall(JavaClass analyzedClass, ClassGen classGen, MethodGen methodGen, String classPath) {
        InnerClassData innerClassData = addAnonymousClassConstants(classGen);
        redumpClassGen(classGen, classPath);

        addInnerClassAttribute(analyzedClass, classGen, innerClassData, classPath);
        InstructionList allMethodInstructions = methodGen.getInstructionList();
        InstructionHandle[] forLoop = LoopUtils.getForLoop(methodGen);
        InstructionHandle lastLoopHandle = forLoop[forLoop.length - 3];

        ConstantPoolGen constantPool = classGen.getConstantPool();
        LocalVariableTable localVariableTable = methodGen.getLocalVariableTable(constantPool);
        InstructionFactory instructionFactory = new InstructionFactory(classGen, constantPool);
        int tasksListIndex = LocalVariableUtils.findLocalVariableByName(Constants.TASK_POOL_NAME, localVariableTable).getIndex();
        int startIndex = LocalVariableUtils.findLocalVariableByName(Constants.START_INDEX_VARIABLE_NAME, localVariableTable).getIndex();
        int endIndex = LocalVariableUtils.findLocalVariableByName(Constants.END_INDEX_VARIABLE_NAME, localVariableTable).getIndex();

        InstructionList addedInstructionsList = new InstructionList();
        addedInstructionsList.append(new ALOAD(tasksListIndex));
        addedInstructionsList.append(new NEW(innerClassData.classIndex));
        addedInstructionsList.append(new DUP());
        addedInstructionsList.append(new ILOAD(startIndex));
        addedInstructionsList.append(new ILOAD(endIndex));
        addedInstructionsList.append(new INVOKESPECIAL(innerClassData.constructorIndex));
        addedInstructionsList.append(instructionFactory.createInvoke("java/util/List",
                                                                     "add",
                                                                     Type.BOOLEAN,
                                                                     new Type[] {Type.getType("Ljava/lang/Object;")},
                                                                     Const.INVOKEINTERFACE));
        addedInstructionsList.append(new POP());

        allMethodInstructions.append(lastLoopHandle, addedInstructionsList);

        methodGen.setMaxStack();
        classGen.replaceMethod(methodGen.getMethod(), methodGen.getMethod());
    }

    public static InnerClassData addAnonymousClassConstants(ClassGen classGen) {
        ConstantPoolGen constantPool = classGen.getConstantPool();
        String anonymousClassName = classGen.getClassName() + "$1";
        InnerClassData innerClassData = new InnerClassData();
        innerClassData.classIndex = constantPool.addClass(anonymousClassName);
        innerClassData.constructorIndex = constantPool.addMethodref(anonymousClassName, Const.CONSTRUCTOR_NAME, "(II)V");
        innerClassData.innerClassesNameIndex = constantPool.addUtf8("InnerClasses");
        return innerClassData;
    }

    private static void redumpClassGen(ClassGen classGen, String classPath) {
        try (FileOutputStream outputStream = new FileOutputStream(classPath + "\\" + classGen.getClassName() + ".class")) {
            classGen.getJavaClass().dump(outputStream);
        } catch (IOException exception) {
            throw new RuntimeException("Error during modified class save.", exception);
        }
    }

    private static void addInnerClassAttribute(JavaClass analyzedClass, ClassGen classGen, InnerClassData innerClassData, String classPath) {
        analyzedClass = classGen.getJavaClass();//TODO CHECK CORRECTNESS
        Attribute[] oldAttributes = analyzedClass.getAttributes();
        Attribute[] newAttributes = Arrays.copyOf(oldAttributes, oldAttributes.length + 1);
        String innerClassName = analyzedClass.getClassName() + "$1";
        InnerClass innerClass = new InnerClass(innerClassData.classIndex,
                                               39/*TODO TEMP*/,
                                               78/*ConstantPoolUtils.getInnerClassNameIndex(classGen, innerClassName)*/,
                                               0);
        InnerClasses innerClasses = new InnerClasses(innerClassData.innerClassesNameIndex, 10, new InnerClass[]{innerClass}, classGen.getConstantPool().getConstantPool());
        newAttributes[newAttributes.length - 1] = innerClasses;
        analyzedClass.setAttributes(newAttributes);

        //LOL, can break methodgen in method on top of this class
        redumpJavaClass(analyzedClass, classGen, classPath);
    }

    private static void redumpJavaClass(JavaClass analyzedClass, ClassGen classGen, String classPath) {
        classGen = new ClassGen(analyzedClass);
        try (FileOutputStream outputStream = new FileOutputStream(classPath + "\\" + analyzedClass.getClassName() + ".class")) {//TODO double "_modified"
            classGen.getJavaClass().dump(outputStream);
        } catch (IOException exception) {
            throw new RuntimeException("Error during modified class save.", exception);
        }
    }

    public static class InnerClassData {
        int classIndex;
        int constructorIndex;
        int innerClassesNameIndex;
    }
}
