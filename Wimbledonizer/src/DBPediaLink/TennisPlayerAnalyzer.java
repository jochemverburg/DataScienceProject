package DBPediaLink;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class TennisPlayerAnalyzer implements SemanticWebAnalyzerInterface {
	
	private List<String> playerURIs;
	private String playerClass;
	
	public TennisPlayerAnalyzer(String playerClass) throws Exception{
		// TODO Make a general implementation
		throw new Exception("This constructor does not work yet");
	}
	
	public TennisPlayerAnalyzer(String playersPath, String playerClass) throws IOException{
		this(getPlayerURIsFromPath(playersPath),playerClass);
	}
	
	public TennisPlayerAnalyzer(List<String> playerURIs, String playerClass){
		this.playerURIs = playerURIs;
		this.playerClass = playerClass;
	}
	
	private static List<String> getPlayerURIsFromPath(String path) throws IOException{
		List<String> playerURIs = new ArrayList<String>();
		String line;
    	BufferedReader reader = new BufferedReader(
    			new FileReader(
    					new File(path)));
    	while((line = reader.readLine()) !=null && !line.isEmpty() ){
    		playerURIs.add(line);
    	}
    	
    	if (reader != null) {
 	       reader.close();
 	    }
    	return playerURIs;
	}
	
	@Override
	public String isOfPersonClass(String name) {
		/*for(String playerURI : playerURIs){
			for(String playerName : getNamesForURI(playerURI)){
				if(playerName.contains(name)){
					return playerURI;
				}
			}
		}*/
		Map<String, List<String>> uriNames = getNamesForURI();
		for(String playerURI : uriNames.keySet()){
			for(String playerName : uriNames.get(playerURI)){
				if(playerName.contains(name)){
					return playerURI;
				}
			}
		}
		return null;
	}

	@Override
	public String getPersonClass() {
		return playerClass;
	}

	public Map<String,List<String>> getNamesForURI(){
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		
		String uri = "?uri";
		String var_name = "?name";
		String extra_uri = "?extraUri";
		
		String namesQuery = QUERY_PREFIXES
				+ "SELECT "+uri+" "+var_name+" WHERE {"
				+ uri + " dbpprop:name "+var_name+"."
				+ uri + " rdf:type dbpedia-owl:TennisPlayer. "
				+ " }";
		String nlNamesQuery = QUERY_PREFIXES
				+ "SELECT "+uri+" "+var_name+" WHERE {"
				+ uri + " prop-nl:bijnaam "+var_name+"."
				+ uri + " rdf:type dbpedia-owl:TennisPlayer. "
				+ " }";
		String nlURIQuery = QUERY_PREFIXES
				+ "SELECT "+uri+" "+extra_uri+" WHERE {"
				+ uri + " rdf:type dbpedia-owl:TennisPlayer. "
				+ uri + " owl:sameAs "+extra_uri+". "
				+ " }";
		//+ "FILTER regex("+extra_uri+", \"http://nl.dbpedia.org/\")"
		
		//System.out.println(playerURI);
		//.replace("http://dbpedia.org/resource/", "")
		QueryExecution qeNames = QueryExecutionFactory.sparqlService(DBPEDIA_SPARQL, namesQuery);
	    QueryExecution qeNLURIs = QueryExecutionFactory.sparqlService(DBPEDIA_SPARQL, nlURIQuery);
		QueryExecution qeNicks = QueryExecutionFactory.sparqlService(DBPEDIANL_SPARQL, nlNamesQuery);
		
		ResultSet nameSet = qeNames.execSelect();
		ResultSet nlUriSet = qeNLURIs.execSelect();
		ResultSet nicknameSet = qeNicks.execSelect();
		
		//Gets the names from the main page
        if (nameSet!=null) {
            while(nameSet.hasNext()){
            	QuerySolution sol = nameSet.next();
            	String playerURI = sol.get(uri).toString();
            	String name = sol.get(var_name).toString();
	            if(playerURIs.contains(playerURI)){	
            		if(result.containsKey(playerURI)){
	            		result.get(playerURI).add(name);
	            	}
	            	else{
	            		List<String> temp = new ArrayList<String>();
	            		temp.add(name);
	            		result.put(playerURI, temp);
	            	}
            		System.out.println(playerURI + " " + name);
	            }

            }
        }
        
        //Gets all Dutch uris mapped to the original dbpedia
        Map<String, String> nlUriMap = new HashMap<String, String>();
        if(nlUriSet != null){
        	while(nlUriSet.hasNext()){
            	QuerySolution sol = nlUriSet.next();
            	String playerURI = sol.get(uri).toString();
            	String nlURI = sol.get(extra_uri).toString();
	            if(playerURIs.contains(playerURI)){	
	            	nlUriMap.put(nlURI, playerURI);
	            }
            }
        }
        else nlUriMap = null;
        
        if (nicknameSet!=null && nlUriMap!=null) {
            while(nicknameSet.hasNext()){
            	QuerySolution sol = nicknameSet.next();
            	String nlURI = sol.get(uri).toString();
            	String name = sol.get(var_name).toString();
            	if(nlUriMap.containsKey(nlURI)){
            		String playerURI = nlUriMap.get(nlURI);
            		if(result.containsKey(playerURI)){
	            		result.get(playerURI).add(name);
	            	}
	            	else{
	            		List<String> temp = new ArrayList<String>();
	            		temp.add(name);
	            		result.put(playerURI, temp);
	            	}
            		System.out.println(playerURI + " " + nlURI + " " + name);
            	}
            }
        }
        
		return result;
	}
	
	public static void main(String[] args) throws IOException{
		TennisPlayerAnalyzer analyzer = new TennisPlayerAnalyzer("resources/participantssubset.nt","Wimbledon_Player");
		System.out.println(analyzer.isOfPersonClass("Nadal"));
	}
	
}
