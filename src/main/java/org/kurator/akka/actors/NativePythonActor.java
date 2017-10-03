package org.kurator.akka.actors;

import org.kurator.akka.KuratorActor;
import org.kurator.akka.interpreters.PythonInterpreter;

import java.util.HashMap;
import java.util.Map;

public class NativePythonActor extends KuratorActor {
    private PythonInterpreter interpreter;

    @Override
    protected void onInitialize() throws Exception {
        interpreter = new PythonInterpreter();
    }

    @Override
    protected void onStart() throws Exception {
        String module = (String)configuration.get("module");
        String onStart = (String)configuration.get("onStart");
        String onData = (String)configuration.get("onData");

        Map<String, Object> input = new HashMap<>();

        if (onStart != null) {
            Map<String, Object> response = interpreter.run(module, onStart, (HashMap<String, Object>) input);
            broadcast(response);
        }

        if (onData == null) {
            endStreamAndStop();
        }

    }

    @Override
    public void onData(Object value) throws Exception {
        String module = (String)configuration.get("module");
        String onData = (String)configuration.get("onData");

        if (onData != null) {
            Object input = null;
            if (this.inputs.isEmpty()) {
                input = value;
            } else {
                input = mapInputs(value);
                ((Map) input).putAll(settings);
            }

            Map<String, Object> response = interpreter.run(module, onData, (HashMap<String, Object>) input);
            broadcast(response);
        }
    }

    private synchronized Map<String,Object> mapInputs(Object receivedValue) {

        Map<String,Object> mappedInputs = new HashMap<String,Object>();
        if (receivedValue instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String,Object> receivedValues = (Map<String,Object>)receivedValue;
            for (Map.Entry<String, String> mapEntry : this.inputs.entrySet()) {
                String incomingName = mapEntry.getKey();
                String localName = mapEntry.getValue();
                mappedInputs.put(localName, receivedValues.get(incomingName));
            }
        }

        return mappedInputs;
    }

}
