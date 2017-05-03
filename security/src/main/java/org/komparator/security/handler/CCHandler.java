package org.komparator.security.handler;

import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Node;

public class CCHandler implements SOAPHandler<SOAPMessageContext> {

	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		System.out.println("AddHeaderHandler: Handling message.");

		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try {
			if (outboundElement.booleanValue()) {
				System.out.println("Teste2...");

				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPBody so = se.getBody();
				Node ns1 = so.getFirstChild();
				if(ns1.getNodeName().equals("ns2:buyCart")){
					String cc = ns1.getLastChild().getTextContent();
					cc = cc.concat("1234");
					ns1.getLastChild().setTextContent(cc);
					
					//System.out.println("CONTEUDO FINAL:");
					//System.out.println(smc.getMessage().getSOAPPart().getEnvelope().getBody().getFirstChild().getLastChild().getTextContent());
				}
				
				

			} else {
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPBody so = se.getBody();
				Node ns1 = so.getFirstChild();				
				if(ns1.getNodeName().equals("ns2:buyCart")){
					String cc = ns1.getLastChild().getTextContent();
					cc = cc.substring(0, cc.length() - 4);
					ns1.getLastChild().setTextContent(cc);
					System.out.println("CONTEUDO FINAL:");
					System.out.println(smc.getMessage().getSOAPPart().getEnvelope().getBody().getFirstChild().getLastChild().getTextContent());
					
				}
				
				
			}
		} catch (Exception e) {
			System.out.print("Caught exception in handleMessage: ");
			System.out.println(e);
			System.out.println("Continue normal processing...");
		}

		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close(MessageContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

}
