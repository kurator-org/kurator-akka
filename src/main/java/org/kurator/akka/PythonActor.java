package org.kurator.akka;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.python.core.PyBoolean;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyDictionary;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class PythonActor extends KuratorActor {
    
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
        
        try {
            loadCustomCode();
            configureCustomCode();
        } catch(Exception e) {
            System.out.println(interpreter.eval(String.format("sys.path")));
            throw e;
        }
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

        Properties properties = System.getProperties();
        properties.put("python.import.site", "false");

        interpreter = new PythonInterpreter(null, new PySystemState());
        interpreter.setOut(super.outStream);
        interpreter.setErr(super.errStream);
        
        interpreter.exec("from org import python");
        interpreter.exec("import sys"); 
        interpreter.exec("import types"); 
        interpreter.exec("import inspect"); 
            
        // configure Jython sys.path variable.
        configureJythonSysPath();

        // cache a python None object
        none = interpreter.eval("None");
    }
    
    
    protected void configureJythonSysPath() {

        // add libray of packages installed locally for Jython
        String jythonHome = System.getenv("JYTHONHOME");
        if (jythonHome != null) {
            prependSysPath(jythonHome + "/Lib/site-packages");
        }
        
        // add the entire Jython path
        prependSysPath(System.getenv("JYTHONPATH"));

        // add to python sys.path directory of packages bundled within Kurator jar
        prependSysPath("src/main/python");
        prependSysPath("packages");
    }
    
    private Boolean isFunction(String f) {
        PyBoolean result = (PyBoolean)interpreter.eval("_is_function('" + f + "')");
        return result.getBooleanValue();
    }
    
    protected Integer getArgCount(String f) {
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
        
        // shut down and deallocate the interpreter
        interpreter.close();
        interpreter = null;
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
        if (path != null) {
            // insert each element of path to intepreter's sys.path maintaining
            // the order of elements in path and after the first element in sys.path
            // (the first element of sys.path must remain first)
            int i = 1;
            for (String pathElement : path.split(System.getProperty("path.separator"))) {
                interpreter.eval(String.format("sys.path.insert(%d, '%s')%s", i, pathElement, EOL));
            }
        }
    }
}
