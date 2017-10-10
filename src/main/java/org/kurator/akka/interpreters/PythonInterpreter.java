package org.kurator.akka.interpreters;

import java.util.HashMap;
import java.util.Map;

public class PythonInterpreter {
    static {
        System.loadLibrary("kurator"); // Load native library at runtime
    }

    public final synchronized native Map<String, Object> run(String name, String func, HashMap<String, Object> options);


    public static void main(String[] args) {
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.run("test", "test", new HashMap<String, Object>());
    }
}