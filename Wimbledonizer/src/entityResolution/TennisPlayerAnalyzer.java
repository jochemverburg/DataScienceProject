package entityResolution;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

public class TennisPlayerAnalyzer extends PersonEntityResolution {
	
	public static final String PARTICIPANT_LIST_COLUMN_DELIM = "\t";
	
	private List<String> playerURIs;
	
	public TennisPlayerAnalyzer(String playerClass) throws Exception{
		// TODO Make a general implementation
		super(playerClass);
		throw new Exception("This constructor does not work yet");
	}
	
	public TennisPlayerAnalyzer(String playersPath, String playerClass) throws IOException{
		this(getPlayerURIsFromPath(playersPath),playerClass);
	}
	
	public TennisPlayerAnalyzer(List<String> playerURIs, String playerClass){
		super(playerClass);
		this.playerURIs = playerURIs;
	}
	
	@Override
	public Map<String,List<String>> getEntityNameMapping(){
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
				+ "FILTER regex("+extra_uri+", \"http://nl.dbpedia.org/\")"
				+ " }";
		//
		
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
            		//System.out.println(playerURI + " " + name);
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
	            	//System.out.println("URI: "+playerURI+" "+nlURI);
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
            		//System.out.println(playerURI + " " + nlURI + " " + name);
            	}
            }
        }
        
		return result;
	}
	
	/**
	 * Can find the URIs for the names of tennis players. Only puts it in the map if all sub-parts of the names (uses containsNames()) are in one of the names of the URI.
	 * @param names
	 * @return
	 */
	public static Map<String, String> makeNameURIMapping(List<String> names){
		Map<String,String> result = new HashMap<String,String>();
    	
    	String var_person = "?p";
    	String var_name = "?name";
    	
    	String service = "http://dbpedia.org/sparql";
    	String query = "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"
         		+ "PREFIX dbpprop: <http://dbpedia.org/property/>"
         		+ "PREFIX dbres: <http://dbpedia.org/resource/>"
         		+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
         		+ "SELECT "+var_person+" "+var_name+" WHERE {"
         		+ " "+var_person+" rdf:type dbpedia-owl:TennisPlayer. "
         		+ " "+var_person+" dbpprop:name "+var_name+"."
         		+ " }"
         		+ "";
        //String query = "ASK { }";
        QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
        ResultSet set = null;
        
		try {
        	set = qe.execSelect();
        } catch (QueryExceptionHTTP e) {
            System.out.println(service + " is DOWN");
        }
		
		List<QuerySolution> queryResults = new ArrayList<QuerySolution>();
		while(set.hasNext()){
	   		queryResults.add(set.next());
        }
        
		for(String name : names) {

		   	String playerRef = null;
			
		   	for(QuerySolution tuple : queryResults){
		   		String var_names = tuple.get(var_name).toString();
                if(containsNames(var_names,name)){
                	playerRef = tuple.get(var_person).toString();
                	//playerRef = "<"+tuple.get(var_person).toString()+"> <"+rdf_type+"> <"+var_type+">";
                }
            }
		   	if(playerRef!=null){
		   		result.put(name, playerRef);
		   	}
		}
        
		return result;
	}
	
	/**
	 * Reads the names from the particpants-file
	 * @param namesPath
	 * @param column The column in the tab-separated file where the names are.
	 * @return
	 * @throws IOException 
	 */
	public static List<String> getNamesForParticipants(String namesPath, int column) throws IOException{
		List<String> result = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(
				new FileReader(
						new File(namesPath)));
		String text = null;	
		
		while ((text = reader.readLine()) != null) {
			result.add(text.split(PARTICIPANT_LIST_COLUMN_DELIM)[column-1]);
		}
		
		if(reader!=null){
			reader.close();
		}
		
		return result;
	}
	
	public static List<String> getURIsOfParticipants(String path, int column) throws IOException{
		List<String>  result = new ArrayList<String>();
		result.addAll(makeNameURIMapping(getNamesForParticipants(path, column)).values());
		return result;
	}
	
	public static void printParticipantURIsToPath(String inputPath, int column, String outputPath) throws IOException{
		List<String> uris = getURIsOfParticipants(inputPath, column);
		
		Writer w = new BufferedWriter(
        		new OutputStreamWriter(
        				new FileOutputStream(
        						new File(outputPath))));
		for(String uri : uris){
			w.write(uri+"\n");
		}
        w.close();
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
	
	public static void main(String[] args) throws IOException{
		TennisPlayerAnalyzer analyzer = new TennisPlayerAnalyzer("resources/participantssubset.nt","Wimbledon_Player");
		System.out.println(analyzer.isOfClass("Djoker"));
	}

}
