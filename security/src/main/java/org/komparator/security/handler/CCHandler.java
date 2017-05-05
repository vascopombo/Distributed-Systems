package org.komparator.security.handler;

import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
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
import org.komparator.security.CryptoUtil;
import org.w3c.dom.Node;

import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;

public class CCHandler implements SOAPHandler<SOAPMessageContext> {
	public static final String CONTEXT_PROPERTY = "my.property";
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
				SOAPBody so = se.getBody();
				Node ns1 = so.getFirstChild();
				if(ns1.getNodeName().equals("ns2:buyCart")){
					System.out.println("[CCHandler: BEGIN]");
					System.out.println("Credit card number is being sent.");
					System.out.println("Getting certificate from CA...");
					CAClient ca = new CAClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca");
					String ca_str = ca.getCertificate("T35_Mediator");

					
					Certificate cert = CryptoUtil.getX509CertificateFromPEMString(ca_str);
					
					//verify certificate
					System.out.println("Verifying Signed Certificate...");	
					PublicKey caPublicKey = CryptoUtil.getPublicKeyFromCertificate(CryptoUtil.getX509CertificateFromFile("../security/src/main/resources/ca.cer"));
					
					
					Boolean is_valid = CryptoUtil.verifySignedCertificate(cert, caPublicKey);
					if(is_valid){
						System.out.println("Certificate from CA is valid.");
					}
					else{
						throw new RuntimeException("Certificate from CA is NOT valid.");	
					}
					
					System.out.println("Encrypting credit card number...");
					PublicKey key = CryptoUtil.getPublicKeyFromCertificate(cert);
					
					String cc = ns1.getLastChild().getTextContent();
					byte[] temp_cc = CryptoUtil.stringToByte(cc);
					byte[] encrypted_cc = CryptoUtil.asymCipher(temp_cc, key);
					ns1.getLastChild().setTextContent(CryptoUtil.byteToString(encrypted_cc));
					System.out.println("Credit card number is now encrypted!");
					System.out.println("[CCHandler: END]");
				}
				
				

			} else {
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPBody so = se.getBody();
				Node ns1 = so.getFirstChild();				
				if(ns1.getNodeName().equals("ns2:buyCart")){
					System.out.println("[CCHandler: BEGIN]");
					System.out.println("credit card number is being received.");
					String cc = ns1.getLastChild().getTextContent();
					PrivateKey pk = CryptoUtil.getPrivateKeyFromKeyStoreFile("../mediator-ws/src/main/resources/T35_Mediator.jks", "56vMW2kU".toCharArray(), "t35_mediator", "56vMW2kU".toCharArray());
					byte[] cc_data = CryptoUtil.stringToByte(cc);
					System.out.println("Decrypting credit card number...");
					String decrypted_cc = CryptoUtil.byteToString((CryptoUtil.asymDecipher(cc_data, pk)));
					ns1.getLastChild().setTextContent(decrypted_cc);
					System.out.println("credit card number is now decrypted!");
					System.out.println("[CCHandler: END]");
				}
				
				//smc.setScope(CONTEXT_PROPERTY, Scope.APPLICATION);
				
				
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
