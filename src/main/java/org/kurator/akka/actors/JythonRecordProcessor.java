package org.kurator.akka.actors;

public class JythonRecordProcessor extends JythonFunctionActor {

    public JythonRecordProcessor() {
//        inputType = Integer.class;
//        outputType = HashMap.class;    
    }
    
    @Override
    public void handleData(Object value) {

        outputType = value.getClass();
        
        // call the function output
        Object output = super.callJythonFunction(value);

        // forward the result if not null
        if (output != null) {
            broadcast(output);
        }
    }
}
