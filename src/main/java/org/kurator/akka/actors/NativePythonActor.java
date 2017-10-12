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
        HashMap<String, Object> input = new HashMap<>(settings);

        //System.out.println("request: " + input);

        String onStart = (String) configuration.get("onStart");
        String onData = (String) configuration.get("onData");

        String code = (String)configuration.get("code");
        String module = (String) configuration.get("module");

        if (onStart != null) {
            Map<String, Object> response = new HashMap<>();

            if (code != null) {
                // inline python actor
                response = interpreter.eval(code, onStart, input);
            } else if (module != null) {
                // module python actor
                response = interpreter.run(module, onStart, input);
            }

            broadcastOutput(response);
        }

        if (onData == null) {
            endStreamAndStop();
        }
    }

    @Override
    public void onData(Object value) throws Exception {
        String onData = (String)configuration.get("onData");

        String code = (String)configuration.get("code");
        String module = (String) configuration.get("module");

        if (onData != null) {
            Object input = null;
            if (this.inputs.isEmpty()) {
                input = value;
            } else {
                input = mapInputs(value);
                ((Map) input).putAll(settings);
            }

            //System.out.println("request (" + module + "): " + input);

            Map<String, Object> response = new HashMap<>();

            if (code != null) {
                // inline python actor
                response = interpreter.eval(code, onData, (HashMap<String, Object>) input);
            } else if (module != null) {
                // module python actor
                //System.out.println("about to invoke: " + module);
                response = interpreter.run(module, onData, (HashMap<String, Object>) input);
            }

            broadcastOutput(response);
        }
    }

    private void broadcastOutput(Map<String, Object> output) {
        if (output != null) {
            // process the response, publish artifacts
            Map<String, String> artifacts = (Map<String, String>) output.get("artifacts");
            if (artifacts != null) {
                publishArtifacts(artifacts);
            }
        }

        //System.out.println("response: " + output);
        broadcast(output);
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
