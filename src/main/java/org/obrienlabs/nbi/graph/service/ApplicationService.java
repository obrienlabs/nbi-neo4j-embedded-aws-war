/*
 * Michael O'Brien (2017)  Overly Enthusiastic - Science | Software | Hardware | Experimentation
 * michael at obrienlabs.org
 * https://github.com/obrienlabs
 * https://twitter.com/_mikeobrien
 * http://eclipsejpa.blogspot.ca/
 */
package org.obrienlabs.nbi.graph.service;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.graphdb.Node; 
//import org.neo4j.graphdb.Transaction;
// neo4j-java-driver 1.1.1
/*import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import static org.neo4j.driver.v1.Values.parameters;*/
import org.springframework.stereotype.Service;

//@TransactionConfiguration( transactionManager = "transactionManager", defaultRollback = true)
@Service("daoFacade")
//@Repository(value="daoFacade")
//@Component
//@Transactional // will get a javax.persistence.TransactionRequiredException: No transactional EntityManager available without it
public class ApplicationService implements ApplicationServiceLocal {
	
	//private @Value("${server}") String server; // aws only
	private Log log = LogFactory.getLog(ApplicationService.class);

	@Inject
	private IGraphDatabaseService graphDatabaseService;

	// url
	// http://localhost:8080/biometric-nbi/FrontController?action=graph
	@Override
	public String getGraph() {
			Node node  = graphDatabaseService.createNode();
		    return node.toString() + "+" + node.getId() + ":" + node.hashCode();
	}
	
	@Override
	public String pushBolt(Node node) {
    	return "true";
	}
		
    @Override
    public Boolean health() {
    	Boolean health = true;
    	// TODO: check database
    	return health;
    }

}
