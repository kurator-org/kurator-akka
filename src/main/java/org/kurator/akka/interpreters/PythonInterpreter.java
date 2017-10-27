package org.kurator.akka.interpreters;

import org.apache.commons.io.IOUtils;
import org.kurator.util.NativeUtil;

import java.io.*;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PythonInterpreter {

    static {
        NativeUtil.loadLibrary("kurator");
    }

    public final synchronized native Map<String, Object> run(String name, String func, HashMap<String, Object> options, Writer writer);

    public final synchronized native Map<String, Object> eval(String code, String on_start, Map<String, Object> input, Writer writer);

    public static void main(String[] args) throws IOException {
        PythonInterpreter interpreter = new PythonInterpreter();
        //String code = "def on_start(options):\n    raise ValueError()";
        String code = "def on_start(options):\n    print 'Hello'";
        File file = File.createTempFile("python_out", ".txt");
        System.out.println(file.getAbsolutePath());
        Writer writer = new StringWriter();
        interpreter.eval(code, "on_start", new HashMap<String, Object>(), writer);
        writer.flush();
        writer.close();
        System.out.println(writer.toString());
    }
}