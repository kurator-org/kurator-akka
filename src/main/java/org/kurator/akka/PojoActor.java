package org.kurator.akka;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

public class PojoActor extends KuratorActor {

	public String wrappedClassName;
	private Class<? extends Object> wrappedClass;
    private Object wrappedObject;
	private String onInitializeMethodName;
    private String onStartMethodName;
	private String onDataMethodName;
	private String onEndMethodName;
	private Method onInitializeMethod;
    private Method onStartMethod;
	private Method onDataMethod;
	private Method onEndMethod;
	private Class<?> onDataReturnType;
	private Class<?> onDataArgumentType;
	
	private boolean broadcastNulls = false;
	private boolean endOnNullOutput = true;
	
    @SuppressWarnings("rawtypes")
    private static final Class[] EMPTY_SIGNATURE = new Class[]{};
    
    @SuppressWarnings("rawtypes")
    private static final Class[] SINGLE_ARGUMENT_SIGNATURE = new Class[]{ Integer.class };
	
	private static final String  DEFAULT_INITIALIZE_METHOD = "onInitialize";
    private static final String  DEFAULT_ON_START_METHOD   = "onStart";
	private static final String  DEFAULT_ON_DATA_METHOD    = "onData";
	private static final String  DEFAULT_ON_END_METHOD     = "onEnd";
	
	///////////////////////////////////////////////////////////////////////////
	////              public constructors and clone methods                ////
	
	@Override
	public void onInitialize() throws Exception {
		
	    Boolean endOnNullOutputConfiguration = (Boolean) configuration.get("endOnNullOutput");
	    if (endOnNullOutputConfiguration != null) {
	        endOnNullOutput = endOnNullOutputConfiguration;
	    }
	    
	    instantiatePojo();
		detectMethodsOnPojo();
		configurePojo();
		
        if (onInitializeMethod != null) {
            callMethodOnPojo(onInitializeMethod);
        }
    }
    
    @Override
    protected void onStart() throws Exception {

        if (settings != null) {
            for(Map.Entry<String, Object> setting : settings.entrySet()) {
                String name = setting.getKey();
                Object value = setting.getValue();
                setPojoProperty(name, value);
            }
        }
        
        if (onStartMethod != null) {
            callMethodOnPojo(onStartMethod);
        }
    }

    @Override
    public void onData(Object value) throws Exception {  
        
        if (onDataMethod != null) {
            
            Object resultObject = callMethodOnPojo(onDataMethod, onDataArgumentType.cast(value));

            if (resultObject == null && endOnNullOutput) {
                endStreamAndStop();
                return;
            }
            
	        if (resultObject != null || broadcastNulls) {
	            broadcast(onDataReturnType.cast(resultObject));
	        }
		}
	}

    @Override
    protected void onEnd() throws Exception {
		
		if (onEndMethod != null) {
			callMethodOnPojo(onEndMethod);
		}
	}
    
    private void instantiatePojo() throws Exception {

        wrappedClassName = (String) configuration.get("pojoClass");
        if (wrappedClassName == null) {
            throw new Exception("No bean class provided for actor " + this);
        }
    
        try {
            wrappedClass = Class.forName(wrappedClassName);
        } catch (ClassNotFoundException cause) {
            throw new Exception("Bean class " + wrappedClassName +
                                                " not found for actor " + this, cause);
        }
    
        try {
            wrappedObject = wrappedClass.newInstance();
        } catch (Exception cause) {
            throw new Exception(
                    "Error instantiating instance of bean class " + 
                    wrappedClass + " for actor " + this, cause);
        }
    }
    
    private void detectMethodsOnPojo() throws Exception {

        onInitializeMethod = getMethodOnPojo(
                onInitializeMethodName, 
                DEFAULT_INITIALIZE_METHOD, 
                EMPTY_SIGNATURE);
                
        onStartMethod = getMethodOnPojo(
                onStartMethodName,
                DEFAULT_ON_START_METHOD, 
                EMPTY_SIGNATURE);

        onDataMethod = getMethodOnPojo(
                onDataMethodName,
                DEFAULT_ON_DATA_METHOD, 
                SINGLE_ARGUMENT_SIGNATURE);
        
        if (onDataMethod != null) {
            onDataArgumentType = onDataMethod.getParameterTypes()[0];
            onDataReturnType = onDataMethod.getReturnType();
        }
        
        onEndMethod = getMethodOnPojo(
                onEndMethodName, 
                DEFAULT_ON_END_METHOD, 
                EMPTY_SIGNATURE);
    }

    private void configurePojo() throws Exception {

        String inputStreamProperty = (String) configuration.get("inputStreamProperty");
        if (inputStreamProperty != null) {
            setPojoProperty(inputStreamProperty, inStream);
        }

        String outputStreamProperty = (String) configuration.get("outputStreamProperty");
        if (outputStreamProperty != null) {
            setPojoProperty(outputStreamProperty, outStream);
        }
        
        String errorStreamProperty = (String) configuration.get("errorStreamProperty");
        if (errorStreamProperty != null) {
            setPojoProperty(errorStreamProperty, outStream);
        }

    }

    private void callMethodOnPojo(Method method) throws Exception {
		
		try {
		    method.invoke(wrappedObject);
			
		} catch (Exception reflectionException) {
			throw new Exception(
					"Exception in " + method.getName() + "() method of actor '" + this + "'",
					 reflectionException.getCause()
			);
		}
	}

	private Object callMethodOnPojo(Method method, Object value) throws Exception {
    
        try {
            return method.invoke(wrappedObject, value);
                
        } catch (Exception reflectionException) {
            throw new Exception(
                "Exception in " + method.getName() + "() method of actor '" + this + "'",
                 reflectionException.getCause()
            );
        }
	}

	
	/** Uses reflection to look up the method named by first parameter (the custom method name), 
	 * or by the second parameter (the default method name) if the first is null 
	 * @throws Exception */
	private Method getMethodOnPojo(String customMethodName, 
			String defaultMethodName, Class[] signature) throws Exception {

        Method[] methods = wrappedClass.getMethods();
		Method method = null;

		if (customMethodName != null) {
			
			// look for the method named by the first parameter and throw an exception if
			// the custom method is not defined on the bean
		    for (Method m : methods) {
		        if (m.getName().equals(customMethodName)) {
		            method = m;
		            break;
		        }
		    }
		    
		    if (method == null) {
    			throw new Exception(
    				"Error finding declared method " + customMethodName +
    				" on object " + wrappedObject.getClass() + " for actor " + this);
			}
			
		} else {
			
	          for (Method m : methods) {
	               if (m.getName().equals(defaultMethodName)) {
	                   method = m;
	                   break;
	               }
	          }
		}
		
		if (method != null) {
			method.setAccessible(true);
		}
		
		return method;
	}
		
    private void setPojoProperty(String name, Object value) throws Exception {
        
        try {
            Field field = wrappedClass.getField(name);
            field.setAccessible(true);
            field.set(wrappedObject, value);
        } catch (Exception e) {
            PropertyUtils.setNestedProperty(wrappedObject, name, value);            
        }
    }
    
    public class Port {
        
        public void write(Object value) {
            broadcast(value);
        }
    }
}