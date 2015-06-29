package org.kurator.akka.actors;

public class PythonActor extends PythonActorBase {

    private static final String onDataWrapperFormat = 
            "def _call_ondata():"                           + EOL +
            "  global " + inputName                         + EOL +
            "  global " + outputName                        + EOL +
            "  " + outputName + " = %s(" + inputName + ")"  + EOL;

    private static final String onStartWrapperFormat = 
            "def _call_onstart():"                          + EOL +
            "  global " + outputName                        + EOL +
            "  " + outputName + " = %s()"                   + EOL;

//    private String isGeneratorFormat =
//            "import inspect"                                + EOL +
//            "__isgenerator__="                              +
//            "inspect.isgeneratorfunction(%s)"               + EOL;

    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        if (this.onStart != null) interpreter.exec(String.format(onStartWrapperFormat, onStart));
        if (this.onData != null) interpreter.exec(String.format(onDataWrapperFormat, onData));
        
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

        super.onStart();
        
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


}
