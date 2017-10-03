package org.kurator.akka.actors;

import org.kurator.akka.KuratorActor;
import org.kurator.akka.interpreters.PythonInterpreter;
import org.kurator.akka.interpreters.RInterpreter;

public class NativeRActor extends KuratorActor {
    private RInterpreter interpreter;

    @Override
    protected void onInitialize() throws Exception {
        interpreter = new RInterpreter();
    }

    @Override
    protected void onStart() throws Exception {
        super.onStart();
    }

    @Override
    protected void onData(Object value) throws Exception {
        super.onData(value);
    }
}
