package org.kurator.akka;

/** 
 * <p>This file is an adaptation of Contract.java in the org.restlow.util
 * package as of 16Sep2015.  See <i>licenses/restflow_license.txt</i> for 
 * the copyright notice, license, and git repository URL for RestFlow.</p>
 */

public abstract class Contract {

	private static boolean enforceContract;
	
   static {
        synchronized(Contract.class) {
            enforceContract = false;
        }
    }
	
	public static void enforceContract(boolean value) {
        synchronized(Contract.class) {
            enforceContract = value;
        }
	}
	
   public static void requires(Object state, Object... allowedStates) {
       synchronized(Contract.class) {
           if (enforceContract) {
               for (Object allowed : allowedStates) {
                   if (state == allowed) return;
               }
               throw new IllegalStateException("State: " + state);
           }
       }
   }
   
   public static void disallows(Object state, Object... disallowedStates) {
       synchronized(Contract.class) {
           if (enforceContract) {
               for (Object disallowed : disallowedStates) {
                   if (state == disallowed) {
                       throw new IllegalStateException("State: " + state);
                   }
               }
           }
       }
   }
}
