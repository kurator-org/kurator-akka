package org.kurator.akka.interpreters;

import java.util.HashMap;
import java.util.Map;

public class PythonInterpreter {
    static {
        System.loadLibrary("kurator"); // Load native library at runtime
    }

    public final synchronized native Map<String, Object> run(String name, String func, HashMap<String, Object> options);

    public final synchronized native Map<String, Object> eval(String code, String on_start, Map<String, Object> input);

    public static void main(String[] args) {
        PythonInterpreter interpreter = new PythonInterpreter();
        String code = "def on_start(options):\n    print 'Hello!'";

        interpreter.eval(code, "on_start", new HashMap<String, Object>());
    }
}