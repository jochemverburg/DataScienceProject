package DBPediaLink;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

import edu.stanford.nlp.util.Pair;

public class QueryTest {

    public static void main(String[] args) throws IOException {
        System.out.println(addWimbledonPlayerType());
    	String service = "http://dbpedia.org/sparql";
        /*String query = "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"
        		+ "PREFIX dbpprop: <http://dbpedia.org/property/>"
        		+ "PREFIX dbres: <http://dbpedia.org/resource/>"
        		+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
        		+ "SELECT ?p ?name WHERE {"
        		+ " ?p rdf:type dbpedia-owl:TennisPlayer. "
        		+ " ?p dbpprop:name ?name."
        		+ " }"
        		+ "";*/
        String query = "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"
        		+ "PREFIX dbpprop: <http://dbpedia.org/property/>"
        		+ "PREFIX dbres: <http://dbpedia.org/resource/>"
        		+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
        		+ "SELECT ?p WHERE {"
        		+ " ?p rdf:type dbpedia-owl:TennisPlayer. "
        		+ " ?p dbpprop:turnedpro ?y. "
        		+ " ?p dbpprop:wimbledonresult ?w."
        		+ " FILTER(?w != \"N/A\"@en)."
        		+ " FILTER(?w != \"–\"@en)."
        		+ " FILTER(?w != \"-\"@en)."
        		+ " FILTER(?w != \"A\"@en)"
        		+ " }"
        		+ "";
        //String query = "ASK { }";
       QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
        
        try {
        	ResultSet set = qe.execSelect();
            if (set!=null) {
                System.out.println(service + " is UP");
                int i = 0;
                while(set.hasNext()){
                    System.out.println(set.next().toString());
                    i++;
                }
                System.out.println("I found "+i+" results");
            } // end if
        } catch (QueryExceptionHTTP e) {
            System.out.println(service + " is DOWN");
        } finally {
            qe.close();
        } // end try/catch/finally
    } // end method
    
    public static String addWimbledonPlayerType() throws IOException{
    	String result = "";
    	
    	String var_type = "resources/ontologies.xml#Wimbledon2014Player";
    	String rdf_type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
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
        
    	
    	File file = new File("resources/participants.txt");
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(file));
		String text = null;	
		int i = 0;
		while ((text = reader.readLine()) != null) {
			try {
	        	set = qe.execSelect();
	        } catch (QueryExceptionHTTP e) {
	            System.out.println(service + " is DOWN");
	        }
			
			String[] names = text.split("\t")[1].split(", ");
		   	String playerRef = null;
		   	for(int n=0; n<names.length; n++){
		   		names[n] = flattenToAsciiLowerCase(names[n]);
		   	}
		   	
		   	while(set.hasNext()){
		   		boolean contains = true;
		   		QuerySolution tuple = set.next();
		   		for(String name : names){
		   			String var_names = tuple.get(var_name).toString();
		   			var_names = flattenToAsciiLowerCase(var_names);
		   			if(!var_names.contains(flattenToAsciiLowerCase(name))){
		   				contains = false;
		   			}
		   		}
                if(contains){
                	playerRef = "<"+tuple.get(var_person).toString()+"> <"+rdf_type+"> <"+var_type+">";
                }
            }
		   	if(playerRef!=null){
		   		result+=playerRef+"\n";
		   		i++;
		   	}
		   	else{
		   		System.out.println("Did not find: "+text);
		   	}
		}
		   
		if (reader != null) {
	       reader.close();
	    }
		result+="Counted: "+i;
		
		File writeFile = new File("resources/participants.nt");
		FileOutputStream is = new FileOutputStream(writeFile);
        OutputStreamWriter osw = new OutputStreamWriter(is);    
        Writer w = new BufferedWriter(osw);
        w.write(result);
        w.close();
        
		return result;
    }
    
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

} // end class