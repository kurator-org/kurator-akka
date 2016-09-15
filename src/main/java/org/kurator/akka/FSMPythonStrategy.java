package org.kurator.akka;

import org.kurator.FSMActorStrategy;
import org.kurator.util.SystemClasspathManager;
import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Python actor FSM strategy class. Based on PythonActor
 */
public class FSMPythonStrategy extends FSMActorStrategy {

    protected static String DEFAULT_ON_INIT    = "on_init";
    protected static String DEFAULT_ON_START   = "on_start";
    protected static String DEFAULT_ON_DATA    = "on_data";
    protected static String DEFAULT_ON_END     = "on_end";

    public Class<? extends Object> inputType = Object.class;
    public Class<? extends Object> outputType = Object.class;
    public boolean broadcastNulls = false;
    public boolean outputTypeIsInputType = false;
    protected boolean usingPythonModule = false;

    protected String functionQualifier = "";

    protected String onInit = null;
    protected String onStart = null;
    protected String onData = null;
    protected String onEnd = null;

    protected PythonInterpreter interpreter;
    protected PyObject none;

    private static final String commonScriptHeader =
            "_KURATOR_INPUT_=None"                          + EOL +
                    "_KURATOR_RESULT_=None"                         + EOL +
                    "_KURATOR_OUTPUT_=None"                         + EOL +
                    "_KURATOR_MORE_DATA_=False"                     + EOL +
                    ""                                              + EOL +
                    "import types"                                  + EOL +
                    "import inspect"                                + EOL +
                    ""                                              + EOL +
                    "def _is_generator(g):"                         + EOL +
                    "  return isinstance(g, types.GeneratorType)"   + EOL +
                    ""                                              + EOL +
                    "def _is_global_function(f):"                   + EOL +
                    "  return f in globals() and inspect.isfunction(globals()[f])"   + EOL +
                    ""                                              + EOL +
                    "def _function_arg_count(f):"                   + EOL +
                    "  return len(inspect.getargspec(f)[0])"        + EOL +
                    ""                                              + EOL +
                    "def _get_next_data():"                         + EOL +
                    "  global _KURATOR_OUTPUT_"                     + EOL +
                    "  global _KURATOR_RESULT_"                     + EOL +
                    "  global _KURATOR_MORE_DATA_"                  + EOL +
                    "  try:"                                        + EOL +
                    "    _KURATOR_OUTPUT_=_KURATOR_RESULT_.next()"  + EOL +
                    "    return"                                    + EOL +
                    "  except StopIteration:"                       + EOL +
                    "    _KURATOR_OUTPUT_=None"                     + EOL +
                    "    _KURATOR_MORE_DATA_=False"                 + EOL;

    private static final String statelessOnInitWrapperTemplate =
            "def _call_oninit():"                           + EOL +
                    "  %s%s()"                                      + EOL;

    private static final String statefulOnInitWrapperTemplate =
            "def _call_oninit():"                           + EOL +
                    "  global _KURATOR_STATE_"                      + EOL +
                    "  %s%s(_KURATOR_STATE_)"                       + EOL;

    private static final String statelessOnStartWrapperTemplate =
            "def _call_onstart():"                          + EOL +
                    "  global _KURATOR_OUTPUT_"                     + EOL +
                    "  global _KURATOR_RESULT_"                     + EOL +
                    "  global _KURATOR_MORE_DATA_"                  + EOL +
                    "  _KURATOR_RESULT_ = %s%s()"                   + EOL +
                    "  if _is_generator(_KURATOR_RESULT_):"         + EOL +
                    "    _KURATOR_MORE_DATA_=True"                  + EOL +
                    "  else:"                                       + EOL +
                    "    _KURATOR_MORE_DATA_=False"                 + EOL +
                    "    _KURATOR_OUTPUT_=_KURATOR_RESULT_"         + EOL;

    private static final String statefulOnStartWrapperTemplate =
            "def _call_onstart():"                          + EOL +
                    "  global _KURATOR_STATE_"                      + EOL +
                    "  global _KURATOR_OUTPUT_"                     + EOL +
                    "  global _KURATOR_RESULT_"                     + EOL +
                    "  global _KURATOR_MORE_DATA_"                  + EOL +
                    "  _KURATOR_RESULT_ = %s%s(_KURATOR_STATE_)"    + EOL +
                    "  if _is_generator(_KURATOR_RESULT_):"         + EOL +
                    "    _KURATOR_MORE_DATA_=True"                  + EOL +
                    "  else:"                                       + EOL +
                    "    _KURATOR_MORE_DATA_=False"                 + EOL +
                    "    _KURATOR_OUTPUT_=_KURATOR_RESULT_"         + EOL;

    private static final String statelessOnDataWrapperTemplate =
            "def _call_ondata():"                           + EOL +
                    "  global _KURATOR_INPUT_"                      + EOL +
                    "  global _KURATOR_OUTPUT_"                     + EOL +
                    "  global _KURATOR_RESULT_"                     + EOL +
                    "  global _KURATOR_MORE_DATA_"                  + EOL +
                    "  _KURATOR_RESULT_ = %s%s(_KURATOR_INPUT_)"    + EOL +
                    "  if _is_generator(_KURATOR_RESULT_):"         + EOL +
                    "    _KURATOR_MORE_DATA_=True"                  + EOL +
                    "  else:"                                       + EOL +
                    "    _KURATOR_MORE_DATA_=False"                 + EOL +
                    "    _KURATOR_OUTPUT_=_KURATOR_RESULT_"         + EOL;

    private static final String statefulOnDataWrapperTemplate =
            "def _call_ondata():"                           + EOL +
                    "  global _KURATOR_STATE_"                      + EOL +
                    "  global _KURATOR_INPUT_"                      + EOL +
                    "  global _KURATOR_OUTPUT_"                     + EOL +
                    "  global _KURATOR_RESULT_"                     + EOL +
                    "  global _KURATOR_MORE_DATA_"                  + EOL +
                    "  _KURATOR_RESULT_ = %s%s(_KURATOR_INPUT_,"    + EOL +
                    "                          _KURATOR_STATE_)"    + EOL +
                    "  if _is_generator(_KURATOR_RESULT_):"         + EOL +
                    "    _KURATOR_MORE_DATA_=True"                  + EOL +
                    "  else:"                                       + EOL +
                    "    _KURATOR_MORE_DATA_=False"                 + EOL +
                    "    _KURATOR_OUTPUT_=_KURATOR_RESULT_"         + EOL;

    private static final String onEndWrapperTemplate =
            "def _call_onend():"                            + EOL +
                    "  %s%s()"                                      + EOL;

    private static final String statefulOnEndWrapperTemplate =
            "def _call_onend():"                            + EOL +
                    "  global _KURATOR_STATE_"                      + EOL +
                    "  %s%s(_KURATOR_STATE_)"                       + EOL;

    @Override
    public void onInitialize() throws Exception {
        initializeJythonInterpreter();
        loadCommonHelperFunctions();

        loadCustomCode();
        configureCustomCode();

        onInit = loadEventHandler("onInit", DEFAULT_ON_INIT, 0, statelessOnInitWrapperTemplate, statefulOnInitWrapperTemplate);
        onStart = loadEventHandler("onStart", DEFAULT_ON_START, 0, statelessOnStartWrapperTemplate, statefulOnStartWrapperTemplate);
        onData = loadEventHandler("onData", DEFAULT_ON_DATA, 1, statelessOnDataWrapperTemplate, statefulOnDataWrapperTemplate);
        onEnd = loadEventHandler("onEnd", DEFAULT_ON_END, 0, onEndWrapperTemplate, statefulOnEndWrapperTemplate);

        Map<String,Object> settings = initializeState();

        // TODO: Move this PythonClassActor.onInitialize()
        applySettings();

        if (onInit != null) {
            interpreter.set("_KURATOR_STATE_", settings);
            interpreter.eval("_call_oninit()");
        }
    }

    // TODO: Move this PythonClassActor.onInitialize()
    private synchronized Map<String,Object> initializeState() {
        Map<String,Object> state = new HashMap<String,Object>();
        for(Map.Entry<String, Object> setting : settings.entrySet()) {
            String name = setting.getKey();
            Object value = setting.getValue();
            state.put(name, value);
        }
        return state;
    }

    protected void configureCustomCode() throws Exception {
    }

    protected void loadCommonHelperFunctions() {
        interpreter.exec(commonScriptHeader);
    }

    protected synchronized void loadCustomCode() throws Exception {

        // read the script into the interpreter
        interpreter.set("__name__",  "__kurator_actor__");
        String script = (String)configuration.get("script");
        if (script != null) interpreter.execfile(script);

        String code = (String)configuration.get("code");
        if (code != null) interpreter.exec(code);

        String moduleConfig = (String)configuration.get("module");
        if (moduleConfig != null) {
            usingPythonModule = true;
            try {
                interpreter.exec("import " + moduleConfig);
            } catch (PyException e) {
                System.out.println("Error: "+  e);
                throw new Exception("Error importing Python module '" +
                        moduleConfig + "': " + e.value);
            }

            String modulePath = interpreter.eval(moduleConfig + ".__file__").toString();
            logger.info("Actor " + this.name + " imported module " + moduleConfig + " from " + modulePath);
            functionQualifier = moduleConfig + ".";
        }
    }

    private synchronized void initializeJythonInterpreter() throws Exception {

        interpreter = new PythonInterpreter(null, new PySystemState());

        interpreter.setOut(super.outStream);
        interpreter.setErr(super.errStream);

        interpreter.exec("from org import python");
        interpreter.exec("import sys");
        interpreter.exec("import types");
        interpreter.exec("import inspect");

        // cache a python None object
        none = interpreter.eval("None");
    }

    private Boolean isGlobalFunction(String f) {
        PyBoolean result = (PyBoolean)interpreter.eval("_is_global_function('" + f + "')");
        return result.getBooleanValue();
    }

    protected Integer getArgCount(String f) {
        PyInteger result = (PyInteger)interpreter.eval("_function_arg_count(" + f + ")");
        return result.asInt();
    }

    protected synchronized String loadEventHandler(String handlerName, String defaultMethodName, int minArgumentCount,
                                                   String statelessWrapperTemplate, String statefulWrapperTemplate) throws Exception {

        if (usingPythonModule) {
            return loadEventHandlerModuleFunction(handlerName, defaultMethodName, minArgumentCount,
                    statelessWrapperTemplate, statefulWrapperTemplate);
        } else {
            return loadEventHandlerLocalFunction(handlerName, defaultMethodName, minArgumentCount,
                    statelessWrapperTemplate, statefulWrapperTemplate);
        }
    }

    private synchronized String loadEventHandlerLocalFunction(String handlerName, String defaultMethodName, int minArgumentCount,
                                                              String statelessWrapperTemplate, String statefulWrapperTemplate) throws Exception {

        String actualMethodName = null;

        String customMethodName = (String)configuration.get(handlerName);
        if (customMethodName != null) {
            if (!isGlobalFunction(customMethodName)) {
                throw new Exception("Custom " + handlerName + " handler '" + customMethodName +
                        "' not defined for actor '" + name + "'");
            }
            actualMethodName = customMethodName;
        } else if (isGlobalFunction(defaultMethodName)) {
            actualMethodName = defaultMethodName;
        }

        if (actualMethodName != null) {

            int argCount = getArgCount(actualMethodName);

            if (argCount == minArgumentCount) {
                interpreter.exec(String.format(statelessWrapperTemplate, functionQualifier, actualMethodName));
            } else if (argCount == minArgumentCount + 1) {
                interpreter.exec(String.format(statefulWrapperTemplate, functionQualifier, actualMethodName));
            }
        }

        return actualMethodName;
    }

    private synchronized String loadEventHandlerModuleFunction(String handlerName, String defaultMethodName, int minArgumentCount,
                                                               String statelessWrapperTemplate, String statefulWrapperTemplate) throws Exception {

        String actualMethodName = null;

        String customMethodName = (String)configuration.get(handlerName);
        if (customMethodName != null) {
            actualMethodName = customMethodName;
        } else if (isGlobalFunction(defaultMethodName)) {
            actualMethodName = defaultMethodName;
        }

        int argCount = 0;
        try {
            argCount = getArgCount(functionQualifier + actualMethodName);
        } catch(Exception e) {
            if (customMethodName != null) {
                throw new Exception("Custom " + handlerName + " handler '" + customMethodName +
                        "' not defined for actor '" + name + "'");
            }
            return null;
        }

        if (argCount == minArgumentCount) {
            interpreter.exec(String.format(statelessWrapperTemplate, functionQualifier, actualMethodName));
        } else if (argCount == minArgumentCount + 1) {
            interpreter.exec(String.format(statefulWrapperTemplate, functionQualifier, actualMethodName));
        }

        return actualMethodName;
    }
    private synchronized void applySettings() {
        if (settings != null && !functionQualifier.isEmpty()) {
            for(Map.Entry<String, Object> setting : settings.entrySet()) {
                String name = functionQualifier + setting.getKey();
                Object value = setting.getValue();
                if (value instanceof String) {
                    interpreter.exec(name + "='" + value + "'");
                } else if (!(value instanceof Collection)) {
                    interpreter.exec(name + "=" + value);
                }
            }
        }
    }

    @Override
    public void onStart() throws Exception{
        Map<String,Object> settings = initializeState();

        if (onStart != null) {
            interpreter.set("_KURATOR_STATE_", settings);
            interpreter.eval("_call_onstart()");
            broadcastOutputs();
        }

        if (onData == null) {
            endStreamAndStop();
        }
    }

    @Override
    public void onData(Object value) throws Exception {

        if (onData == null) {
            throw new Exception("No onData handler for actor " + this);
        }

        if (outputTypeIsInputType) {
            outputType = value.getClass();
        }

        Map<String,Object> state = initializeState();

        Object input = null;
        if (this.inputs.isEmpty()) {
            input = value;
        } else {
            input = mapInputs(value);
            ((Map)input).putAll(state);
        }

        interpreter.set("_KURATOR_STATE_", state);
        interpreter.set("_KURATOR_INPUT_", input);
        interpreter.eval("_call_ondata()");
        broadcastOutputs();
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

    @Override
    public void onEnd() {
        Map<String,Object> settings = initializeState();

        // call script end function if defined
        if (onEnd != null) {
            interpreter.set("_KURATOR_STATE_", settings);
            interpreter.eval("_call_onend()");
        }

        // shut down and deallocate the interpreter
        interpreter.close();
        interpreter = null;
    }


    private void broadcastOutputs() {

        if (! interpreter.get("_KURATOR_MORE_DATA_", Boolean.class)) {
            broadcastOutput(interpreter.get("_KURATOR_OUTPUT_", outputType));
            return;
        }

        do {
            interpreter.eval("_get_next_data()");
            Object output = interpreter.get("_KURATOR_OUTPUT_", outputType);
            if (output != null) broadcastOutput(output);
        } while (interpreter.get("_KURATOR_MORE_DATA_", Boolean.class));
    }


    private void broadcastOutput(Object output) {

        publishProducts(output);

        if (output != null || broadcastNulls) {
            broadcast(output);
        }
    }

    @SuppressWarnings("unchecked")
    private void publishProducts(Object output) {

        if (output != null && output instanceof Map) {
            Map<Object,Object> outputMap = (Map<Object,Object>) output;

            Map<String,Object> products = (Map<String,Object>) outputMap.get("products");
            if (products != null) {
                publishProducts(products);
            }

            Map<String,String> artifacts = (Map<String,String>) outputMap.get("artifacts");
            if (artifacts != null) {
                publishArtifacts(artifacts);
            }
        }
    }

    public static void updateClasspath() {

        for (String jythonPathVar : new String[] {"JYTHONPATH", "JYTHON_PATH"}) {
            addToClasspath(System.getenv(jythonPathVar));
        }

        for (String jythonHomeVar : new String[] {"JYTHONHOME", "JYTHON_HOME"}) {
            String jythonHome = System.getenv(jythonHomeVar);
            if (jythonHome != null) {
                addToClasspath(jythonHome + "/Lib/site-packages");
            }
        }

        addToClasspath("packages");
    }

    private static void addToClasspath(String path) {
        if (path != null) {
            for (String pathElement : path.split(System.getProperty("path.separator"))) {
                try {
                    SystemClasspathManager.addPath(pathElement);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
    }

    protected String reconstructPythonSourcePath(String compiledClassPath) {
        int i = compiledClassPath.lastIndexOf("$py.class");
        if (i != -1) {
            return compiledClassPath.substring(0, i) + ".py";
        } else {
            return compiledClassPath;
        }
    }
}
