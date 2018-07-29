package ch.njol.tome.common;

/**
 * Object of this type may change, and can inform listeners of that fact.
 */
public interface Modifiable {
	
	/**
	 * Registers a listener to be notified when this object becomes invalid.
	 * 
	 * @param listener
	 */
	void addModificationListener(ModificationListener listener);
	
	/**
	 * Removes a listener from this object. Useful if the listener itself became invalid.
	 * 
	 * @param listener
	 */
	void removeModificationListener(ModificationListener listener);
	
}
