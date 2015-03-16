package org.kurator.akka.actors;

import java.util.Map;

import org.kurator.akka.AkkaActor;
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
            "import sys"                                    + EOL +
            "def wrapper():"                                + EOL +
            "  global " + inputName                         + EOL +
            "  global " + outputName                        + EOL +
            "  " + outputName + " = %s(" + inputName + ")"  + EOL;

    private PythonInterpreter interpreter;
    private PyObject none;
    
    @Override
    protected void handleInitialize() {
        
        // create a python interpreter
        PySystemState.initialize(System.getProperties( ), null, new String[] {""});
        interpreter = new PythonInterpreter();
        
        // set output streams of interpreter to that set for this actor
        interpreter.setOut(super.outStream);
        interpreter.setErr(super.errStream);
        
        // expand wrapper function template using custom function name
        interpreter.exec(String.format(wrapperFormat, function));
        prependSysPath("kurator-jython");
        prependSysPath("../kurator-jython");
        prependSysPath("src/main/resources/org");
        
        // read the script into the interpreter
        if (path != null) {
            interpreter.execfile(path);
        }
        
        // cache a python None object
        none = interpreter.eval("None");
    }

    private void appendSysPath(String path) {
        interpreter.eval(String.format("sys.path.append('%s')%s", path, EOL));
    }

    private void prependSysPath(String path) {
        interpreter.eval(String.format("sys.path.insert(0, '%s')%s", path, EOL));
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
        broadcast(callJythonFunction(value));
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

    protected Object callJythonFunction(Object input) {
        
        // reset output variable to null
        interpreter.set(outputName, none);
        
        // stage input value
        interpreter.set(inputName, input);
        
        // call the python function
        interpreter.eval("wrapper()");
        
        // return the function output
        return interpreter.get(outputName, outputType);
    }
    
}
