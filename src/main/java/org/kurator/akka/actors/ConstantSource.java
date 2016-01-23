package org.kurator.akka.actors;

import java.util.Collection;

public class ConstantSource extends OneShot {

    private Object value;
    private Collection<Object> values;

	@Override
    public void fireOnce() throws Exception {
        
        if (value != null) {
            broadcast(value);
        } else if (values != null) {
            for (Object value : values) {
                broadcast(value);                    
            }
        }        
    }    
    
    /**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return the values
	 */
	public Collection<Object> getValues() {
		return values;
	}

	/**
	 * @param values the values to set
	 */
	public void setValues(Collection<Object> values) {
		this.values = values;
	}


}
