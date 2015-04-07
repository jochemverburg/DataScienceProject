package Reasoner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.PrintUtil;

public class Executer {
	
	public static final String DBPEDIA = "http://dbpedia.org/resource/";
	public static final String RESOURCE_FILE = "file:///C:/Users/Jochem/Dropbox/UT/Master/DataScience/DataScienceProject/Wimbledonizer/resources/ontologies.xml#";
	public static final String DEFEAT = RESOURCE_FILE+"def";
	public static final String WIMBLEDON_PLAYER = RESOURCE_FILE+"Wimbledon2014Player";
	public static final String RDF_TYPE = "rdf:type";
	
	public static void main(String[] args) {
		/*Model data = FileManager.get().loadModel("resources/test.nt");
		InfModel infmodel = ModelFactory.createRDFSModel(data);
		ValidityReport validity1 = infmodel.validate();
		if (validity1.isValid()) {
		    System.out.println("OK");
		} else {
		    System.out.println("Conflicts");
		    for (Iterator i = validity1.getReports(); i.hasNext(); ) {
		        System.out.println(" - " + i.next());
		    }
		}*/
		String resources = "resources/results.nt";
		//resources = "resources/exampleTooManyDefeats.nt";
		Executer newEx = new Executer("resources/ontologies.xml",resources);
		
		Resource nadal = newEx.getResource(DBPEDIA+"Murray%E2%80%93Nadal_rivalry");
		newEx.printStatements(nadal, null, null);
		//newEx.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
		//newEx.getProperty("file:///C:/Users/Jochem/Dropbox/UT/Master/DataScience/2.Semantics/Workspace/SW-Assignment2/data/2.6-b-gedcom.xml#parentIn")
		//newEx.printStatements(beatrix, newEx.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), null);
		//testEx.printStatements(nForce, null, null);
		
		ValidityReport validity = newEx.getInfModel().validate();
		if (validity.isValid()) {
		    System.out.println("OK");
		} else {
		    System.out.println("Conflicts");
		    for (Iterator i = validity.getReports(); i.hasNext(); ) {
		        ValidityReport.Report report = (ValidityReport.Report)i.next();
		        System.out.println(" - " + report);
		    }
		}
	}
	
	private InfModel infmodel;
	
	public Executer(String schemaFileNameorURI, String dataFileNameorURI){
		Model schema = FileManager.get().loadModel(schemaFileNameorURI);
		Model data = FileManager.get().loadModel(dataFileNameorURI);
		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		reasoner = reasoner.bindSchema(schema);
		infmodel = ModelFactory.createInfModel(reasoner, data);
	}
	
	public InfModel getInfModel(){
		return infmodel;
	}
	
	public Property getProperty(String uri){
		return getInfModel().getProperty(uri);
	}
	
	public Resource getResource(String uri){
		return getInfModel().getResource(uri);
	}
	
	public void printStatements(Resource s, Property p, Resource o) {
	    for (StmtIterator i = getInfModel().listStatements(s,p,o); i.hasNext(); ) {
	        Statement stmt = i.nextStatement();
	        System.out.println(" - " + PrintUtil.print(stmt));
	    }
	}
	
	public List<Resource> listResourcesOfClass(Resource type){
		List<Resource> result = new ArrayList<Resource>();
		for (StmtIterator i = getInfModel().listStatements(null,getProperty(RDF_TYPE),type); i.hasNext(); ) {
			Statement stmt = i.nextStatement();
			result.add(stmt.getSubject());
		}
		return result;
	}
	
	/*public Map<Resource,List<Resource>> findMultipleLost(List<Resource> players){
		Map<Resource,List<Resource>> result = new HashMap<Resource,List<Resource>>();
		Property def = getProperty(DEFEAT);
		for(Resource player : players){
			List<Resource> winner = new ArrayList<Resource>();
			for (StmtIterator i = getInfModel().listStatements(null,getProperty(RDF_TYPE),type); i.hasNext(); ) {
				Statement stmt = i.nextStatement();
				state
			}
			getInfModel().listStatements(null,def,player);
		}
		return result;
	}*/
	
}
