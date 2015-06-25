package org.kurator.akka.actors;

public class JythonFunctionActor extends JythonActor {

    public String function = "function";
    
    private static final String callWrapperFormat = 
            "def call_function():"                          + EOL +
            "  global " + inputName                         + EOL +
            "  global " + outputName                        + EOL +
            "  " + outputName + " = %s(" + inputName + ")"  + EOL;

    private static final String triggerWrapperFormat = 
            "def trigger_function():"                       + EOL +
            "  global " + outputName                        + EOL +
            "  " + outputName + " = %s()"                   + EOL;

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
        if (this.needsTrigger) {
            interpreter.exec(String.format(triggerWrapperFormat, function));
        } else {
            interpreter.exec(String.format(callWrapperFormat, function));
        }
    }
    
    @Override
    protected void handleStart() {
        super.handleStart();
    }

    @Override
    protected void handleTrigger() throws Exception {
        Object output = triggerJythonFunction();
        handleOutput(output);
        endStreamAndStop();
    }

    private void handleOutput(Object output) {
        if (output != null || broadcastNulls) {
            broadcast(output);
        }
    }
    
    @Override
    public void handleData(Object value) {  

        if (outputTypeIsInputType) {
            outputType = value.getClass();
        }

        Object output = callJythonFunction(value);

        handleOutput(output);
    }

    protected Object triggerJythonFunction() {
        
        // reset output variable to null
        interpreter.set(outputName, none);
        
        // call the python function
        interpreter.eval("trigger_function()");
        
        // return the function output
        return interpreter.get(outputName, outputType);
    }
    
    protected Object callJythonFunction(Object input) {
        
        // reset output variable to null
        interpreter.set(outputName, none);
        
        // stage input value
        interpreter.set(inputName, input);
        
        // call the python function
        interpreter.eval("call_function()");
        
        // return the function output
        return interpreter.get(outputName, outputType);
    }


}
