package pl.edu.agh.transformations;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.util.Constants;
import pl.edu.agh.util.LoopUtils;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;

public class TransformUtils {

    /**
     * Method that adds static field with thread pool to the class
     * @param classGen
     */
    static void addThreadPool(ClassGen classGen) {
        ConstantPoolGen constantPoolGen = classGen.getConstantPool();
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
        InstructionList instructionList = new InstructionList();
        InstructionFactory instructionFactory = new InstructionFactory(classGen, constantPoolGen);
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
        instructionList.append(instructionFactory.createPutStatic(classGen.getClassName(),
                                                                  Constants.NUMBER_OF_THREADS_CONSTANT_NAME,
                                                                  Type.INT));
        instructionList.append(instructionFactory.createGetStatic(classGen.getClassName(),
                                                                  Constants.NUMBER_OF_THREADS_CONSTANT_NAME,
                                                                  Type.INT));
        instructionList.append(instructionFactory.createInvoke("java.util.concurrent.Executors",
                                                               "newFixedThreadPool",
                                                               Type.getType(ExecutorService.class),
                                                               new Type[]{Type.INT},
                                                               Const.INVOKESTATIC));
        instructionList.append(instructionFactory.createPutStatic(classGen.getClassName(),
                                                                  Constants.EXECUTOR_SERVICE_CONSTANT_NAME,
                                                                  Type.getType(ExecutorService.class)));
        instructionList.append(InstructionFactory.createReturn(Type.VOID));
        MethodGen methodGen = new MethodGen(Const.ACC_STATIC,
                                            Type.VOID,
                                            Type.NO_ARGS,
                                            new String[0],
                                            Const.STATIC_INITIALIZER_NAME,
                                            classGen.getClassName(),
                                            instructionList,
                                            constantPoolGen
        );
        methodGen.stripAttributes(true);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
    }

    /**
     * adds List<Callable> of tasks to method
     * @param classGen
     * @param methodGen
     */
    static void addTaskPool(ClassGen classGen, MethodGen methodGen) {
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

    /**
     * adds list of Futures to store partial results of parallelized method
     * @param classGen
     * @param methodGen
     */
    static void addFutureResultsList(ClassGen classGen, MethodGen methodGen) {
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
        MethodGen subTaskMethod = new MethodGen(Const.ACC_PRIVATE,
                                                Type.getType((new int[0]).getClass()),
                                                new Type[] {Type.INT, Type.INT},
                                                new String[] {Constants.START_INDEX_VARIABLE_NAME, Constants.END_INDEX_VARIABLE_NAME},
                                                Constants.SUBTASK_METHOD_NAME,
                                                classGen.getClassName(),
                                                subTaskInstructionList,
                                                classGen.getConstantPool());
        subTaskMethod.setMaxLocals();
        subTaskMethod.setMaxStack();
        classGen.addMethod(subTaskMethod.getMethod());
    }

    private static InstructionList getSubtaskInstructions(MethodGen methodGen) {
        InstructionHandle[] loopInstructions = LoopUtils.getForLoop(methodGen);
        InstructionList subTaskInstructionList = new InstructionList();
        Arrays.stream(loopInstructions)
                .forEach(instr -> subTaskInstructionList.append(instr, instr.getInstruction()));
        return subTaskInstructionList;
    }
}
