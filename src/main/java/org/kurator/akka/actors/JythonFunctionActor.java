package org.kurator.akka.actors;

public class JythonFunctionActor extends JythonActor {

    private static final String onDataWrapperFormat = 
            "def call_function():"                          + EOL +
            "  global " + inputName                         + EOL +
            "  global " + outputName                        + EOL +
            "  " + outputName + " = %s(" + inputName + ")"  + EOL;

    private static final String onStartWrapperFormat = 
            "def start_function():"                         + EOL +
            "  global " + outputName                        + EOL +
            "  " + outputName + " = %s()"                   + EOL;

    @Override
    protected void onInitialize() {
        super.onInitialize();
        if (this.onStart != null) interpreter.exec(String.format(onStartWrapperFormat, onStart));
        if (this.onData != null) interpreter.exec(String.format(onDataWrapperFormat, onData));
    }
    
    @Override
    protected void onStart() throws Exception {

        super.onStart();
        
        if (onStart != null) {
            interpreter.set(outputName, none);
            interpreter.eval("start_function()");
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

        Object output = callJythonFunction(value);

        handleOutput(output);
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
