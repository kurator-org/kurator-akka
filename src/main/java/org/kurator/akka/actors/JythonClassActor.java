package org.kurator.akka.actors;

public class JythonClassActor extends JythonActor {

    public String actorName = null;
    public String method = null;
    
    private static final String wrapperFormat = 
            "def wrapper():"                                        + EOL +
            "  global actor"                                        + EOL +
            "  global " + inputName                                 + EOL +
            "  global " + outputName                                + EOL +
            "  " + outputName + " = actor.%s(" + inputName + ")"    + EOL;

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
        interpreter.exec(String.format(wrapperFormat, method));
    }

    @Override
    protected void handleStart() {
        super.handleStart();
        interpreter.exec("actor=" + actorName + "()");
    }

    @Override
    public void handleData(Object value) {

        if (outputTypeIsInputType) {
            outputType = value.getClass();
        }
        
        Object output = callActorMethod(value);
        if (output != null || broadcastNulls) {
            broadcast(output);
        }
    }
    
    protected Object callActorMethod(Object input) {
        
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
