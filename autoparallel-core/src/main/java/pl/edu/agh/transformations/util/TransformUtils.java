package pl.edu.agh.transformations.util;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
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

    public static void addRangeFields(ClassGen classGen) {
        FieldGen startRangeField = new FieldGen(Const.ACC_PRIVATE | Const.ACC_STATIC,
                                                Type.INT,
                                                Constants.START_RANGE_CONSTANT_NAME,
                                                classGen.getConstantPool());
        FieldGen endRangeField = new FieldGen(Const.ACC_PRIVATE | Const.ACC_STATIC,
                                              Type.INT,
                                              Constants.END_RANGE_CONSTANT_NAME,
                                              classGen.getConstantPool());
        classGen.addField(startRangeField.getField());
        classGen.addField(endRangeField.getField());
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
        LoopUtils.updateLoopEndCondition(subTaskInstructionList.getInstructionHandles(), 2);
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
                subTaskInstructionList.append((BranchInstruction) branch.getInstruction().copy());
            } else {
                subTaskInstructionList.append(instr.getInstruction().copy());
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

    public static void changeLoopLimitToNumberOfThreads(ClassGen classGen, MethodGen methodGen) {
        InstructionHandle[] forLoop = LoopUtils.getForLoop(methodGen);
        int numThreadsConstantIndex = getNumThreadsFieldIndex(classGen);
        forLoop[3].setInstruction(new GETSTATIC(numThreadsConstantIndex));
        classGen.replaceMethod(methodGen.getMethod(), methodGen.getMethod());
    }

    private static int getNumThreadsFieldIndex(ClassGen classGen) {
        ConstantPool constantPool = classGen.getConstantPool().getConstantPool();
        ConstantFieldref numThreadsField = Arrays.stream(constantPool.getConstantPool())
                .filter(ConstantFieldref.class::isInstance)
                .map(ConstantFieldref.class::cast)
                .filter((constant -> "NUM_THREADS".equals(getConstantName(constantPool, constant))))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Wrong state - constant NUM_THREADS cannot be found."));
        for (int i = 1; i < constantPool.getConstantPool().length; i++) {
            if (constantPool.getConstantPool()[i].equals(numThreadsField)) {
                return i;
            }
        }
        return -1;
    }

    private static String getConstantName(ConstantPool constantPool, ConstantFieldref constant) {
        ConstantNameAndType constantNameAndType = (ConstantNameAndType) constantPool.getConstantPool()[constant.getNameAndTypeIndex()];
        return constantNameAndType.getName(constantPool);
    }

    public static void emptyMethodLoop(ClassGen classGen, MethodGen methodGen) {
        InstructionHandle[] forLoop = LoopUtils.getForLoop(methodGen);
        InstructionList modifiedInstructionList = new InstructionList();
        appendInstructionsUntilLoopStart(modifiedInstructionList, methodGen.getInstructionList().getInstructionHandles(), forLoop[0]);
        appendAll(modifiedInstructionList, LoopUtils.emptyLoop(forLoop));
        appendInstructionsAfterLoop(modifiedInstructionList, methodGen.getInstructionList().getInstructionHandles(), forLoop[forLoop.length - 1]);
        retargetEmptyLoopEndCondition(modifiedInstructionList);
        methodGen.setInstructionList(modifiedInstructionList);
        adjustLocalVariableTable(methodGen);
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        methodGen.removeLineNumbers();
        classGen.replaceMethod(methodGen.getMethod(), methodGen.getMethod());
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
        InstructionHandle start = findByInstruction(localVariableGen.getStart().getInstruction(), instructionHandles);
        InstructionHandle end = findByInstruction(localVariableGen.getEnd().getInstruction(), instructionHandles);
        localVariableGen.setStart(start);
        localVariableGen.setEnd(end);
    }

    private static InstructionHandle findByInstruction(Instruction instruction, InstructionHandle[] instructionHandles) {
        return Arrays.stream(instructionHandles)
                .filter(handle -> handle.getInstruction().equals(instruction))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No matching instruction found for instruction handle."));
    }
}
