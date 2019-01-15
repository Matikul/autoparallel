package pl.edu.agh.transformations;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.util.Constants;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class TransformUtils {

    /*
    Method that adds static field with thread pool to the class
     */
    public static void addThreadPool(ClassGen classGen) {
        ConstantPoolGen constantPoolGen = classGen.getConstantPool();
        FieldGen threadCount = new FieldGen(Const.ACC_PUBLIC | Const.ACC_STATIC | Const.ACC_FINAL,
                Type.INT,
                Constants.NUMBER_OF_THREADS_CONSTANT_NAME,
                constantPoolGen);
        FieldGen service = new FieldGen(Const.ACC_PUBLIC,
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

    //adds List<Callable> of tasks to method
    public static void addTaskPool(ClassGen classGen, MethodGen methodGen) {
        ConstantPoolGen constantPoolGen = classGen.getConstantPool();
        InstructionFactory instructionFactory = new InstructionFactory(classGen, constantPoolGen);
        InstructionList appendedInstructions = new InstructionList();
//        LocalVariableGen taskVariable = new LocalVariableGen()
        appendedInstructions.append(instructionFactory.createNew(ObjectType.getInstance("java.util")));//TODO only java.util???
        appendedInstructions.append(InstructionFactory.createDup(1));
        appendedInstructions.append(instructionFactory.createInvoke("java.util.ArrayList<Callable<Integer>>",
                "<init>",
                Type.getType((new ArrayList<Callable<Integer>>()).getClass()),
                new Type[] {},
                Const.INVOKESPECIAL));
        appendedInstructions.append(InstructionFactory.createStore(Type.getType(ArrayList.class), methodGen.getLocalVariables().length));
        InstructionList currentList = methodGen.getInstructionList();
        appendedInstructions.append(currentList);
        methodGen.setInstructionList(appendedInstructions);
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        classGen.removeMethod(methodGen.getMethod());
        classGen.addMethod(methodGen.getMethod());
    }

    public static boolean hasLoop(MethodGen methodGen) {
        //TODO finding loops is  probably necessary
        return false;
    }
}
