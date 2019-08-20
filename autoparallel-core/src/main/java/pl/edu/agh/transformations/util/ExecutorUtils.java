package pl.edu.agh.transformations.util;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;

public class ExecutorUtils {

    public static void addExecutorInvocation(ClassGen classGen, MethodGen methodGen) {
        ConstantPoolGen constantPool = classGen.getConstantPool();
        InstructionFactory instructionFactory = new InstructionFactory(classGen, constantPool);
        InstructionList allMethodInstructions = methodGen.getInstructionList();
        InstructionHandle[] forLoop = LoopUtils.getForLoop(methodGen);
        InstructionHandle lastLoopHandle = forLoop[forLoop.length - 1];
        int executorIndex = ConstantPoolUtils.getFieldIndex(classGen, Constants.EXECUTOR_SERVICE_CONSTANT_NAME);
        int tasksListIndex = LocalVariableUtils.findLocalVariableByName(Constants.TASK_POOL_NAME, methodGen.getLocalVariableTable(constantPool)).getIndex();
        InstructionList invokeInstructions = new InstructionList();
        invokeInstructions.append(new GETSTATIC(executorIndex));
        invokeInstructions.append(new ALOAD(tasksListIndex));
        invokeInstructions.append(instructionFactory.createInvoke("java/util/concurrent/ExecutorService",
                                                                  "invokeAll",
                                                                  Type.getType("Ljava/util/List;"),
                                                                  new Type[]{Type.getType("Ljava/util/Collection;")},
                                                                  Const.INVOKEINTERFACE));
        invokeInstructions.append(new POP());
        invokeInstructions.append(new GETSTATIC(executorIndex));
        invokeInstructions.append(instructionFactory.createInvoke("java/util/concurrent/ExecutorService",
                                                                  "shutdown",
                                                                  Type.VOID,
                                                                  new Type[]{},
                                                                  Const.INVOKEINTERFACE));
        allMethodInstructions.append(lastLoopHandle, invokeInstructions);

        methodGen.setMaxLocals();
        classGen.replaceMethod(methodGen.getMethod(), methodGen.getMethod());
    }
}
