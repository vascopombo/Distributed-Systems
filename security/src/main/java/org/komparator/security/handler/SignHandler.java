package org.komparator.security.handler;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
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
import javax.xml.ws.soap.SOAPFaultException;

import org.komparator.security.CryptoUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;

public class SignHandler implements SOAPHandler<SOAPMessageContext> {

	//public static final String CONTEXT_PROPERTY = "my.property";

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
		System.out.println("[SignHandler:BEGIN]");	
		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try {
			if (outboundElement.booleanValue()) {

				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				
				
				// add header
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();
				
				// add header element (name, namespace prefix, namespace)
				
				
				//SIGNATURE
				Name idname = se.createName("identifier", "d", "urn:identifiers");
				Iterator itname = sh.getChildElements(idname);
				// check header element
				if (!itname.hasNext()) {
					throw new RuntimeException("Header not found.");
				}
				SOAPElement wselement = (SOAPElement) itname.next();

				// get header element value
				String soapWsName = wselement.getValue();
				
				System.out.println("Generating signature...");	
				//generate signature
				String keyPath;
				byte[] message_bytes = CryptoUtil.SOAPMessageToByteArray(msg);
				if(soapWsName.contains("Supplier")){
					keyPath = "../supplier-ws/src/main/resources/" + soapWsName + ".jks";
				}
				else {
					keyPath = "../mediator-ws/src/main/resources/" + soapWsName + ".jks";
				}
				PrivateKey key = CryptoUtil.getPrivateKeyFromKeyStoreFile(keyPath, "56vMW2kU".toCharArray(), soapWsName.toLowerCase(), "56vMW2kU".toCharArray());
				byte[] signature = CryptoUtil.makeDigitalSignature("SHA256withRSA",key, message_bytes);
				Name name = se.createName("signature", "d", "urn:signatures");
				SOAPHeaderElement element = sh.addHeaderElement(name);		
				
				element.addTextNode(CryptoUtil.byteToString(signature));
				System.out.println("Signature added to Header.");	
			} else {
				System.out.println("Reading signature from Header...");

				// get SOAP envelope header
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();

				// check header
				if (sh == null) {
					System.out.println("Header not found.");
					return true;
				}
				
				
				//get wsname
				Name idname = se.createName("identifier", "d", "urn:identifiers");
				Iterator itname = sh.getChildElements(idname);
				// check header element
				if (!itname.hasNext()) {
					//throw new RuntimeException("Header not found.");
					return false;
				}
				SOAPElement wselement = (SOAPElement) itname.next();

				// get header element value
				String soapWsName = wselement.getValue();
				
				
				// get first header element
				Name name = se.createName("signature", "d", "urn:signatures");
				Iterator it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					//throw new RuntimeException("Header not found.");
					return false;
				}
				SOAPElement element = (SOAPElement) it.next();

				// get header element value
				String valueString = element.getValue();
				byte[] givenSignature = CryptoUtil.stringToByte(valueString);
				CAClient ca = new CAClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca");;
				String ca_str = ca.getCertificate(soapWsName);
				
				Certificate cert = CryptoUtil.getX509CertificateFromPEMString(ca_str);
				
				//verify certificate
				System.out.println("Verifying signature...");
				System.out.println("Validating CA certificate...");
				PublicKey caPublicKey = CryptoUtil.getPublicKeyFromCertificate(CryptoUtil.getX509CertificateFromFile("../security/src/main/resources/ca.cer"));
				
				
				Boolean is_valid = CryptoUtil.verifySignedCertificate(cert, caPublicKey);
				if(is_valid){
					System.out.println("Certificate from CA is valid.");
				}
				else{
					throw new RuntimeException("Certificate from CA is NOT valid.");	
				}
				
				
				PublicKey pkey = CryptoUtil.getPublicKeyFromCertificate(cert);
				//remove signature header so the bytes are the same
				removeHeader(sh, "signature");
				byte[] actualMessage = CryptoUtil.SOAPMessageToByteArray(msg);
				Boolean is_verified = CryptoUtil.verifyDigitalSignature("SHA256withRSA", pkey, actualMessage, givenSignature);
				
				if(is_verified){
					System.out.println("Signature is valid.");
				}
				else{
					throw new RuntimeException("Signature failed to verify!");
				}
				// print received header
				//System.out.println("Header value is " + value);

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
		System.out.println("[SignHandler:END]");	
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
	
	/**
	 * Method to remove a header from a SOAP message.
	 * @param sh
	 * @param header
	 */
	public void removeHeader(SOAPHeader sh, String header) {
		
		NodeList nodes = sh.getChildNodes();
	
		for(int i=0; i < nodes.getLength(); i++) {
			
			Node n = (Node) nodes.item(i);				
			if(n.getNodeName().equals("d:" + header))
				n.getParentNode().removeChild(n);
			
		}	
		sh.normalize();
	}
	

}
