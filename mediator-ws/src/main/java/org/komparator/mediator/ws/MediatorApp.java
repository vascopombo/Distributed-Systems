package org.komparator.mediator.ws;

import org.komparator.security.handler.IDHandler;

public class MediatorApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length == 0 || args.length == 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + MediatorApp.class.getName() + " wsURL primary OR uddiURL wsName wsURL primary");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;

		// Create server implementation object, according to options
		MediatorEndpointManager endpoint = null;
		if (args.length == 2) {
			wsURL = args[0];
			endpoint = new MediatorEndpointManager(wsURL, args[1].equals("1"));
		} else if (args.length >= 3) {
			uddiURL = args[0];
			wsName = args[1];
			IDHandler.wsName = wsName;
			wsURL = args[2];
			endpoint = new MediatorEndpointManager(uddiURL, wsName, wsURL, args[3].equals("1"));
			endpoint.setVerbose(true);
		}
		LifeProof.endpoint = endpoint;

		try {
			endpoint.start();
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}

	}

}
