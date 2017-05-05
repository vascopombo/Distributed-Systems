package org.komparator.security.handler;

import java.util.Set;

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

import org.komparator.security.CryptoUtil;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

public class FreshnessTestHandler  implements SOAPHandler<SOAPMessageContext> {
	private final String token = "fail";
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		System.out.println();
		

		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try {
			if (outboundElement.booleanValue()) {
				
				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();
				// check header
				if (sh == null) {
					return true;
				}
				// get first header element
				Name name = se.createName("tokenHeader", "d", "urn:tokens");
				Iterator it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					return true;
				}
				
				System.out.println("[FreshnessTestHandler:BEGIN]");				
				SOAPElement element = (SOAPElement) it.next();
				

				System.out.println("Switching token for a constant one...");
				element.setValue(token);
				
				
				System.out.println("Token switched.");				
				System.out.println("[FreshnessTestHandler:END]");	
				
				
			}
			}catch (Exception e) {
				System.out.print("Caught exception in handleMessage: ");
				System.out.println(e);
				System.out.println("Continue normal processing...");
			}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		System.out.println("Ignoring fault message...");
		return true;
	}

	@Override
	public void close(MessageContext smc) {
		// nothing to clean up
	}

	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

}
