package org.kurator.akka.actors;

import org.kurator.akka.AkkaActor;

/**
 * Multiply the input by a factor and broadcast the result.
 * 
 * @author tmcphillips
 * @author mole
 *
 */
public class Multiplier extends AkkaActor {

    private Integer factor = 1;

    @Override
    public void onData(Object value) throws Exception {
        if (value instanceof Integer) {
            int product = (int) value * this.factor.intValue();
            broadcast(product);
        } else if (value instanceof Long) {
            Long product = (Long) value * this.factor.longValue();
            broadcast(product);
        } else if (value instanceof Double) { 
        	Double product = (Double) value * this.factor.doubleValue();
        	broadcast(product);
        } else if (value instanceof Float) { 
        	Float product = (Float) value * this.factor.floatValue();
        	broadcast(product);
        }
    }

	/**
	 * @return the factor
	 */
	public int getFactor() {
		return factor;
	}

	/**
	 * Set the factor to multiply the input value by.
	 * 
	 * @param factor the factor to set
	 */
	public void setFactor(int factor) {
		this.factor = new Integer(factor);
	}
    
    
    
}
