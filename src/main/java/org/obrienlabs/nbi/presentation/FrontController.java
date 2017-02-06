/*
 * Michael O'Brien (2017)  Overly Enthusiastic - Science | Software | Hardware | Experimentation
 * michael at obrienlabs.org
 * https://github.com/obrienlabs
 * https://twitter.com/_mikeobrien
 * http://eclipsejpa.blogspot.ca/
 */

package org.obrienlabs.nbi.presentation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.obrienlabs.nbi.graph.service.ApplicationServiceLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;


/**
 * <pattern>FrontController</pattern><br>
 * This class is the controller end of an active client first principles ajaxclient.jsp.
 * Normally a standard JSF .xhtml and @ManagedBean presentation bean would be used.
 * It is part of a distributed application framework used to simulate and research
 * concurrency, analytics, management, performance and exception handling.
 * The focus is on utilizing JPA 2.0 as the persistence layer for scenarios involving
 * multicore, multithreaded and multiuser distributed memory L1 persistence applications.
 * The secondary focus is on exercising Java EE6 API to access the results of this distributed application.
 * 
 * @see http://obrienscience.blogspot.com
 * @author Michael O'Brien
 * 
 * URL
 * http://localhost:8080/nbi-neo4j/FrontController?action=graph
 */
//@WebListener
public class FrontController extends HttpServlet {
    private static final long serialVersionUID = -312633509671504746L;
    
    @Autowired
    @Qualifier("daoFacade")
    private ApplicationServiceLocal service;// = new ApplicationService();
     
	// Reference the database specific persistence unit in persistence.xml
	public static final String PU_NAME_CREATE = "to";
	private long sentSeq = 1;
	private long recvSeq = 1;
    
    private static AtomicLong nextSessionId = new AtomicLong(1);
    //private Map<Long, AtomicLong> lastTimestampMap = new ConcurrentHashMap<>();
    //private Map<Long, AtomicLong> nextReadingSequenceIdMap = new ConcurrentHashMap<>();
    private static AtomicLong nextReadingSequenceId = new AtomicLong(1);

    public static final String EMPTY_STRING = "";
    public static final String FRONT_CONTROLLER_ACTION = "action";
    public static final String FRONT_CONTROLLER_ACTION_DEMO = "demo";

    public static final String FRONT_CONTROLLER_ACTION_GRAPH = "graph";   
    
    
    public FrontController() {
        super();
    }

    private void processSession() {
    }
    
    private void processDemoCommand(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String cell = request.getParameter("cell");
        int cellNumber = -1;
        if(null != cell) {
            cellNumber = Integer.parseInt(cell);
        }
        
        StringBuffer xmlBuffer = new StringBuffer();
        long number = System.nanoTime();
        xmlBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xmlBuffer.append("<state>");
        switch (cellNumber) {
        case 0:
            long random = Math.round(Math.random() * 1000);
        	try {
        		//xmlBuffer.append(service.captureImage("http://weather.gc.ca/data/lightning_images/NAT.png"));
        	} catch (Exception e) {
        		e.printStackTrace(out);
        	}
            xmlBuffer.append(random);
            break;
        case 1:
            xmlBuffer.append(number);
            break;
        default:
            xmlBuffer.append(number);
            break;
        }
        xmlBuffer.append("</state>");
        out.println(xmlBuffer.toString());        
        //StringBuffer outBuffer = new StringBuffer("Thread: ");        
        System.out.println("_xml: " + xmlBuffer.toString());
    }
    
    private void processAction(HttpServletRequest aRequest, HttpServletResponse aResponse) {
        PrintWriter out = null;
        try {
                //HttpSession aSession = aRequest.getSession(true);

                String action = aRequest.getParameter(FRONT_CONTROLLER_ACTION);
                if(null == action) {
                    action = FRONT_CONTROLLER_ACTION_DEMO;
                }
            	System.out.println("processAction: " + action);
                // Process requests
                if(action.equalsIgnoreCase(FRONT_CONTROLLER_ACTION_DEMO)) {
                    aResponse.setContentType("text/xml");
                    Writer writer = new BufferedWriter(new OutputStreamWriter(aResponse.getOutputStream(),"UTF-8"));
                    out = new PrintWriter(writer, true);
                    processDemoCommand(aRequest, aResponse, out);
                }
                if(action.equalsIgnoreCase("graph")) {
                    aResponse.setContentType("text/xml");
                    Writer writer = new BufferedWriter(new OutputStreamWriter(aResponse.getOutputStream(),"UTF-8"));
                    out = new PrintWriter(writer, true);
                    processGraph(aRequest, aResponse, out);
                }                                                     
        } catch (Exception e) {
            	e.printStackTrace();
        }
    }
    
    
    private void processBolt(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {

    }
    
    private void processGraph(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        //String user = request.getParameter("u");        
        StringBuffer xmlBuffer = new StringBuffer();
        
        xmlBuffer.append("<state>");
        xmlBuffer.append(service.getGraph());
       
        xmlBuffer.append("</state>");
        System.out.println(xmlBuffer.toString());
        out.println(xmlBuffer.toString()); 
    }
    
	@Override
    public void init(ServletConfig config) throws ServletException {
		super.init(config);
		// enable autowiring in servlets
		SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
			      config.getServletContext());
	}

	@Override
    public ServletConfig getServletConfig() {
		return null;
	}

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processAction(request, response);
	}

	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processAction(request, response);
	}

	@Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	@Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	@Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}
}
