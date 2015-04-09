package classifier;

import java.util.List;
import java.util.Map;

public interface EntityResolutionInterface {
	
	/**
	 * @param name The name of the person
	 * @return The main-entry of the associated person, or null if the person is not part of the class
	 */
	public String isOfClass(String name);
	
	/**
	 * @return The class this analyzer can analyze
	 */
	public String getClassName();

}
