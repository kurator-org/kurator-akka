package org.kurator.akka.interpreters;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class PythonInterpreter {
    static {
        System.loadLibrary("kurator"); // Load native library at runtime
    }

    public final synchronized native Map<String, Object> run(String name, String func, HashMap<String, Object> options, String filename);

    public final synchronized native Map<String, Object> eval(String code, String on_start, Map<String, Object> input, String filename);

    public static void main(String[] args) throws IOException {
        PythonInterpreter interpreter = new PythonInterpreter();
        String code = "def on_start(options):\n    raise ValueError('you broke it')";
        File file = File.createTempFile("python_out", ".txt");
        System.out.println(file.getAbsolutePath());

        interpreter.eval(code, "on_start", new HashMap<String, Object>(), file.getAbsolutePath());
    }
}