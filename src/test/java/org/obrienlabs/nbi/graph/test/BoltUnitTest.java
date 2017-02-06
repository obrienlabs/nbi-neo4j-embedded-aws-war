package org.obrienlabs.nbi.graph.test;

import static org.neo4j.driver.v1.Values.parameters;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

public class BoltUnitTest {

	public static long linkStep = 0;
	public static long linkCount = 0;
	public static long pk = 0;
	public static long pair = 0;
	public static long linkMax = 0;
    private static final String url = "bolt://192.168.0.26:7687";
    private static final String username =  "neo4j";
    private static final String password = "password"; 
	
	private void link(String label, long pair0, long pair1, long pk0, long pk1, Session session) {
         try (Transaction writeTransaction = session.beginTransaction()) {
        		writeTransaction.success();
        		writeTransaction.run( 
        				"MATCH (a:Node"+ pair0 + "),(b:Node"+ pair1 + ") WHERE a.name = {p0} AND b.name = {p1} CREATE (a)-[r:" + label + "]->(b)",
                        parameters( "p0", pk0, "p1", pk1));
        		StatementResult edge1  = writeTransaction.run( 
        				"MATCH (a:Node"+ pair0 + "),(b:Node"+ pair1 + ") WHERE a.name = {p0} AND b.name = {p1} CREATE (a)-[r:" + label + "]->(b) RETURN r",
                        parameters( "p0", pk1, "p1", pk0));
        		writeTransaction.success();
        		linkStep--;
        		linkCount++;
        		if(linkStep < 0) {
        			linkStep = 16;
        			System.out.println("link: " + pk0 + "-" + pk1 + " : " + linkCount + " of " + linkMax);
        		}
        }
	}

	private void create(long pk0, long pair, Session session) {
        try (Transaction writeTransaction = session.beginTransaction()) {
        		StatementResult left  = writeTransaction.run( "CREATE (a:Node" + pair + " {name: {pk}}) return a",
                    parameters( "pk", pk));
        		pk++;
        		writeTransaction.success();
        }
	}

	private void createL(int power, long id, Session session) {	
		// base step
		if(power == 0) {
	        create(id,  0, session);
	        create(id + 1, 0,  session);
	        link("L0", 0, 0, id ,id + 1, session);  //pair++;
	        return;
		} else {
			// recursive step
			int ext = 1 << power;
			createL(power - 1, id, session);
			createL(power - 1, id + ext, session);	
			for(int i=0; i<ext; i++) {
				link("L" + power, 0, 0, id + i, id + ext + i, session);
			}
		}
	}
	
	// match(n:Node0) where n.name=0 return(n);
	 public void populateHypercube() throws Exception {
		   long count = 0;
		   int lastPower = 16;
		   int power = 16;
		   linkMax = (1 << power) * (power + 1);
	        Driver driver = GraphDatabase.driver(url,  AuthTokens.basic(username, password)); 
	        try (Session session = driver.session()) {
	            try (Transaction writeTransaction = session.beginTransaction()) {
	            	for(int i=0;i<lastPower + 1;i++) {
	            		writeTransaction.run(" match (:Node0)-[r:L" + i + "]->(:Node0) delete r");
	            	}
	        		 writeTransaction.run(" match(n) delete(n)");
	        		 writeTransaction.success();
	            }
	        	createL(power, count, session);
	        }
	        driver.close();
	    }
	 
	// https://github.com/neo4j/neo4j-java-driver/blob/1.1/examples/src/main/java/org/neo4j/docs/driver/MinimalWorkingExample.java
	
	public static void main(String[] args) {
		BoltUnitTest test = new BoltUnitTest();
		try {
			test.populateHypercube();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
