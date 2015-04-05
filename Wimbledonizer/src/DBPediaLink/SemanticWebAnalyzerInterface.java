package DBPediaLink;

import java.text.Normalizer;

public interface SemanticWebAnalyzerInterface {
	public static final String DBPEDIA_SPARQL = "http://dbpedia.org/sparql";
	public static final String DBPEDIANL_SPARQL = "http://nl.dbpedia.org/sparql";
	public static final String QUERY_PREFIXES = "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> "
						    		+ "PREFIX dbpprop: <http://dbpedia.org/property/> "
						    		+ "PREFIX dbres: <http://dbpedia.org/resource/> "
						    		+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						    		+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
						    		+ "PREFIX prop-nl: <http://nl.dbpedia.org/property/>";
	
	/**
	 * @param name The name of the person
	 * @return The main-entry of the associated person, or null if the person is not part of the class
	 */
	public String isOfPersonClass(String name);
	
	/**
	 * @return The class this analyzer can analyze
	 */
	public String getPersonClass();
	
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
}
