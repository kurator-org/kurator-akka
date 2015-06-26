package org.kurator.akka.actors;

public class JythonFunctionActor extends JythonActor {

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
    protected void onInitialize() {
        super.onInitialize();
        if (this.needsTrigger) {
            interpreter.exec(String.format(triggerWrapperFormat, onData));
        } else {
            interpreter.exec(String.format(callWrapperFormat, onData));
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onTrigger() throws Exception {
        Object output = triggerJythonFunction();
        handleOutput(output);
        endStreamAndStop();
    }
    
    @Override
    public void onData(Object value) {  

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
