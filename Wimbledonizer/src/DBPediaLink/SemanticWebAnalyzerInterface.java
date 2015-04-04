package DBPediaLink;

public interface SemanticWebAnalyzerInterface {
	
	/**
	 * 
	 * @param name The name of the person
	 * @param personClassPath The path of the person-class
	 * @return The main-entry of the associated person, or null if the person is not part of the class
	 */
	public String isOfPersonClass(String name, String personClassPath);
}
