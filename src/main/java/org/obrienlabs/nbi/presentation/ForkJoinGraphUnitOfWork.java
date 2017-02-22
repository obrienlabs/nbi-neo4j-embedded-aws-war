package org.obrienlabs.nbi.presentation;

import static org.neo4j.driver.v1.Values.parameters;

import java.util.concurrent.RecursiveAction;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

public class ForkJoinGraphUnitOfWork extends RecursiveAction {
	private static final long serialVersionUID = 2854227036188716499L;
	private long startId;
	private long endId;
	private long length;
	
    private String ip;
    private static final String username =  "neo4j";
    private String port;
    private String pass; 
    private long batchSize;
    private int threadMult;
    private boolean rel;
	
	public ForkJoinGraphUnitOfWork(long start, long _end, long _length, String pass, String port, String ip, int threadMult, long batchSize, boolean rel) {
		startId = start;
		length = _length;	
		endId = _end;
		this.ip = ip;
		this.port = port;
		this.threadMult = threadMult;
		this.pass = pass;
		this.batchSize = batchSize;
		this.rel = rel;
	}	
	
	private void endTransaction(Transaction transaction) {
		transaction.success();
		transaction.close();
	}
	
	private void create(long pk0, long pair, Session session) {
        try (Transaction writeTransaction = session.beginTransaction()) {
        		writeTransaction.run( "CREATE (a:Node0 {name: {pk}})", parameters( "pk", pk0));
        		writeTransaction.success();
        }
	}
	
	private void linkUnidirectionalNoTx(Transaction transaction, String label, long pair0, long pair1, long pk0, long pk1) {
   		transaction.run(
   				new StringBuffer("MATCH (a:Node0),(b:Node0) WHERE a.name = {p0} AND b.name = {p1} CREATE (a)-[r:")
   					.append(label).append("]->(b)").toString(),
                   parameters( "p0", pk0, "p1", pk1));
	}
	
	protected void computeNoFork() {
		//System.out.println(new StringBuffer(String.valueOf(System.currentTimeMillis())).append(", start: ").append(startId)
		//		.append(" end: ").append(endId).append(" length: ").append(length).toString());
		Driver driver = GraphDatabase.driver("bolt://" + ip + ":" + port,  AuthTokens.basic(username, pass)); 
		try (Session session = driver.session()) {
			create(startId,  0, session);
			//long batchCount = 0;
			//ong batchMax = 100;	
			Transaction transaction = session.beginTransaction();
			for(long link=startId + 1; link<endId; link++) {
        		transaction.run( "CREATE (a:Node0 {name: {pk}})", parameters( "pk", link));
        		if(rel) {
        			linkUnidirectionalNoTx(transaction, "L0", 0, 0, link - 1, link);
        		}
        		//if(batchCount < batchMax) {
        		//	batchCount++;
        		//} else {
        		//	batchCount = 0;
        		//	endTransaction(transaction);
        		//	transaction = session.beginTransaction();
        		//}
			}
			endTransaction(transaction);
		}
		driver.close();
		//System.out.println(new StringBuffer(String.valueOf(System.currentTimeMillis())).append(", end ").append(endId).toString());
	}
	
    @Override
	protected void compute() {
    	// base case
	    if (length < batchSize) {
	        computeNoFork();
	        return;
	    }
	    
	    long split = length >> 1;
	    // recursive case
	    invokeAll(
	    		new ForkJoinGraphUnitOfWork(startId, startId + split, split, pass, port, ip, threadMult, batchSize, rel),
	    		new ForkJoinGraphUnitOfWork(startId + split, endId, split, pass, port, ip, threadMult, batchSize, rel)
	    );		
	}
}
