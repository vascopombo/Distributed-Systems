package org.komparator.supplier.ws;

import java.io.IOException;
import java.rmi.Naming;

import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;


/** End point manager */
public class SupplierEndpointManager {

	/** Web Service location to publish */
	private String wsURL = null;
	private String wsName = null;
	private String uddiUrl = null;

	/** Port implementation */
	private SupplierPortImpl portImpl = new SupplierPortImpl(this);

// TODO
//	/** Obtain Port implementation */
	public SupplierPortType getPort() {
		return portImpl;
	}

	/** Web Service end point */
	private Endpoint endpoint = null;

	/** Uddi **/
	private UDDINaming uddiNaming = null;
	
	/** output option **/
	private boolean verbose = true;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided web service URL */
	public SupplierEndpointManager(String wsURL) {
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		this.wsURL = wsURL;
	}
	
	/** constructor for new uddi-based web service */
	public SupplierEndpointManager(String url,String name, String uddi) {
		this.wsURL = url;
		this.wsName = name;
		this.uddiUrl = uddi;
	}	

	/* end point management */

	public void start() throws Exception {
		try {
			if(this.uddiUrl == null){
				// publish end point
				endpoint = Endpoint.create(this.portImpl);
				if (verbose) {
					System.out.printf("Starting %s%n", wsURL);
				}
				endpoint.publish(wsURL);
				}
				
			else {
				endpoint = Endpoint.create(this.portImpl);				
				// publish to uddi
				System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, wsURL);
				uddiNaming = new UDDINaming(uddiUrl);
				uddiNaming.rebind(wsName, wsURL);
				endpoint.publish(wsURL);
			}
			
			
		} catch (Exception e) {
			endpoint = null;
			if (verbose) {
				System.out.printf("Caught exception when starting: %s%n", e);
				e.printStackTrace();
			}
			throw e;
		}
	}
	
//	public void start_ws() throws Exception {
//		try {
//			// publish to uddi
//			System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, uddiUrl);
//			uddiNaming = new UDDINaming(uddiUrl);
//			uddiNaming.rebind(wsName, uddiUrl);
//		} catch (Exception e) {
//			endpoint = null;
//			if (verbose) {
//				System.out.printf("Caught exception when starting: %s%n", e);
//				e.printStackTrace();
//			}
//			throw e;
//		}
//	}	

	public void awaitConnections() {
		if (verbose) {
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
		}
		try {
			System.in.read();
		} catch (IOException e) {
			if (verbose) {
				System.out.printf("Caught i/o exception when awaiting requests: %s%n", e);
			}
		}
	}

	public void stop() throws Exception {
		try {
			if(this.uddiUrl == null){
				if (endpoint != null) {
					// stop end point
					endpoint.stop();
					if (verbose) {
						System.out.printf("Stopped %s%n", wsURL);
					}
				}
			}
			else {
				if (uddiNaming != null) {
					// stop end point
					uddiNaming.unbind(wsName);
					endpoint.stop();
					if (verbose) {
						System.out.printf("Deleted '%s' from UDDI%n", wsName);
					}
				}				
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when stopping: %s%n", e);
			}
		}
		this.portImpl = null;
	}

//	public void stop_ws() throws Exception {
//		try {
//			if (uddiNaming != null) {
//				// stop end point
//				uddiNaming.unbind(wsName);
//				if (verbose) {
//					System.out.printf("Deleted '%s' from UDDI%n", wsName);
//				}
//			}
//		} catch (Exception e) {
//			if (verbose) {
//				System.out.printf("Caught exception when stopping: %s%n", e);
//			}
//		}
//		this.portImpl = null;
//	}

}
