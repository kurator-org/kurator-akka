package org.kurator.akka;

import java.util.Map;
import java.util.Properties;

import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class PythonActor extends AkkaActor {
    
    public Class<? extends Object> inputType = Object.class;
    public Class<? extends Object> outputType = Object.class;
    public boolean broadcastNulls = false;
    public boolean outputTypeIsInputType = false;
    
    protected String functionQualifier = "";
    
    protected static final String inputName = "_KURATOR_INPUT_";
    protected static final String outputName = "_KURATOR_OUTPUT_";

    protected PythonInterpreter interpreter;
    protected PyObject none;
    
    protected String onDataWrapperFormat = 
            "def _call_ondata():"                               + EOL +
            "  global " + inputName                             + EOL +
            "  global " + outputName                            + EOL +
            "  " + outputName + " = %s%s(" + inputName + ")"    + EOL;

    private static final String onStartWrapperFormat = 
            "def _call_onstart():"                              + EOL +
            "  global " + outputName                            + EOL +
            "  " + outputName + " = %s%s()"                     + EOL;

//    private String isGeneratorFormat =
//            "import inspect"                                + EOL +
//            "__isgenerator__="                              +
//            "inspect.isgeneratorfunction(%s)"               + EOL;

    @Override
    protected void onInitialize() {
        
        Properties properties = System.getProperties();
        properties.put("python.import.site", "false");
                
        // create a python interpreter
        PySystemState.initialize(properties, null, new String[] {""});
        interpreter = new PythonInterpreter();
        
        // set output streams of interpreter to that set for this actor
        interpreter.setOut(super.outStream);
        interpreter.setErr(super.errStream);
        
        interpreter.exec("import sys");
        
        // expand wrapper function template using custom function name
        prependSysPath("src/main/resources/python");
        prependSysPath("kurator-jython");
        prependSysPath("../kurator-jython");
        
        // read the script into the interpreter
        if (script != null) interpreter.execfile(script);
        if (code != null) interpreter.exec(code);

        // cache a python None object
        none = interpreter.eval("None");
        
        if (this.onStart != null) interpreter.exec(String.format(onStartWrapperFormat, functionQualifier, onStart));
        if (this.onData != null) interpreter.exec(String.format(onDataWrapperFormat, functionQualifier, onData));
        
//        if (this.onData != null) {
//            onDataIsGenerator = isGenerator(onData);
//            interpreter.exec(String.format(onDataWrapperFormat, onData));
//        }
    }
    
//    private boolean isGenerator(String function) {
//        String s = String.format(isGeneratorFormat, function);
//        interpreter.exec(s);
//        Object result = interpreter.get("__isgenerator__");
//        return (boolean)result;
//    }
        
    @Override
    protected void onStart() throws Exception {

        if (settings != null) {
            for(Map.Entry<String, Object> setting : settings.entrySet()) {
                String name = setting.getKey();
                Object value = setting.getValue();
                interpreter.set(name, value);
            }
        }
        
        if (onStart != null) {
            interpreter.set(outputName, none);
            interpreter.eval("_call_onstart()");
            Object output = interpreter.get(outputName, outputType);
            handleOutput(output);
        }

        if (onData == null) {
            endStreamAndStop();       
        }
    }
    
    @Override
    public void onData(Object value) {  

        if (outputTypeIsInputType) {
            outputType = value.getClass();
        }

        Object output = callOnData(value);

        handleOutput(output);
    }

    @Override
    protected void onEnd() {
        
        // call script end function if defined
        if (onEnd != null) {
            interpreter.eval(onEnd + "()");
        }
        
        // shut down the interpreter
        interpreter.cleanup();
    }    

    protected void handleOutput(Object output) {
        if (output != null || broadcastNulls) {
            broadcast(output);
        }
    }
    
    protected Object callOnData(Object input) {
        
        // reset output variable to null
        interpreter.set(outputName, none);
        
        // stage input value
        interpreter.set(inputName, input);
        
        // call the python function
        interpreter.eval("_call_ondata()");
        
        // return the function output
        return interpreter.get(outputName, outputType);
    }
    
    private void prependSysPath(String path) {
        interpreter.eval(String.format("sys.path.insert(0, '%s')%s", path, EOL));
    }
}
