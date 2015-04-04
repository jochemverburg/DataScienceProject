package DBPediaLink;

public interface SemanticWebAnalyzerInterface {
	
	/**
	 * 
	 * @param name The name of the person
	 * @param personClassPath The path of the person-class
	 * @return Whether the person is part of that class
	 */
	public boolean isOfPersonClass(String name, String personClassPath);
}
