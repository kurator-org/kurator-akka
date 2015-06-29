package org.kurator.akka.actors;

public class PythonClassActor extends PythonActor {

    public String pythonClass = null;

    public PythonClassActor() {
    
        onDataWrapperFormat = 
            "def _call_ondata():"                                  + EOL +
            "  global actor"                                        + EOL +
            "  global " + inputName                                 + EOL +
            "  global " + outputName                                + EOL +
            "  " + outputName                                       +
            " = _pythonClassInstance.%s(" + inputName + ")"         + EOL;
    }

    @Override
    protected void onStart() throws Exception {
        interpreter.exec("_pythonClassInstance=" + pythonClass + "()");
        super.onStart();
    }
}
