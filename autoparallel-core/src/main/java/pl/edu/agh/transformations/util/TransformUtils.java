package pl.edu.agh.transformations.util;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;

public class TransformUtils {

    public static void addThreadPool(ClassGen classGen) {
        ConstantPoolGen constantPoolGen = classGen.getConstantPool();
        addClassFields(classGen, constantPoolGen);
        InstructionList instructionList = new InstructionList();
        InstructionFactory instructionFactory = new InstructionFactory(classGen, constantPoolGen);
        String className = classGen.getClassName();
        appendFieldsInstructions(instructionList, instructionFactory, className);
        MethodGen methodGen = new MethodGen(Const.ACC_STATIC,
                                            Type.VOID,
                                            Type.NO_ARGS,
                                            new String[0],
                                            Const.STATIC_INITIALIZER_NAME,
                                            className,
                                            instructionList,
                                            constantPoolGen);
        methodGen.stripAttributes(true);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
    }

    private static void addClassFields(ClassGen classGen, ConstantPoolGen constantPoolGen) {
        FieldGen threadCount = new FieldGen(Const.ACC_PUBLIC | Const.ACC_STATIC | Const.ACC_FINAL,
                                            Type.INT,
                                            Constants.NUMBER_OF_THREADS_CONSTANT_NAME,
                                            constantPoolGen);
        FieldGen service = new FieldGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
                                        Type.getType(ExecutorService.class),
                                        Constants.EXECUTOR_SERVICE_CONSTANT_NAME,
                                        constantPoolGen);
        classGen.addField(threadCount.getField());
        classGen.addField(service.getField());
    }

    private static void appendFieldsInstructions(InstructionList instructionList,
                                                 InstructionFactory instructionFactory,
                                                 String className) {
        instructionList.append(instructionFactory.createInvoke("java.lang.Runtime",
                                                               "getRuntime",
                                                               Type.getType(Runtime.class),
                                                               Type.NO_ARGS,
                                                               Const.INVOKESTATIC));
        instructionList.append(instructionFactory.createInvoke("java.lang.Runtime",
                                                               "availableProcessors",
                                                               Type.INT,
                                                               Type.NO_ARGS,
                                                               Const.INVOKEVIRTUAL));
        instructionList.append(instructionFactory.createPutStatic(className,
                                                                  Constants.NUMBER_OF_THREADS_CONSTANT_NAME,
                                                                  Type.INT));
        instructionList.append(instructionFactory.createGetStatic(className,
                                                                  Constants.NUMBER_OF_THREADS_CONSTANT_NAME,
                                                                  Type.INT));
        instructionList.append(instructionFactory.createInvoke("java.util.concurrent.Executors",
                                                               "newFixedThreadPool",
                                                               Type.getType(ExecutorService.class),
                                                               new Type[]{Type.INT},
                                                               Const.INVOKESTATIC));
        instructionList.append(instructionFactory.createPutStatic(className,
                                                                  Constants.EXECUTOR_SERVICE_CONSTANT_NAME,
                                                                  Type.getType(ExecutorService.class)));
        instructionList.append(InstructionFactory.createReturn(Type.VOID));
    }

    public static void addTaskPool(ClassGen classGen, MethodGen methodGen) {
        ConstantPoolGen constantPoolGen = classGen.getConstantPool();
        InstructionFactory instructionFactory = new InstructionFactory(classGen, constantPoolGen);
        InstructionList appendedInstructions = new InstructionList();
        appendedInstructions.append(instructionFactory.createNew(ObjectType.getInstance("java.util.ArrayList")));
        appendedInstructions.append(InstructionFactory.createDup(1));
        appendedInstructions.append(instructionFactory.createInvoke("java.util.ArrayList",
                                                                    "<init>",
                                                                    Type.VOID,
                                                                    new Type[]{},
                                                                    Const.INVOKESPECIAL));
        appendedInstructions.append(InstructionFactory.createStore(Type.getType("Ljava/util/List;"), methodGen.getMaxLocals()));
        methodGen.addLocalVariable(Constants.TASK_POOL_NAME,
                                   Type.getType("Ljava/util/List;"),
                                   appendedInstructions.getEnd(),
                                   null);
        InstructionList currentList = methodGen.getInstructionList();
        appendedInstructions.append(currentList);
        methodGen.setInstructionList(appendedInstructions);
        updateMethodParametersScope(methodGen, constantPoolGen); //TODO method arguments are OK, but task pool scope is too low by 1
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        classGen.removeMethod(methodGen.getMethod());
        classGen.addMethod(methodGen.getMethod());
    }

    private static void updateMethodParametersScope(MethodGen methodGen, ConstantPoolGen constantPoolGen) {
        InstructionHandle startHandle = methodGen.getInstructionList().getInstructionHandles()[0];
        Arrays.stream(methodGen.getLocalVariables())
                .filter(var -> var.getLocalVariable(constantPoolGen).getStartPC() == 0)
                .forEach(var -> var.setStart(startHandle));
    }

    public static void addFutureResultsList(ClassGen classGen, MethodGen methodGen) {
        ConstantPoolGen constantPoolGen = classGen.getConstantPool();
        InstructionFactory instructionFactory = new InstructionFactory(classGen, constantPoolGen);
        InstructionList appendedInstructions = new InstructionList();
        appendedInstructions.append(instructionFactory.createNew(ObjectType.getInstance("java.util.ArrayList")));
        appendedInstructions.append(InstructionFactory.createDup(1));
        appendedInstructions.append(instructionFactory.createInvoke("java.util.ArrayList",
                                                                    "<init>",
                                                                    Type.VOID,
                                                                    new Type[]{},
                                                                    Const.INVOKESPECIAL));
        appendedInstructions.append(InstructionFactory.createStore(Type.getType("Ljava/util/List;"), methodGen.getMaxLocals()));
        methodGen.addLocalVariable(Constants.RESULTS_POOL_NAME,
                                   Type.getType("Ljava/util/List;"),
                                   appendedInstructions.getEnd(),
                                   null);
        InstructionList currentList = methodGen.getInstructionList();
        appendedInstructions.append(currentList);
        methodGen.setInstructionList(appendedInstructions);
        updateMethodParametersScope(methodGen, constantPoolGen);
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        classGen.removeMethod(methodGen.getMethod());
        classGen.addMethod(methodGen.getMethod());
    }

    public static void copyLoopToMethod(ClassGen classGen, MethodGen methodGen) {

        InstructionList subTaskInstructionList = getSubtaskInstructions(methodGen);
        subTaskInstructionList.append(InstructionFactory.createReturn(methodGen.getReturnType()));

        int previousLoopVariableSlot = ((StoreInstruction) (subTaskInstructionList.getInstructions()[1])).getIndex();
        LocalVariableGen loopVariable = methodGen.getLocalVariables()[previousLoopVariableSlot];

        MethodGen subTaskMethod = new MethodGen(Const.ACC_PRIVATE,
                                                methodGen.getReturnType(),
                                                new Type[]{},
                                                new String[]{},
                                                Constants.SUBTASK_METHOD_NAME,
                                                classGen.getClassName(),
                                                subTaskInstructionList,
                                                classGen.getConstantPool());
        subTaskMethod.addLocalVariable(Constants.START_INDEX_VARIABLE_NAME,
                                       Type.INT,
                                       1,
                                       null, null);
        subTaskMethod.addLocalVariable(Constants.END_INDEX_VARIABLE_NAME,
                                       Type.INT,
                                       2,
                                       null, null);
        updateBranchInstructions(subTaskInstructionList);
        int newLoopVariableSlot = 3;
        subTaskMethod.addLocalVariable(loopVariable.getName(),
                                       loopVariable.getType(),
                                       newLoopVariableSlot,
                                       null, null);
        LoopUtils.updateLoopVariableIndex(subTaskInstructionList.getInstructionHandles(), newLoopVariableSlot);
        LoopUtils.updateLoopStartCondition(subTaskInstructionList.getInstructionHandles(), 1);
        subTaskMethod.setArgumentNames(new String[]{Constants.START_INDEX_VARIABLE_NAME, Constants.END_INDEX_VARIABLE_NAME});
        subTaskMethod.setArgumentTypes(new Type[]{Type.INT, Type.INT});
        subTaskMethod.setMaxLocals(4);
        subTaskMethod.setMaxStack();
        classGen.addMethod(subTaskMethod.getMethod());
    }

    private static InstructionList getSubtaskInstructions(MethodGen methodGen) {
        InstructionHandle[] loopInstructions = LoopUtils.getForLoop(methodGen);
        InstructionList subTaskInstructionList = new InstructionList();
        for (InstructionHandle instr : loopInstructions) {
            if (instr instanceof BranchHandle) {
                BranchHandle branch = (BranchHandle) instr;
                subTaskInstructionList.append((BranchInstruction) branch.getInstruction());
            } else {
                subTaskInstructionList.append(instr.getInstruction());
            }
        }
        return subTaskInstructionList;
    }

    private static void updateBranchInstructions(InstructionList instructions) {
        InstructionHandle returnHandle = instructions.getInstructionHandles()[instructions.getInstructionHandles().length - 1];
        InstructionHandle loopBeginning = instructions.getInstructionHandles()[2];
        Arrays.stream(instructions.getInstructionHandles())
                .filter(BranchHandle.class::isInstance)
                .forEach(instr -> adjustInstructionTarget(instr, returnHandle, loopBeginning));
    }

    private static void adjustInstructionTarget(InstructionHandle instruction,
                                                InstructionHandle returnHandle,
                                                InstructionHandle loopBeginning) {
        short opCode = instruction.getInstruction().getOpcode();
        BranchHandle branchHandle = (BranchHandle) instruction;
        if (opCode == Const.GOTO) {
            branchHandle.setTarget(loopBeginning);
        } else {
            branchHandle.setTarget(returnHandle);
        }
    }
}
