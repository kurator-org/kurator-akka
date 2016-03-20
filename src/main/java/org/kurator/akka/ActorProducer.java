package org.kurator.akka;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kurator.akka.metadata.MetadataReader;
import org.kurator.akka.metadata.MetadataWriter;

import akka.actor.IndirectActorProducer;

public class ActorProducer implements IndirectActorProducer {

    private Class<? extends AkkaActor> actorClass;
    private Map<String, Object> defaults;
    private Map<String, Object> parameters;
    private Map<String, Object> configuration;
    private List<ActorConfig> listeners;
    private List<MetadataReader> metadataReaders;
    private List<MetadataWriter> metadataWriters;
    private WorkflowRunner workflowRunner;
    private AkkaActor actor;
    private InputStream inStream;
    private PrintStream outStream;
    private PrintStream errStream;

    public ActorProducer(
            Class<? extends AkkaActor> actorClass, 
            Map<String,Object> configuration,
            Map<String, Object> defaults, 
            Map<String, Object> parameters, 
            List<ActorConfig> listeners,
            List<MetadataReader> metadataReaders,
            List<MetadataWriter> metadataWriters,
            InputStream inStream, 
            PrintStream outStream, 
            PrintStream errStream,
            WorkflowRunner workflowRunner) {
        
        this.actorClass = actorClass;
        this.configuration = configuration;
        this.defaults = defaults;
        this.parameters = parameters;
        this.listeners = listeners;
        this.metadataReaders = metadataReaders;
        this.metadataWriters = metadataWriters;
        this.workflowRunner = workflowRunner;
        this.inStream = inStream;
        this.outStream = outStream;
        this.errStream = errStream;
    }

    @Override
    public Class<? extends AkkaActor> actorClass() {
        return actorClass;
    }

    @Override
    public AkkaActor produce() {
        
        // create the actor instance from its class
        try {
            actor = actorClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        // configure the actor according to its configuration
        actor.listeners(listeners)
             .runner(workflowRunner)
             .inputStream(inStream)
             .outputStream(outStream)
             .errorStream(errStream)
             .configuration(configuration)
             .metadataWriters(metadataWriters)
             .metadataReaders(metadataReaders);
        
        // assign values to the actor parameters 
        Map<String,Object> parameterSettings = new HashMap<String,Object>();
        Map<String,Object> unappliedSettings = new HashMap<String,Object>();
        parameterSettings.putAll(defaults);
        parameterSettings.putAll(parameters);
        for (Map.Entry<String,Object> setting : parameterSettings.entrySet()) {
            String name = setting.getKey();
            Object value = setting.getValue();
            boolean parameterWasSet = false;
            try {
                 parameterWasSet = (setParameter(name, value));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
            
            if (!parameterWasSet) {
                unappliedSettings.put(name, value);
            }
        }
        
        actor.settings(unappliedSettings);
            
        return actor;
    }
    
    /**
     * Attempt to set an instance variable of actor to a provided value.
     * 
     * @param pameterName the name of the instance variable to set.
     * @param value the value to set it to
     * @return true if the value was successfully set, otherwise false.
     * 
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private boolean setParameter(String pameterName, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {        

    	boolean parameterWasSet = false;
    	try {
    		// First try setting the property with the Java Bean getter and setter convention.
    		Method set = findSetFor(actor.getClass(), pameterName);
    		set.invoke(actor, value);
    		parameterWasSet = true;
    	} catch (IntrospectionException e1) {
    		// If the set method doesn't exist, try setting the property directly
    		// in case it is simply exposed as public without encapsulation
    		try {
    			Field field = actor.getClass().getField(pameterName);
    			boolean accessible = field.isAccessible();
    			field.setAccessible(true);
    			field.set(actor, value);
    			field.setAccessible(accessible);
    			parameterWasSet = true;
    		} catch (NoSuchFieldException e) {
    			parameterWasSet = false;
    		}
    	}
        
        return parameterWasSet;
    }
    
    /**
     * Using reflection, return the setter method for a property of a class that follows the
     * java bean convention for setter methods. 
     * 
     * @see java.beans.PropertyDescriptor.PropertyDescriptor
     * 
     * @param beanClass the class which to check for a setter
     * @param propertyName the property name for which to look for a setter, expectation is that 
     * the property name will begin with a lower case character, which will be capitalized in 
     * the setter name, property foo is expected to have a setter setFoo().
     * @return the set method, which can be invoked with .invoke(class, value);
     * @throws IntrospectionException if a set method following the java convention is not met.  
     */
    private Method findSetFor(@SuppressWarnings("rawtypes") final Class beanClass,final String propertyName) throws IntrospectionException {
        return new PropertyDescriptor(propertyName,beanClass).getWriteMethod();
    }    
}
