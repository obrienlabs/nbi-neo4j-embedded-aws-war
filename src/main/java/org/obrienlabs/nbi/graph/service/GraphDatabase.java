/*
 * Michael O'Brien (2017)  Overly Enthusiastic - Science | Software | Hardware | Experimentation
 * michael at obrienlabs.org
 * https://github.com/obrienlabs
 * https://twitter.com/_mikeobrien
 * http://eclipsejpa.blogspot.ca/
 */
package org.obrienlabs.nbi.graph.service;

import javax.inject.Inject;
import javax.inject.Named;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.ServerBootstrapper;

@Named("biometricGraphDatabase")
public class GraphDatabase implements IGraphDatabase {
	@Inject
	private GraphDatabaseService graphDb;

    private String path = null;
    
    private Thread shutdownHook;

    private ServerBootstrapper server = null;

    public GraphDatabase()
    {
        shutdownHook = new Thread()
        {
            @Override
            public void run()
            {
                GraphDatabase.this.stop();
                
            }
        };
    }
    
    public GraphDatabase(String aInPath)
    {
        path = aInPath;
        shutdownHook = new Thread()
        {
            @Override
            public void run()
            {
                GraphDatabase.this.stop();
            }
        };
    }

    //@Override
    // For non-Spring (standalone) execution
    public void start()
    {
    }

    //@Override
    // For non-Spring (standalone) execution
    public void stop()
    {
        if (graphDb != null)
        {
            server.stop();
            graphDb.shutdown();
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            graphDb = null;
            server = null;
        }
    }

    @Override
    public GraphDatabaseService getDB()
    {
        return graphDb;
    }
	

}
