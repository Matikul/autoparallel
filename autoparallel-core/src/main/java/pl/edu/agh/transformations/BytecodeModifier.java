package pl.edu.agh.transformations;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import pl.edu.agh.transformations.util.TransformUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class BytecodeModifier {

    public static final String MODIFICATION_SUFFIX = "_modified";
    private static final String CLASS_SUFFIX = ".class";
    private static final String JAVA_SUFFIX = ".java";

    public void modifyBytecode(String classPath, String className, int methodPosition) throws IOException {
        JavaClass analyzedClass = new ClassParser(classPath + "\\" + className + CLASS_SUFFIX).parse();
        ClassGen modifiedClass = getModifiedClass(className, analyzedClass);
        copyMethods(analyzedClass, modifiedClass);

        Method transformedMethod = modifiedClass.getMethodAt(methodPosition);
        MethodGen methodGen = new MethodGen(transformedMethod, modifiedClass.getClassName(), modifiedClass.getConstantPool());

        TransformUtils.addThreadPool(modifiedClass);
        TransformUtils.addRangeFields(modifiedClass);
        TransformUtils.addTaskPool(modifiedClass, methodGen);
        TransformUtils.addFutureResultsList(modifiedClass, methodGen);
        TransformUtils.copyLoopToMethod(modifiedClass, methodGen);
        TransformUtils.changeLoopLimitToNumberOfThreads(modifiedClass, methodGen);
        TransformUtils.emptyMethodLoop(modifiedClass, methodGen);
        TransformUtils.setNewLoopBody(modifiedClass, methodGen);
        saveModifiedClass(classPath, className, modifiedClass);
    }

    private ClassGen getModifiedClass(String className, JavaClass analyzedClass) {
        ClassGen oldClass = new ClassGen(analyzedClass);
        return new ClassGen(analyzedClass.getPackageName() + className + MODIFICATION_SUFFIX,
                            Object.class.getName(),
                            className + MODIFICATION_SUFFIX + JAVA_SUFFIX,
                            Const.ACC_PUBLIC,
                            null,
                            oldClass.getConstantPool());
    }

    private void copyMethods(JavaClass oldClass, ClassGen newClass) {
        Arrays.stream(oldClass.getMethods())
                .forEach(newClass::addMethod);
    }

    private void saveModifiedClass(String classPath, String className, ClassGen classGen) {
        try (FileOutputStream outputStream = new FileOutputStream(classPath + "\\" + className + MODIFICATION_SUFFIX + CLASS_SUFFIX)) {
            classGen.getJavaClass().dump(outputStream);
        } catch (IOException exception) {
            throw new RuntimeException("Error during modified class save.", exception);
        }
    }
}
