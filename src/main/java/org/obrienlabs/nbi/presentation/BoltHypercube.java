package org.obrienlabs.nbi.presentation;

import static org.neo4j.driver.v1.Values.parameters;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

public class BoltHypercube {

	public static long linkStep = -1;
	public static long linkCount = 0;
	public static long pk = 0;
	public static long pair = 0;
	public static long linkMax = 0;
    private static final String ip = "127.0.0.1";
    private static final String username =  "neo4j";
    private static String port ;
    private static String pass; 
	
    // dbms.logs.query.threshold=9
    // dbms.logs.query.enabled=true
    
    // node
    // MATCH (a:Node0) WHERE a.name = {p0}  return a
    // with relationships
    
    private void queryPage() {
    		Driver driver = GraphDatabase.driver("bolt://" + ip + ":" + port,  AuthTokens.basic(username, pass)); 
    		try (Session session = driver.session()) {
    			try (Transaction aTransaction = session.beginTransaction()) {
    				// get count
    				// iterate
    				StatementResult result  = aTransaction.run(
    						//new StringBuffer("MATCH (a:Node0 {name: {p0}})  return a").toString(),
    						//new StringBuffer("MATCH (a) return a AS name ORDER BY name DESC LIMIT 10").toString(), // no
    						new StringBuffer("MATCH (a) return a SKIP 1 LIMIT 10").toString(),
    							parameters( "p0", 0));
    				aTransaction.success();
    				if(result.hasNext()) {
    					System.out.println("Size: " + result.keys().size());
    					while(result.hasNext()) {
    						System.out.println(new StringBuffer(String.valueOf(System.currentTimeMillis())).append(",").append(result.next())
    							.toString());
    					}
    				}
    			}
    			driver.close();
    			System.out.println(System.currentTimeMillis() + ", end");
    		}
    }
    
	private void linkUnidirectionalNoTx(Transaction writeTransaction, long diffTime, String label, long pair0, long pair1, long pk0, long pk1) {
       		StatementResult edge0 = writeTransaction.run(
       						new StringBuffer("MATCH (a:Node").append(pair0).append("),(b:Node").append(pair1)
       							.append(") WHERE a.name = {p0} AND b.name = {p1} CREATE (a)-[r:")
       							.append(label).append("]->(b) return r").toString(),
                       parameters( "p0", pk0, "p1", pk1));
       		linkStep--;
       		if(linkStep < 0) {
       			linkStep = 99;
       			System.out.println(new StringBuffer(String.valueOf(System.currentTimeMillis())).append(",").append(diffTime)
       			.append(" ,").append(linkCount).append(" ,").append(linkMax).toString());
       		}
       		linkCount++;
	}
	private void linkUnidirectional(long diffTime, String label, long pair0, long pair1, long pk0, long pk1, Session session) {
        try (Transaction writeTransaction = session.beginTransaction()) {
       		StatementResult edge0 = writeTransaction.run(
       						new StringBuffer("MATCH (a:Node").append(pair0).append("),(b:Node").append(pair1)
       							.append(") WHERE a.name = {p0} AND b.name = {p1} CREATE (a)-[r:")
       							.append(label).append("]->(b) return r").toString(),
                       parameters( "p0", pk0, "p1", pk1));
       		writeTransaction.success();
       		linkStep--;
       		if(linkStep < 0) {
       			linkStep = 99;
       			System.out.println(new StringBuffer(String.valueOf(System.currentTimeMillis())).append(",").append(diffTime)
       			.append(" ,").append(linkCount).append(" ,").append(linkMax).toString());
       		}
       		linkCount++;
       }
	}
	
	private void linkBidirectional(long lastTime, String label, long pair0, long pair1, long pk0, long pk1, Session session) {
         try (Transaction writeTransaction = session.beginTransaction()) {
        		StatementResult edge0  = 
        				writeTransaction.run( 
        				"MATCH (a:Node" + pair0 + "),(b:Node" + pair1 + ") WHERE a.name = {p0} AND b.name = {p1} CREATE (a)-[r:" 
        				+ label + "]->(b) return r",
                        parameters( "p0", pk0, "p1", pk1));
        		StatementResult edge1  = 
        				writeTransaction.run( 
        				"MATCH (a:Node" + pair1 + "),(b:Node" + pair0 + ") WHERE a.name = {p0} AND b.name = {p1} CREATE (a)-[r:" 
        				+ label + "]->(b) return r",
                        parameters( "p0", pk1, "p1", pk0));
        		writeTransaction.success();
        		linkStep--;
        		linkCount++;
        		//if(linkStep < 0) {
        			//linkStep = 16;
        			System.out.println((System.currentTimeMillis() - lastTime) + ", link: " + pk0 + "-" + pk1 + " : " + linkCount + " of " + linkMax);
        		//}
        }
	}

	private void createNoTx(Transaction transaction) {
		transaction.run( "CREATE (a:Node" + pair + " {name: {pk}})", parameters( "pk", pk));
        pk++;
	}
	
	private void create(long pk0, long pair, Session session) {
        try (Transaction writeTransaction = session.beginTransaction()) {
        		writeTransaction.run( "CREATE (a:Node" + pair + " {name: {pk}})", parameters( "pk", pk));
        		pk++;
        		writeTransaction.success();
        		//System.out.print(new StringBuffer(String.valueOf(pk)).append(".").toString());
        }
	}

	private void createRecursive(int power, long id, Session session) {	
		// base step
		if(power == 0) {
	        create(id,  0, session);
	        create(id + 1, 1,  session);
	        linkBidirectional(System.currentTimeMillis(), "L0", 0, 1, id ,id + 1, session); 
	        return;
		} else {
			// recursive step
			int ext = 1 << power;
			createRecursive(power - 1, id, session);
			createRecursive(power - 1, id + ext, session);	
			for(int i=0; i<ext; i+=2) {
				linkBidirectional(System.currentTimeMillis(), "L" + power, 0, 0, id + i, id + ext + i, session);
				linkBidirectional(System.currentTimeMillis(), "L" + power, 1,1, id + i + 1, id + ext + i + 1, session);
			}
		}
	}

	private Transaction startTransaction(Session session) {
		return session.beginTransaction();
	}
	
	private void endTransaction(Transaction transaction) {
		transaction.success();
		transaction.close();
	}
	
	private void createListNoTx(int power, long id, Session session) {	
	        create(id,  0, session);
	        long lastTime = System.currentTimeMillis();
	        long diffTime = 0;
	        long batchCount = 0;
	        long batchMax = 100;	
	        Transaction transaction = startTransaction(session);
	        for(long link=1; link<linkMax; link++) {
	        		diffTime = System.currentTimeMillis() - lastTime;
	        		lastTime = System.currentTimeMillis();
	        		createNoTx(transaction);
	        		linkUnidirectionalNoTx(transaction, diffTime, "L0", 0, 0, link - 1, link);
	        		if(batchCount < batchMax) {
	        			batchCount++;
	        		} else {
	        			batchCount = 0;
	        			endTransaction(transaction);
	        			transaction = startTransaction(session);
	        		}
	        }
	        endTransaction(transaction);
	}

	private void createList(int power, long id, Session session) {	
        create(id,  0, session);
        long lastTime = System.currentTimeMillis();
        long diffTime = 0;
        for(long link=1; link<linkMax; link++) {
        	diffTime = System.currentTimeMillis() - lastTime;
        	lastTime = System.currentTimeMillis();
        	create(link, 0,  session);
        	linkUnidirectional(diffTime, "L0", 0, 0, link - 1, link, session);
        }
}
	 public void populateListSerial(boolean delete, int power) {
		   linkMax = 1 << (power + 1) ;
		   System.out.println(System.currentTimeMillis() + ", start");
	       Driver driver = GraphDatabase.driver("bolt://" + ip + ":" + port,  AuthTokens.basic(username, pass)); 
	       try (Session session = driver.session()) {
	        	createListNoTx(power, 0, session);
	       }
	       driver.close();
		   System.out.println(System.currentTimeMillis() + ", end");
	    }
	
	// match(n:Node0) where n.name=0 return(n);
	 public void populateHypercubeSerial(boolean delete, int power) {
		   System.out.println(System.currentTimeMillis() + ", start");
		   linkMax = (1 <<(power - 1)) * (power);
	       Driver driver = GraphDatabase.driver("bolt://" + ip + ":" + port,  AuthTokens.basic(username, pass)); 
	       try (Session session = driver.session()) {
	        	createRecursive(power, 0, session);
	       }
	       driver.close();
		   System.out.println(System.currentTimeMillis() + ", end");
	    }
	 
	 public void clean(boolean delete, int power) {
		   int lastPower = 22;
		   linkMax = (1 << power) * (power + 1);
		   Driver driver = GraphDatabase.driver("bolt://" + ip + ":" + port,  AuthTokens.basic(username, pass)); 
	       try (Session session = driver.session()) {
	    	   if(delete) {
          		try (Transaction writeTransaction = session.beginTransaction()) {
	            		for(int i=0;i<lastPower + 1;i++) {
	            			writeTransaction.run(" match (:Node0)-[r:L" + i + "]->(:Node0) delete r");
	            			writeTransaction.run(" match (:Node0)-[r:L" + i + "]->(:Node1) delete r");
	            			writeTransaction.run(" match (:Node1)-[r:L" + i + "]->(:Node0) delete r");
	            			writeTransaction.run(" match (:Node1)-[r:L" + i + "]->(:Node1) delete r");
	            		}
	            		writeTransaction.run(" match(n) delete(n)");
	            		writeTransaction.success();
	            	}
	            }
	        }
	        driver.close();
	    } 
	// https://github.com/neo4j/neo4j-java-driver/blob/1.1/examples/src/main/java/org/neo4j/docs/driver/MinimalWorkingExample.java
	
	public static void main(String[] args) {
		int dim = 1;
		int del = 0;
		boolean delete = false;
		BoltHypercube test = new BoltHypercube();
		if (args.length > 3) {
		    dim = Integer.parseInt(args[0]);
	        del = Integer.parseInt(args[1]);
	        pass = args[2];
	        port = args[3];
	        if(del > 0) {
	        	delete = true;
	        }
		} else {
			System.out.println("Usage dimensions pre-delete/0/1 password port");
			System.exit(1);;
		}
		try {
			long startTime = System.currentTimeMillis();
			//test.queryPage();
			test.clean(delete, dim);
			//test.populateHypercubeSerial(delete, dim);
//			test.populateListSerial(delete, dim);
			System.out.println("Dur: " + (System.currentTimeMillis() - startTime));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
