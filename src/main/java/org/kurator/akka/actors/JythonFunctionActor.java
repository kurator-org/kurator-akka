package org.kurator.akka.actors;

import java.util.Map;

import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class JythonFunctionActor extends AkkaActor {

    public Class<? extends Object> inputType = Integer.class;
    public Class<? extends Object> outputType = Integer.class;
    public String function = "function";
    public String path = null;
    public String start = null;
    public String end = null;
    
    private static final String inputName = "_KURATOR_INPUT_";
    private static final String outputName = "_KURATOR_OUTPUT_";
    private static final String wrapperFormat = 
           "def wrapper():"                                 + EOL +
           "  global " + inputName                          + EOL +
           "  global " + outputName                         + EOL +
           "  " + outputName + " = %s(" + inputName + ")"   + EOL;

    private PythonInterpreter interpreter;
    private PyObject none;
    
    @Override
    protected void handleInitialize() {
        
        // create a python interpreter
        PySystemState.initialize();
        interpreter = new PythonInterpreter();
        
        // read the script into the interpreter
        if (path != null) {
            interpreter.execfile(path);
        }
        
        // set output streams of interpreter to that set for this actor
        interpreter.setOut(super.outStream);
        interpreter.setErr(super.errStream);
        
        // expand wrapper function template using custom function name
        String wrapper = String.format(wrapperFormat, function);
        interpreter.exec(wrapper);
        
        // cache a python None object
        none = interpreter.eval("None");
    }
    
    @Override
    protected void handleStart() {

        if (settings != null) {
            for(Map.Entry<String, Object> setting : settings.entrySet()) {
                String name = setting.getKey();
                Object value = setting.getValue();
                interpreter.set(name, value);
            }
        }
        
        // call script start function if defined
        if (start != null) {
            interpreter.eval(start + "()");
        }
    }

    @Override
    public void handleData(Object value) {
        
        // reset output variable to null
        interpreter.set(outputName, none);
        
        
        // stage input value
        Object input = inputType.cast(value);
        interpreter.set(inputName, input);
        
        // call the python function
        interpreter.eval("wrapper()");
        
        // extract the function output
        Object output = interpreter.get(outputName, outputType);

        // send results to listeners
        broadcast(output);
    }
    
    @Override
    protected void handleEnd() {
        
        // call script end function if defined
        if (end != null) {
            interpreter.eval(end + "()");
        }
        
        // shut down the interpreter
        interpreter.cleanup();
    }
    
}
