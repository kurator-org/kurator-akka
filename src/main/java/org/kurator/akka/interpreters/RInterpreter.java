package org.kurator.akka.interpreters;

import org.kurator.util.NativeUtil;

import java.util.HashMap;
import java.util.Map;

public class RInterpreter {
    static {
        NativeUtil.loadLibrary("kurator"); // Load native library at runtime
    }

    public final native Map<String, Object> run(String name, String func, HashMap<String, Object> options);

    public static void main(String[] args) {
        RInterpreter interpreter = new RInterpreter();
        System.out.println(interpreter);
        interpreter.run("test", "test", new HashMap<String, Object>());
    }
}
