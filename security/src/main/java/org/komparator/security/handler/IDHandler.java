package org.komparator.security.handler;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

public class IDHandler implements SOAPHandler<SOAPMessageContext> {

	//public static final String CONTEXT_PROPERTY = "my.property";
	public static String wsName;

	//
	// Handler interface implementation
	//

	/**
	 * Gets the header blocks that can be processed by this Handler instance. If
	 * null, processes all.
	 */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/**
	 * The handleMessage method is invoked for normal processing of inbound and
	 * outbound messages.
	 */
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		System.out.println();


		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try {
			if (outboundElement.booleanValue()) {
				System.out.println("[IDHandler:BEGIN]");				
				System.out.println("Adding identifier to Header in message...");

				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				
				// add header
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();

				// add header element (name, namespace prefix, namespace)
				
				//Add identifier
				Name name = se.createName("identifier", "d", "urn:identifiers");
				SOAPHeaderElement element = sh.addHeaderElement(name);

				if(wsName == null){
					element.addTextNode("T35_Supplier1"); //This will only happen during tests.
				}
				else {
					element.addTextNode(wsName);
				}
				System.out.println("Identifier " + wsName + " added.");
				System.out.println("[IDHandler:END]");				
			} else {
				//System.out.println("Reading identifier from Header in message...");

				// get SOAP envelope header
				//SOAPMessage msg = smc.getMessage();
				//SOAPPart sp = msg.getSOAPPart();
				//SOAPEnvelope se = sp.getEnvelope();
				//SOAPHeader sh = se.getHeader();

				// check header
				//if (sh == null) {
				//	System.out.println("Header not found.");
				//	return true;
				//}

				//String endpointAddress = (String) smc.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
				//UDDINaming uddiNaming = new UDDINaming("http://t35:56vMW2kU@uddi.sd.rnl.tecnico.ulisboa.pt:9090/");
				//Collection<UDDIRecord> records = uddiNaming.listRecords("T35_Supplier%");
				//for( UDDIRecord record: records){
				//	if( record.getUrl().equals(endpointAddress)){
				//		wsName = record.getOrgName();
				//		}
				//}				
				
				
				// get first header element
				//Name name = se.createName("identifier", "d", "urn:identifiers");
				//Iterator it = sh.getChildElements(name);
				// check header element
				//if (!it.hasNext()) {
				//	throw new RuntimeException("Header not found.");
				//}
				//SOAPElement element = (SOAPElement) it.next();

				// get header element value
				//String valueString = element.getValue();
				
				//System.out.println("This is my wsName:" + wsName);
				//System.out.println("This is the message's destination:" + valueString);
				
				
				//if(valueString.equals("TestClient")){
				//	System.out.println("This SupplierClient was generated during Mediator tests. Allowing.");
				//} else {
				//if(!valueString.equals(wsName)){
				//	throw new RuntimeException("This message is not meant for this receiver!");
				//}
				//}
				
				//System.out.println("Identifier is correct.");
				// print received header
				//System.out.println("Header value is " + value);

				// put header in a property context
				//smc.put(CONTEXT_PROPERTY, wsName);
				// set property scope to application client/server class can
				// access it
				//smc.setScope(CONTEXT_PROPERTY, Scope.APPLICATION);

			}
		} catch (Exception e) {
			System.out.print("Caught exception in handleMessage: ");
			System.out.println(e);
			System.out.println("Continue normal processing...");
		}

		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		System.out.println("Ignoring fault message...");
		return true;
	}

	/**
	 * Called at the conclusion of a message exchange pattern just prior to the
	 * JAX-WS runtime dispatching a message, fault or exception.
	 */
	@Override
	public void close(MessageContext messageContext) {
		// nothing to clean up
	}
	

}
