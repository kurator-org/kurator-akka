package org.kurator.akka.interpreters;

import java.util.HashMap;
import java.util.Map;

public class RInterpreter {
    static {
        System.loadLibrary("kurator"); // Load native library at runtime
    }

    public final synchronized native Map<String, Object> run(String name, String func, HashMap<String, Object> options);
}
