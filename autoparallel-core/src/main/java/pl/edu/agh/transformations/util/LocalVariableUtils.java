package pl.edu.agh.transformations.util;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;

import java.util.Arrays;

public class LocalVariableUtils {

    public static LocalVariable findLocalVariableByName(String name, LocalVariableTable localVariableTable) {
        return Arrays.stream(localVariableTable.getLocalVariableTable())
                .filter(var -> var.getName().equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Variable " + name + "not found by name."));
    }
}
