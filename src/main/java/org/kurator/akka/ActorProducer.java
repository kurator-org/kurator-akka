package org.kurator.akka;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kurator.akka.metadata.MetadataReader;
import org.kurator.akka.metadata.MetadataWriter;
import org.kurator.log.Log;
import org.kurator.log.Logger;

import akka.actor.IndirectActorProducer;

public class ActorProducer implements IndirectActorProducer {

    private Class<? extends KuratorActor> actorClass;
    private Map<String, Object> defaults;
    private Map<String, Object> parameters;
    private Map<String, Object> configuration;
    private List<ActorConfig> listeners;
    private Map<String, String> inputs = new HashMap<String,String>();
    private List<MetadataReader> metadataReaders;
    private List<MetadataWriter> metadataWriters;
    private WorkflowRunner workflowRunner;
    private KuratorActor actor;
    private String actorName;
    private InputStream inStream;
    private PrintStream outStream;
    private PrintStream errStream;
    private Logger logger;

    public ActorProducer(
            Class<? extends KuratorActor> actorClass, 
            Map<String,Object> configuration,
            Map<String, Object> defaults, 
            Map<String, Object> parameters, 
            List<ActorConfig> listeners,
            Map<String, String> inputs,
            List<MetadataReader> metadataReaders,
            List<MetadataWriter> metadataWriters,
            Logger logger,
            InputStream inStream, 
            PrintStream outStream, 
            PrintStream errStream,
            WorkflowRunner workflowRunner) {
        
        this.logger = logger;
        this.logger.setSource("ACTOR-PRODUCER");
        
        this.actorClass = actorClass;
        this.configuration = configuration;
        this.defaults = defaults;
        this.parameters = parameters;
        this.listeners = listeners;
        this.inputs = inputs;
        this.metadataReaders = metadataReaders;
        this.metadataWriters = metadataWriters;
        this.workflowRunner = workflowRunner;
        this.inStream = inStream;
        this.outStream = outStream;
        this.errStream = errStream;
        this.actorName = (String) configuration.get("name");
        
        if (this.metadataReaders == null) {
            this.metadataReaders = new LinkedList<MetadataReader>();
        }

        if (this.metadataWriters == null) {
            this.metadataWriters = new LinkedList<MetadataWriter>();
        }
    }

    @Override
    public Class<? extends KuratorActor> actorClass() {
        return actorClass;
    }

    @Override
    public KuratorActor produce() {
        
        logger.trace("Producing Akka actor representing " + Log.ACTOR(actorName));
        
        // create the actor instance from its class
        try {
            actor = actorClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        // configure the actor according to its configuration
        actor.listeners(listeners)
             .inputs(inputs)
             .runner(workflowRunner)
             .logger(logger.createChild())
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
     * @param parameterName the name of the instance variable to set.
     * @param value the value to set it to
     * @return true if the value was successfully set, otherwise false.
     * 
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private boolean setParameter(String parameterName, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {        

    	boolean parameterWasSet = false;
    	try {
    		// First try setting the property with the Java Bean getter and setter convention.
    		Method set = findSetFor(actor.getClass(), parameterName);
    		set.invoke(actor, value);
    		parameterWasSet = true;
    	} catch (IntrospectionException e1) {
    		// If the set method doesn't exist, try setting the property directly
    		// in case it is simply exposed as public without encapsulation
    		try {
    			Field field = actor.getClass().getField(parameterName);
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
