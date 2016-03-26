package org.kurator.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

public class SystemClasspathManager {

    private static final Set<String> addedURLs = new HashSet<String>();
    private static URLClassLoader systemClassLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
    private static Method addUrlMethod;
    
    static {
        Class<URLClassLoader> urlClassLoaderClass = URLClassLoader.class;        
        try {
            addUrlMethod = urlClassLoaderClass.getDeclaredMethod("addURL", new Class[]{URL.class});
        } catch (Exception e) {
            System.err.println("Error accessing method for adding to CLASSPATH");
            e.printStackTrace();
            System.exit(-1);
        }
        addUrlMethod.setAccessible(true);
    }
    
	public static void addPath(String path) throws Exception {

	    URL url = new File(path).toURI().toURL();
        String urlString = url.toString();
		
	    synchronized(addedURLs) {
    	    if (addedURLs.contains(urlString)) {
    	        return;
    	    } else {
                addedURLs.add(urlString);
    	    }    	        
	    }
    	 
    	try {
    		addUrlMethod.invoke(systemClassLoader, new Object[]{url});
    	} catch (Exception e) {
            System.err.println("Error adding " + urlString + " to CLASSPATH");
    		throw e;
    	}
	}
    
    public static void printClasspath() {
        for (URL u : systemClassLoader.getURLs()) {
            System.out.println("CLASSPATH ELEMENT: " + u);
        }
    }
}