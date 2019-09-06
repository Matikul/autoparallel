package pl.edu.agh.transformations.util;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.generic.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class TransformUtils {

    public static void addThreadPool(ClassGen classGen) {
        Optional<MethodGen> classInitMethod = MethodUtils.findMethodByName(classGen, Const.STATIC_INITIALIZER_NAME);
        ConstantPoolGen constantPoolGen = classGen.getConstantPool();
        addClassFields(classGen, constantPoolGen);
        InstructionList instructionList = new InstructionList();
        classInitMethod.ifPresent(init -> {
            instructionList.append(init.getInstructionList());
            try {
                instructionList.delete(instructionList.getEnd());
            } catch (TargetLostException e) {
                e.printStackTrace();
            }
            retargetStaticPuts(classGen, instructionList);
        });
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
        classGen.replaceMethod(methodGen.getMethod(), methodGen.getMethod());
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

    private static void retargetStaticPuts(ClassGen classGen, InstructionList instructionList) {
        int classNameIndex = classGen.getClassNameIndex();
        Arrays.stream(instructionList.getInstructionHandles())
                .filter(handle -> handle.getInstruction() instanceof PUTSTATIC)
                .forEach(handle -> retargetSingleHandle(classGen.getConstantPool(), handle, classNameIndex));
    }

    private static void retargetSingleHandle(ConstantPoolGen constantPool, InstructionHandle handle, int classNameIndex) {
        PUTSTATIC staticPut = (PUTSTATIC) handle.getInstruction();
        ConstantFieldref constant = (ConstantFieldref) constantPool.getConstant(staticPut.getIndex());
        constant.setClassIndex(classNameIndex);
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

    public static void addExecutorServiceInit(ClassGen classGen, MethodGen methodGen) {
        ConstantPoolGen constantPoolGen = classGen.getConstantPool();
        InstructionFactory instructionFactory = new InstructionFactory(classGen, constantPoolGen);
        InstructionList appendedInstructions = new InstructionList();
        appendedInstructions.append(instructionFactory.createGetStatic(classGen.getClassName(),
                                                                  Constants.NUMBER_OF_THREADS_CONSTANT_NAME,
                                                                  Type.INT));
        appendedInstructions.append(instructionFactory.createInvoke("java.util.concurrent.Executors",
                                                               "newFixedThreadPool",
                                                               Type.getType(ExecutorService.class),
                                                               new Type[]{Type.INT},
                                                               Const.INVOKESTATIC));
        appendedInstructions.append(instructionFactory.createPutStatic(classGen.getClassName(),
                                                                  Constants.EXECUTOR_SERVICE_CONSTANT_NAME,
                                                                  Type.getType(ExecutorService.class)));
        InstructionList instructionList = methodGen.getInstructionList();
        appendedInstructions.append(instructionList);
        methodGen.setInstructionList(appendedInstructions);
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
        updateMethodParametersScope(methodGen, constantPoolGen);
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

        subTaskInstructionList.append(new ICONST(1));
        subTaskInstructionList.append(InstructionFactory.createReturn(Type.INT));//TODO - would be nicer to get it by param from analyzer

        MethodGen subTaskMethod = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
                                                Type.INT,//TODO - would be nicer to get it by param from analyzer
                                                new Type[]{},
                                                new String[]{},
                                                Constants.SUBTASK_METHOD_NAME,
                                                classGen.getClassName(),
                                                subTaskInstructionList,
                                                classGen.getConstantPool());

        LocalVariableGen startVariable = subTaskMethod.addLocalVariable(Constants.START_INDEX_VARIABLE_NAME,
                                                                           Type.INT,
                                                                           //0,
                                                                           null, null);
        LocalVariableGen endVariable = subTaskMethod.addLocalVariable(Constants.END_INDEX_VARIABLE_NAME,
                                                                           Type.INT,
                                                                           //1,
                                                                           null, null);
        transferLocalVariables(methodGen, subTaskMethod);
        updateBranchInstructions(subTaskInstructionList);
        int newLoopIteratorVariableIndex = LocalVariableUtils.findLocalVariableByName(Constants.LOOP_ITERATOR_NAME, subTaskMethod.getLocalVariableTable(classGen.getConstantPool())).getIndex();
        LoopUtils.broadenCompareCondition(subTaskInstructionList.getInstructionHandles());
        LoopUtils.updateLoopVariableIndex(subTaskInstructionList.getInstructionHandles(), newLoopIteratorVariableIndex);
        LoopUtils.updateLoopStartCondition(subTaskInstructionList.getInstructionHandles(), startVariable.getIndex());
        LoopUtils.updateLoopEndCondition(subTaskInstructionList.getInstructionHandles(), endVariable.getIndex());
        subTaskMethod.setArgumentNames(new String[]{Constants.START_INDEX_VARIABLE_NAME, Constants.END_INDEX_VARIABLE_NAME});
        subTaskMethod.setArgumentTypes(new Type[]{Type.INT, Type.INT});
        subTaskMethod.setMaxLocals();
        subTaskMethod.setMaxStack();
        classGen.addMethod(subTaskMethod.getMethod());
        classGen.getConstantPool().addMethodref(subTaskMethod);
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

    private static void transferLocalVariables(MethodGen methodGen, MethodGen subTaskMethod) {
        InstructionList instructionList = subTaskMethod.getInstructionList();
        List<LocalVariableGen> variablesToCopy = getVariablesToCopy(methodGen, instructionList);
        variablesToCopy.forEach(variable -> subTaskMethod.addLocalVariable(variable.getName(),
                                                                           variable.getType(),
                                                                           gatHandleOnNewList(variable.getStart(), instructionList),
                                                                           gatHandleOnNewList(variable.getEnd(), instructionList)));
        Map<Integer, Integer> oldIndexesToNewIndexes = variablesToCopy.stream()
                .collect(Collectors.toMap(LocalVariableGen::getIndex, variable -> LocalVariableUtils.findLocalVariableByName(variable.getName(), subTaskMethod.getLocalVariableTable(methodGen.getConstantPool())).getIndex(), (a, b) -> a));
        updateVariableInstructions(variablesToCopy, subTaskMethod, oldIndexesToNewIndexes);
    }

    private static List<LocalVariableGen> getVariablesToCopy(MethodGen methodGen, InstructionList subTaskInstructionList) {
        List<Integer> variableIndexes = getVariableIndexes(subTaskInstructionList);
        return Arrays.stream(methodGen.getLocalVariables())
                .filter(variable -> variableIndexes.contains(variable.getIndex()))
                .collect(Collectors.toList());
    }

    private static List<Integer> getVariableIndexes(InstructionList subTaskInstructionList) {
        //we will not consider 4th instruction which is for(...; I < VAR;...) because VAR will be replaced
        InstructionHandle irrelevant = subTaskInstructionList.getInstructionHandles()[0].getNext().getNext().getNext();
        return Arrays.stream(subTaskInstructionList.getInstructionHandles())
                .filter(handle -> !handle.equals(irrelevant))
                .filter(handle -> handle.getInstruction() instanceof LoadInstruction)
                .map(handle -> (LoadInstruction)handle.getInstruction())
                .map(LoadInstruction::getIndex)
                .distinct()
                .collect(Collectors.toList());
    }

    private static InstructionHandle gatHandleOnNewList(InstructionHandle handle, InstructionList instructionList) {
        return Arrays.stream(instructionList.getInstructionHandles())
                .filter(listHandle -> handle.getInstruction().equals(listHandle.getInstruction()))
                .findFirst()
                .orElse(instructionList.getEnd());
    }

    private static void updateVariableInstructions(List<LocalVariableGen> variablesToCopy, MethodGen subTaskMethod, Map<Integer, Integer> oldIndexesToNewIndexes) {
        InstructionHandle[] instructionHandles = subTaskMethod.getInstructionList().getInstructionHandles();
        List<Integer> indexes = variablesToCopy.stream()
                .map(LocalVariableGen::getIndex)
                .collect(Collectors.toList());
        Arrays.stream(instructionHandles)
                .filter(handle -> handle.getInstruction() instanceof LocalVariableInstruction)
                .map(handle -> (LocalVariableInstruction)handle.getInstruction())
                .filter(instruction -> indexes.contains(instruction.getIndex()))
                .forEach(instruction -> instruction.setIndex(oldIndexesToNewIndexes.get(instruction.getIndex())));
    }

    private static void updateBranchInstructions(InstructionList instructions) {
        InstructionHandle returnHandle = instructions.getInstructionHandles()[instructions.getInstructionHandles().length - 2];
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
        int numThreadsConstantIndex = ConstantPoolUtils.getFieldIndex(classGen, Constants.NUMBER_OF_THREADS_CONSTANT_NAME);
        forLoop[3].setInstruction(new GETSTATIC(numThreadsConstantIndex));
        classGen.replaceMethod(methodGen.getMethod(), methodGen.getMethod());
    }

    public static void emptyMethodLoop(ClassGen classGen, MethodGen methodGen) {
        InstructionHandle[] forLoop = LoopUtils.getForLoop(methodGen);
        LoopUtils.emptyMethodLoop(methodGen, forLoop);
        classGen.replaceMethod(methodGen.getMethod(), methodGen.getMethod());
    }

    public static void setNewLoopBody(ClassGen modifiedClass, MethodGen methodGen) {
        InstructionList allInstructionsList = methodGen.getInstructionList();
        InstructionHandle[] loopHandles = LoopUtils.getForLoop(methodGen);
        InstructionHandle firstLoopInstruction = loopHandles[0];
        InstructionHandle lastInstructionBeforeLoopBody = loopHandles[4];
        InstructionHandle lastLoopInstruction = loopHandles[loopHandles.length - 1];
        methodGen.addLocalVariable(Constants.START_INDEX_VARIABLE_NAME,
                                   Type.INT,
                                   firstLoopInstruction,
                                   lastLoopInstruction);
        methodGen.addLocalVariable(Constants.END_INDEX_VARIABLE_NAME,
                                   Type.INT,
                                   firstLoopInstruction,
                                   lastLoopInstruction);
        InstructionList startInitInstructions = InstructionUtils.getStartInitInstructions(modifiedClass, methodGen);
        InstructionHandle endOfStartInit = startInitInstructions.getEnd();
        InstructionList endInitInstructions = InstructionUtils.getEndInitInstructions(modifiedClass, methodGen);
        allInstructionsList.append(lastInstructionBeforeLoopBody, startInitInstructions);
        allInstructionsList.append(endOfStartInit, endInitInstructions);
        methodGen.setMaxStack();
        modifiedClass.replaceMethod(methodGen.getMethod(), methodGen.getMethod());
    }
}
