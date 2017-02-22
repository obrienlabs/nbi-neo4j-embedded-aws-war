package org.obrienlabs.nbi.presentation;

import java.util.concurrent.ForkJoinPool;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;


public class ForkJoinGraphServer {
    private ForkJoinPool mapReducePool;

    private static String ip;
    private static final String username =  "neo4j";
    private static String port;
    private static String pass;
    private static int power;
    private static int threadMult;
    // serial (batch 100)
    // 16384 1t 265 sec 12%
    
    // parallel (batch 128)
    // 14 8 th 75 sec 96%
    // 14 16 th 89 sec 80%
    // 14 32 th 27 sec 60%
    
	public void compute(long startId, long endId, long length) {
		ForkJoinGraphUnitOfWork forkJoinUOW = new ForkJoinGraphUnitOfWork(startId, endId, length,pass, port, ip, threadMult);
		int threads = Runtime.getRuntime().availableProcessors() * threadMult;
		long startTime = System.currentTimeMillis();
		// create a ppol of threads to the power of pool * # of (proc + ht)
		mapReducePool = new ForkJoinPool(1);//threads);
		mapReducePool.invoke(forkJoinUOW);
		long endTime = System.currentTimeMillis();
		System.out.println(new StringBuffer(
				String.valueOf(endTime - startTime)).append(",")
				.append(threads).append(",")
				.append(threads).append(",")
				.append(String.valueOf((endTime - startTime) / 1000))
				.append(threads).append(",").append(power).toString());
	}
	
	 public void clean(boolean delete, int power) {
		   //long count = 0;
		   int lastPower = 128;
		   Driver driver = GraphDatabase.driver("bolt://" + ip + ":" + port,  AuthTokens.basic(username, pass)); 
	       try (Session session = driver.session()) {
	    	   if(delete) {
	    		   try (Transaction writeTransaction = session.beginTransaction()) {
	    			   for(int i=0;i<lastPower + 1;i++) {
	    				   writeTransaction.run(" match (:Node0)-[r:L" + i + "]->(:Node0) delete r");
	    			   }
	    			   writeTransaction.run(" match(n) delete(n)");
	    			   writeTransaction.success();
	    		   }
	    	   }
	       }
	       driver.close();
	 } 
	 
	public static void main(String[] args) {
	    System.out.println("ForkJoinGraphServer threadMult, dim, del, pass, port, ip (v 20170208)");
		int del = 0;
		boolean delete = false;
		if (args.length > 5) {
		    power = Integer.parseInt(args[0]);
	        del = Integer.parseInt(args[1]);
	        pass = args[2];
	        port = args[3];
	        ip = args[4];
	        threadMult = Integer.parseInt(args[5]);
	        if(del > 0) {
	        	delete = true;
	        }
		} else {
			System.out.println("Usage dimensions pre-delete/0/1 password port");
			System.exit(1);;
		}
		System.out.println("availableProc\t: " + Runtime.getRuntime().availableProcessors());
		System.out.println("fjps threads\t: " + power + " thread mult " + threadMult);
		System.out.println("freeMemory()\t: " + Runtime.getRuntime().freeMemory());
		System.out.println("maxMemory()\t: " + Runtime.getRuntime().maxMemory());
		System.out.println("totalMemory()\t: " + Runtime.getRuntime().totalMemory());
		System.out.println("System.getEnv()\t: " + System.getenv().toString());
		ForkJoinGraphServer server = new ForkJoinGraphServer();
		//long extent = 25;
		server.clean(delete, power);
		//System.out.println("Range: bits\t: " + extent);
		//for(long r=0;r<runs;r++) {
			//for(long p=poolStart;p<poolEnd + 1;p++) {
				//for(long i=3;i<extent + 1;i++) {
					server.compute(0L, Long.valueOf(1 << power), Long.valueOf(1 << power));	
				//}
			//}
		//}
	}
}
