package org.komparator.security.handler;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
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
import javax.xml.ws.soap.SOAPFaultException;

import org.komparator.security.CryptoUtil;

import com.sun.xml.ws.developer.JAXWSProperties;

public class TokenHandler implements SOAPHandler<SOAPMessageContext> {

	//public static final String CONTEXT_PROPERTY = "my.property";
	private static List<String> tokens = new ArrayList<String>();
	

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
		System.out.println("[TokenHandler:BEGIN]");	
		
		
		
		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try {
			if (outboundElement.booleanValue()) {
				System.out.println("Adding token to header in message...");

				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				// add header
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();
				// add header element (name, namespace prefix, namespace)
				
				//TOKEN
				Name name = se.createName("tokenHeader", "d", "urn:tokens");
				SOAPHeaderElement element = sh.addHeaderElement(name);
				// add header element value
				System.out.println("Generating random number...");
				String value = CryptoUtil.byteToString(CryptoUtil.generateRandomNumber());
				element.addTextNode(value);
				System.out.println("Token added.");
				
								
			} else {
				System.out.println("Verifying token from header in message...");

				// get SOAP envelope header
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();

				// check header
				if (sh == null) {
					System.out.println("Header not found.");
					//throw new RuntimeException("Header not found.");
					return true;
				}

				// get token from header
				Name name = se.createName("tokenHeader", "d", "urn:tokens");
				Iterator it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					throw new RuntimeException("Token not found.");
				}
				SOAPElement element = (SOAPElement) it.next();

				// get header element value
				String valueString = element.getValue(); 
				//byte[] tkn = CryptoUtil.stringToByte(valueString);
				System.out.println("Checking token against previously used tokens...");
				
				
				if(tokens.contains(valueString)){
					throw new RuntimeException("This token has already been used!");					
				}
				else {
					System.out.println("Received token is valid.");
					System.out.println("New token has been added to the list.");
					tokens.add(valueString);
				}
				
				// put header in a property context
				//smc.put(CONTEXT_PROPERTY, value);
				// set property scope to application client/server class can
				// access it
				//smc.setScope(CONTEXT_PROPERTY, Scope.APPLICATION);

			}
		}catch (RuntimeException e){
			System.out.println(e);
			throw new SOAPFaultException(null);
		} catch (Exception e) {
			System.out.print("Caught exception in handleMessage: ");
			System.out.println(e);
			System.out.println("Continue normal processing...");
		}
		System.out.println("[TokenHandler:END]");	
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
