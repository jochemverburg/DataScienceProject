package ner;

public interface EntityResolutionInterface {
	
	/**
	 * @param name The name of the entity
	 * @return The URI of the associated entity, or null if the name is not part of the class
	 */
	public String isOfClass(String name);
	
	/**
	 * @return The class this analyzer can analyze
	 */
	public String getClassName();

}
