package org.kurator.akka;

import java.util.Map;
import java.util.Properties;

import org.python.core.PyBoolean;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyDictionary;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class PythonActor extends AkkaActor {
    
    protected static String DEFAULT_ON_INIT    = "on_init";
    protected static String DEFAULT_ON_START   = "on_start";
    protected static String DEFAULT_ON_DATA    = "on_data";
    protected static String DEFAULT_ON_END     = "on_end";
    
    public Class<? extends Object> inputType = Object.class;
    public Class<? extends Object> outputType = Object.class;
    public boolean broadcastNulls = false;
    public boolean outputTypeIsInputType = false;
    
    protected String functionQualifier = "";
    
    protected String onInit = null;
    protected String onStart = null;
    protected String onData = null;
    protected String onEnd = null;
    
    protected PythonInterpreter interpreter;
    protected PyDictionary state;
    protected PyObject none;
    
    private static Boolean jythonInitialized = false;
    
    private String commonScriptHeader =
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
            "def _is_function(f):"                          + EOL +
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
    
    private static String statelessOnDataWrapperTemplate = 
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

    private static String statefulOnDataWrapperTemplate = 
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
    protected void onInitialize() throws Exception {
        
        initializeJythonInterpreter();
        loadCommonHelperFunctions();
        loadCustomCode();
        configureCustomCode();
        
        onInit = loadEventHandler("onInit", DEFAULT_ON_INIT, 0, statelessOnInitWrapperTemplate, statefulOnInitWrapperTemplate);
        onStart = loadEventHandler("onStart", DEFAULT_ON_START, 0, statelessOnStartWrapperTemplate, statefulOnStartWrapperTemplate);
        onData = loadEventHandler("onData", DEFAULT_ON_DATA, 1, statelessOnDataWrapperTemplate, statefulOnDataWrapperTemplate);
        onEnd = loadEventHandler("onEnd", DEFAULT_ON_END, 0, onEndWrapperTemplate, statefulOnEndWrapperTemplate);
        
        initializeState();
        applySettings();
        
        if (onInit != null) {
            interpreter.set("_KURATOR_STATE_", state);
            interpreter.eval("_call_oninit()");
        }
    }
    
    private void initializeState() {
        state = new PyDictionary();
        for(Map.Entry<String, Object> setting : settings.entrySet()) {
            String name = setting.getKey();
            Object value = setting.getValue();
            state.put(name, value);
        }
    }

    protected void configureCustomCode() throws Exception {
    }
    
    protected void loadCommonHelperFunctions() {
        interpreter.exec(commonScriptHeader);
    }
    
    protected void loadCustomCode() {
        
        // read the script into the interpreter
        interpreter.set("__name__",  "__kurator_actor__");
        String script = (String)configuration.get("script");
        if (script != null) interpreter.execfile(script);
        
        String code = (String)configuration.get("code");
        if (code != null) interpreter.exec(code);        
    }

    
    protected void initializeJythonInterpreter() {

        synchronized(jythonInitialized) {

            Properties properties = System.getProperties();
            properties.put("python.import.site", "false");
    
            // create a python interpreter
            if (jythonInitialized == false) {
                PySystemState.initialize(properties, null, new String[] {""});
            }
    
            interpreter = new PythonInterpreter();
            interpreter.setOut(super.outStream);
            interpreter.setErr(super.errStream);
            
            interpreter.exec("from org import python");
            interpreter.exec("import sys"); 
            interpreter.exec("import types"); 
            interpreter.exec("import inspect"); 
            
            // configure Jython sys.path variable.
            if (jythonInitialized == false) {
                configureJythonSysPath();
                jythonInitialized = true;
            }
        }
        
        // cache a python None object
        none = interpreter.eval("None");
    }
    
    
    protected void configureJythonSysPath() {
        
        // add to python sys.path library directories from local Jython installation
        String jythonHome = System.getenv("JYTHON_HOME");
        if (jythonHome != null) {
            prependSysPath(jythonHome + "/Lib/site-packages");
            prependSysPath(jythonHome + "/Lib");
        }

        // add to python sys.path custom Python libraries
        String kuratorLocalPythonLib = System.getenv("KURATOR_LOCAL_PYTHON_LIB");
        if (kuratorLocalPythonLib != null) {
            prependSysPath(kuratorLocalPythonLib + "/site-packages");
            prependSysPath(kuratorLocalPythonLib);
        }
        
        // add to python sys.path optional local packages directory
        String kuratorLocalPackages = System.getenv("KURATOR_LOCAL_PACKAGES");
        if (kuratorLocalPackages != null) {
            prependSysPath(kuratorLocalPackages); 
        }

        // add to python sys.path jython libraries distributed via kurator-jython Git repo
        prependSysPath("kurator-jython");
        prependSysPath("../kurator-jython");
        
        // add to python sys.path directory of packages bundled within Kurator jar
        prependSysPath("src/main/python");
    }
    
    private Boolean isFunction(String f) {
        PyBoolean result = (PyBoolean)interpreter.eval("_is_function('" + f + "')");
        return result.getBooleanValue();
    }
    
    private Integer getArgCount(String f) {
        PyInteger result = (PyInteger)interpreter.eval("_function_arg_count(" + f + ")");
        return result.asInt();
    }
    
    protected String loadEventHandler(String handlerName, String defaultMethodName, int minArgumentCount,
            String statelessWrapperTemplate, String statefulWrapperTemplate) throws Exception {

        String actualMethodName = null;
        
        String customMethodName = (String)configuration.get(handlerName);
        if (customMethodName != null) {
            if (!isFunction(customMethodName)) {
                throw new Exception("Custom " + handlerName + " handler '" + customMethodName + 
                                    "' not defined for actor '" + name + "'");
            }
            actualMethodName = customMethodName;
        } else if (isFunction(defaultMethodName)) {
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
    
    
    protected void applySettings() {
    
        if (settings != null) {
            for(Map.Entry<String, Object> setting : settings.entrySet()) {
                String name = setting.getKey();
                Object value = setting.getValue();
                interpreter.set(name, value);
            }
        }
    }    
    
    @Override
    protected void onStart() throws Exception {

        if (onStart != null) {
            interpreter.set("_KURATOR_STATE_", state);
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
        
        if (onData != null) {
        
            if (outputTypeIsInputType) {
                outputType = value.getClass();
            }
            
            interpreter.set("_KURATOR_STATE_", state);
            interpreter.set("_KURATOR_INPUT_", value);
            interpreter.eval("_call_ondata()");
            broadcastOutputs();
        }
    }

    @Override
    protected void onEnd() {
        
        // call script end function if defined
        if (onEnd != null) {
            interpreter.set("_KURATOR_STATE_", state);
            interpreter.eval("_call_onend()");
        }
        
        // shut down the interpreter
        interpreter.cleanup();
    }    

    protected void broadcastOutputs() {

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


    protected void broadcastOutput(Object output) {
        if (output != null || broadcastNulls) {
            broadcast(output);
        }
    }

    private void prependSysPath(String path) {
        interpreter.eval(String.format("sys.path.insert(0, '%s')%s", path, EOL));
    }
}
