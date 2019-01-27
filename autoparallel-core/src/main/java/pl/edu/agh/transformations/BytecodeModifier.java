package pl.edu.agh.transformations;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;

import java.io.FileOutputStream;
import java.io.IOException;

public class BytecodeModifier {

    public static final String MODIFICATION_SUFFIX = "_modified";
    private static final String CLASS_SUFFIX = ".class";

    public void modifyBytecode(String classPath, String className) throws IOException {
        JavaClass analyzedClass = new ClassParser(classPath + "\\" + className + CLASS_SUFFIX).parse();
        ClassGen classGen = new ClassGen(analyzedClass);
        TransformUtils.addThreadPool(classGen);
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
