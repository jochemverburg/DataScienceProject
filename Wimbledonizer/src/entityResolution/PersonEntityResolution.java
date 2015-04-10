package entityResolution;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import classifier.EntityResolutionInterface;
import edu.berkeley.nlp.util.SortedList;
import edu.stanford.nlp.util.Pair;

public abstract class PersonEntityResolution implements EntityResolutionInterface{
		
		Map<String,List<String>> entityNameMapping;
		String className;
	
		public static final String DBPEDIA_SPARQL = "http://dbpedia.org/sparql";
		public static final String DBPEDIANL_SPARQL = "http://nl.dbpedia.org/sparql";
		public static final String QUERY_PREFIXES = "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> "
							    		+ "PREFIX dbpprop: <http://dbpedia.org/property/> "
							    		+ "PREFIX dbres: <http://dbpedia.org/resource/> "
							    		+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
							    		+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
							    		+ "PREFIX prop-nl: <http://nl.dbpedia.org/property/>";
		
		public static final double TRESHOLD = 0.7;
		
		public PersonEntityResolution(String className){
			this.className = className;
		}
		
		/**
		 * @param name The name of the person
		 * @return The main-entry of the associated person, or null if the person is not part of the class
		 */
		public String isOfClass(String name){
			if(entityNameMapping==null){
				entityNameMapping = getEntityNameMapping();
			}
			return getEntity(name, entityNameMapping);
		}
			
		
		/**
		 * @return The class this analyzer can analyze
		 */
		public String getClassName(){
			return className;
		}
		
		/**
		 * 
		 * @return A mapping from the entity-identifiers/URIs to a List of names belonging to the entity.
		 */
		public abstract Map<String,List<String>> getEntityNameMapping();
			
		//Solution copied from http://stackoverflow.com/questions/3322152/is-there-a-way-to-get-rid-of-accents-and-convert-a-whole-string-to-regular-lette on 30-03-2015
	    public static String flattenToAsciiLowerCase(String string) {
	        char[] out = new char[string.length()];
	        string = Normalizer.normalize(string, Normalizer.Form.NFD);
	        int j = 0;
	        for (int i = 0, n = string.length(); i < n; ++i) {
	            char c = string.charAt(i);
	            if (c <= '\u007F') out[j++] = c;
	        }
	        return new String(out).toLowerCase();
	    }
	    
	    /**
	     * This method should decide the probability that the name belongs the same entity as this list of names.
	     * @param name The name to be analyzed
	     * @param listOfNames The names belonging to one of the possible entities
	     * @return The probability of the name
	     */
	    public static double decideProbability(String name, List<String> listOfNames){
	    	// TODO Implement Fuzzy logic/probability calculation
	    	return 0.0;
	    }
	    
	    /**
	     * This method returns the entity which is most probable for this name. Returns 0 if none of the entities come above a certain treshold.
	     * @param name The name to be linked to an entity
	     * @param entityNamesMapping A mapping from the possible entities to a list of names belonging to the entities.
	     * @return The entity-name (usually a dbpedia-link)
	     */
	    public static String getEntity(String name, Map<String,List<String>> entityNamesMapping){
			// TODO Implement using probabilities
	    	List<Pair<String, Double>> probabilities = new SortedList<Pair<String, Double>>(new ProbabilityComparator<String>());
	    	for(Entry<String, List<String>> entity : entityNamesMapping.entrySet()){
	    		double probability = decideProbability(name, entity.getValue());
	    		
	    		//Only add results above a certain treshold
	    		if(probability>=TRESHOLD){
	    			probabilities.add(new Pair<String,Double>(entity.getKey(),probability));
	    		}
	    	}
	    	if(probabilities.isEmpty()){
		    	return null;
	    	}
	    	else{
	    		//Return first result
	    		return probabilities.get(0).first();
	    	}
	    }


}
