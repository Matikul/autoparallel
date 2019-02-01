package pl.edu.agh.transformations;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;

import java.io.FileOutputStream;
import java.io.IOException;

public class BytecodeModifier {

    public static final String MODIFICATION_SUFFIX = "_modified";
    private static final String CLASS_SUFFIX = ".class";

    public void modifyBytecode(String classPath, String className) throws IOException {
        JavaClass analyzedClass = new ClassParser(classPath + "\\" + className + CLASS_SUFFIX).parse();
        ClassGen classGen = new ClassGen(analyzedClass);

        //TODO main() is on the position 1 (default constructor is on position 0),
        //TODO to change other methods I need to change the way of calling TransformUtils
        MethodGen methodGen = new MethodGen(classGen.getMethodAt(1), classGen.getClassName(), classGen.getConstantPool());

        TransformUtils.addThreadPool(classGen);
        TransformUtils.addTaskPool(classGen, methodGen);
        TransformUtils.addFutureResultsList(classGen, methodGen);
        saveModifiedClass(classPath, className, classGen);
    }

    private void saveModifiedClass(String classPath, String className, ClassGen classGen) {
        try (FileOutputStream outputStream = new FileOutputStream(classPath + "\\" + className + MODIFICATION_SUFFIX + CLASS_SUFFIX)) {
            classGen.getJavaClass().dump(outputStream);
        } catch (IOException exception) {
            throw new RuntimeException("Error during modified class save.", exception);
        }
    }
}
