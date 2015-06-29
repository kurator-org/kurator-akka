package org.kurator.akka.actors;

public class PythonClassActor extends PythonActorBase {

    public String pythonClass = null;
    public String onData = null;
    
    private static final String onDataWrapperFormat = 
            "def _call_ondata_():"                                  + EOL +
            "  global actor"                                        + EOL +
            "  global " + inputName                                 + EOL +
            "  global " + outputName                                + EOL +
            "  " + outputName                                       +
            " = _pythonClassInstance.%s(" + inputName + ")"         + EOL;

    @Override
    protected void onInitialize() {
        super.onInitialize();
        interpreter.exec(String.format(onDataWrapperFormat, onData));
    }

    @Override
    protected void onStart() throws Exception {
        super.onStart();
        interpreter.exec("_pythonClassInstance=" + pythonClass + "()");
    }

    @Override
    public void onData(Object value) {

        if (outputTypeIsInputType) {
            outputType = value.getClass();
        }
        
        Object output = callOnData(value);
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
        interpreter.eval("_call_ondata_()");
        
        // return the function output
        return interpreter.get(outputName, outputType);
    }
    
}
