package org.kurator.akka.actors;

public class JythonFunctionActor extends JythonActor {

    public String function = "function";
    
    private static final String wrapperFormat = 
            "def wrapper():"                                + EOL +
            "  global " + inputName                         + EOL +
            "  global " + outputName                        + EOL +
            "  " + outputName + " = %s(" + inputName + ")"  + EOL;
    
    @Override
    protected void handleInitialize() {
        super.handleInitialize();
        interpreter.exec(String.format(wrapperFormat, function));
    }
    
    @Override
    protected void handleStart() {
        super.handleStart();
    }

    @Override
    public void handleData(Object value) {  

        if (outputTypeIsInputType) {
            outputType = value.getClass();
        }

        Object output = callJythonFunction(value);
        if (output != null || broadcastNulls) {
            broadcast(output);
        }
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
