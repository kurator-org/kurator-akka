package org.kurator.akka.actors;

public class PythonClassActor extends PythonActor {

    public PythonClassActor() {
        functionQualifier = "_PYTHON_CLASS_INSTANCE_.";
    }

    @Override
    protected void onStart() throws Exception {
        interpreter.exec("_PYTHON_CLASS_INSTANCE_=" + pythonClass + "()");
        super.onStart();
    }
}
