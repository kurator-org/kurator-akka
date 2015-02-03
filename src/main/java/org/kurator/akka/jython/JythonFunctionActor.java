package org.kurator.akka.jython;

import org.kurator.akka.actors.AkkaActor;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class JythonFunctionActor extends AkkaActor {

    public Class<? extends Object> inputType = Integer.class;
    public Class<? extends Object> outputType = Integer.class;
    public String inputName = "input";
    public String outputName = "output";
    public String function = "function()";
    public String path = null;
    public String start = null;
    public String end = null;

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
        
        // cache a python None object
        none = interpreter.eval("None");
    }
    
    @Override
    protected void handleStart() {

        // call script start function if defined
        if (start != null) {
            interpreter.eval(start);
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
        interpreter.eval(function);
        
        // extract the function output
        Object output = interpreter.get(outputName, outputType);

        // send results to listeners
        broadcast(output);
    }
    
    @Override
    protected void handleEnd() {
        
        // call script end function if defined
        if (end != null) {
            interpreter.eval(end);
        }
        
        // shut down the interpreter
        interpreter.cleanup();
    }
    
}
