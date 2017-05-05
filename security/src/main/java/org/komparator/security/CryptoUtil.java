package org.komparator.security;

import java.io.*;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import java.security.*;
import javax.crypto.*;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;

import java.util.*;

public class CryptoUtil {
	/** print some error messages to standard error. */
	public static boolean outputFlag = true;
	
	
    public static byte[] SOAPMessageToByteArray(SOAPMessage msg) throws Exception {
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	msg.writeTo(stream);
		return stream.toByteArray();
    }
    
	public static boolean verifySignedCertificate(Certificate certificate, PublicKey caPublicKey) {
		try {
			certificate.verify(caPublicKey);
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			if (outputFlag) {
				System.err.println("Caught exception while verifying certificate with CA public key : " + e);
				System.err.println("Returning false.");
			}
			return false;
		}
		return true;
	}
	
	public static byte[] asymCipher(byte[] data, 	PublicKey key){
		
		try {
			Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			c.init(Cipher.ENCRYPT_MODE, key);
			byte[] cypherBytes = c.doFinal(data);
			return cypherBytes;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
		}
		
		return null;
	}
	
	public static byte[] asymDecipher(byte[] data, PrivateKey key){
		
		try {
			Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			c.init(Cipher.DECRYPT_MODE, key);
			byte[] cypherBytes = c.doFinal(data);
			return cypherBytes;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
		}
		
		return null;
	}
	
	
	public static byte[] stringToByte(String s){
		return parseBase64Binary(s);
	}
	
	public static String byteToString(byte[] b){
		return printBase64Binary(b);
	}
	
	
	// resource stream helpers ------------------------------------------------

	/** Method used to access resource. */
	private static InputStream getResourceAsStream(String resourcePath) {
		// uses current thread's class loader to also work correctly inside
		// application servers
		// reference: http://stackoverflow.com/a/676273/129497
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
		return is;
	}

	/** Do the best effort to close the stream, but ignore exceptions. */
	private static void closeStream(InputStream in) {
		try {
			if (in != null)
				in.close();
		} catch (IOException e) {
			// ignore
		}
	}
	
	
	/**
	 * Reads a KeyStore from a stream.
	 * 
	 * @param keyStoreInputStream
	 *            key store stream
	 * @param keyStorePassword
	 *            key store password
	 * @return The read KeyStore
	 * @throws FileNotFoundException
	 * @throws KeyStoreException
	 */
	private static KeyStore readKeystoreFromStream(InputStream keyStoreInputStream, char[] keyStorePassword)
			throws KeyStoreException {
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		try {
			keystore.load(keyStoreInputStream, keyStorePassword);
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new KeyStoreException("Could not load key store", e);
		} finally {
			closeStream(keyStoreInputStream);
		}
		return keystore;
	}

	/**
	 * Reads a KeyStore from a resource.
	 * 
	 * @param keyStoreResourcePath
	 *            key store resource path
	 * @param keyStorePassword
	 *            key store password
	 * @return The read KeyStore
	 * @throws FileNotFoundException
	 * @throws KeyStoreException
	 */
	public static KeyStore readKeystoreFromResource(String keyStoreResourcePath, char[] keyStorePassword)
			throws KeyStoreException {
		InputStream is = getResourceAsStream(keyStoreResourcePath);
		return readKeystoreFromStream(is, keyStorePassword);
	}
	
	/**
	 * Reads a certificate from a resource (included in the application
	 * package).
	 * 
	 * @param certificateResourcePath
	 * @return the Certificate
	 * @throws IOException
	 * @throws CertificateException
	 */
	public static Certificate getX509CertificateFromResource(String certificateResourcePath)
			throws IOException, CertificateException {
		InputStream is = getResourceAsStream(certificateResourcePath);
		return getX509CertificateFromStream(is);
	}
	
	public static Certificate getX509CertificateFromStream(InputStream in) throws CertificateException {
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			Certificate cert = certFactory.generateCertificate(in);
			return cert;
		} finally {
			closeStream(in);
		}
	}
	
	public static PublicKey getPublicKeyFromCertificate(Certificate certificate) {
		return certificate.getPublicKey();
	}
	
	
	/**
	 * Reads a PrivateKey from a key-store resource.
	 * 
	 * @param keyStoreResourcePath
	 *            key store resource path
	 * @param keyStorePassword
	 *            key store password
	 * @param keyAlias
	 *            name of the key to retrieve
	 * @param keyPassword
	 *            key password
	 * @return The PrivateKey
	 * @throws FileNotFoundException
	 * @throws KeyStoreException
	 */
	public static PrivateKey getPrivateKeyFromKeyStoreResource(String keyStoreResourcePath, char[] keyStorePassword,
			String keyAlias, char[] keyPassword)
			throws FileNotFoundException, KeyStoreException, UnrecoverableKeyException {
		KeyStore keystore = readKeystoreFromResource(keyStoreResourcePath, keyStorePassword);
		return getPrivateKeyFromKeyStore(keyAlias, keyPassword, keystore);
	}
	public static PrivateKey getPrivateKeyFromKeyStore(String keyAlias, char[] keyPassword, KeyStore keystore)
			throws KeyStoreException, UnrecoverableKeyException {
		PrivateKey key;
		try {
			key = (PrivateKey) keystore.getKey(keyAlias, keyPassword);
		} catch (NoSuchAlgorithmException e) {
			throw new KeyStoreException(e);
		}
		return key;
	}
	
	// digital signature ------------------------------------------------------

	/**
	 * Signs the input bytes with the private key and returns the bytes. If
	 * anything goes wrong, null is returned (swallows exceptions).
	 * 
	 * @param signatureMethod
	 *            e.g. "SHA1WithRSA"
	 * @param privateKey
	 * @param bytesToSign
	 * @return bytes of resulting signature
	 */
	public static byte[] makeDigitalSignature(final String signatureMethod, final PrivateKey privateKey,
			final byte[] bytesToSign) {
		try {
			Signature sig = Signature.getInstance(signatureMethod);
			sig.initSign(privateKey);
			sig.update(bytesToSign);
			byte[] signatureResult = sig.sign();
			return signatureResult;
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			if (outputFlag) {
				System.err.println("Caught exception while making signature: " + e);
				System.err.println("Returning null.");
			}
			return null;
		}
	}

	/**
	 * Verify signature of bytes with the public key. If anything goes wrong,
	 * returns false (swallows exceptions).
	 * 
	 * @param signatureMethod
	 *            e.g. "SHA1WithRSA"
	 * @param publicKey
	 * @param bytesToVerify
	 * @param signature
	 * @return
	 */
	public static boolean verifyDigitalSignature(final String signatureMethod, PublicKey publicKey,
			byte[] bytesToVerify, byte[] signature) {
		try {
			Signature sig = Signature.getInstance(signatureMethod);
			sig.initVerify(publicKey);
			sig.update(bytesToVerify);
			return sig.verify(signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			if (outputFlag) {
				System.err.println("Caught exception while verifying signature " + e);
				System.err.println("Returning false.");
			}
			return false;
		}
	}

	/**
	 * Verify signature of bytes with the public key contained in the
	 * certificate. If anything goes wrong, returns false (swallows exceptions).
	 * 
	 * @param signatureMethod
	 * @param publicKeycertificate
	 * @param bytesToVerify
	 * @param signature
	 * @return
	 */
	public static boolean verifyDigitalSignature(final String signatureMethod, Certificate publicKeyCertificate,
			byte[] bytesToVerify, byte[] signature) {
		return verifyDigitalSignature(signatureMethod, publicKeyCertificate.getPublicKey(), bytesToVerify, signature);
	}
	
	/**
	 * Converts a byte array to a Certificate object. Returns null if the bytes
	 * do not correspond to a certificate.
	 * 
	 * @param bytes
	 *            the byte array to convert
	 * @return the certificate
	 * @throws CertificateException
	 */
	public static Certificate getX509CertificateFromBytes(byte[] bytes) throws CertificateException {
		InputStream in = new ByteArrayInputStream(bytes);
		return getX509CertificateFromStream(in);
	}
	
	/**
	 * Reads a certificate from a file.
	 * 
	 * @param certificateFile
	 * @return the Certificate
	 * @throws FileNotFoundException
	 * @throws CertificateException
	 */
	public static Certificate getX509CertificateFromFile(File certificateFile)
			throws FileNotFoundException, CertificateException {
		FileInputStream fis = new FileInputStream(certificateFile);
		return getX509CertificateFromStream(fis);
	}

	/**
	 * Reads a certificate from a file path.
	 * 
	 * @param certificateFilePath
	 * @return the Certificate
	 * @throws FileNotFoundException
	 * @throws CertificateException
	 */
	public static Certificate getX509CertificateFromFile(String certificateFilePath)
			throws FileNotFoundException, CertificateException {
		File certificateFile = new File(certificateFilePath);
		return getX509CertificateFromFile(certificateFile);
	}
	
	/**
	 * Reads a PrivateKey from a key-store file.
	 * 
	 * @param keyStoreFile
	 *            key store file
	 * @param keyStorePassword
	 *            key store password
	 * @param keyAlias
	 *            name of the key to retrieve
	 * @param keyPassword
	 *            key password
	 * @return The PrivateKey
	 * @throws FileNotFoundException
	 * @throws KeyStoreException
	 */
	public static PrivateKey getPrivateKeyFromKeyStoreFile(File keyStoreFile, char[] keyStorePassword, String keyAlias,
			char[] keyPassword) throws FileNotFoundException, KeyStoreException, UnrecoverableKeyException {
		KeyStore keystore = readKeystoreFromFile(keyStoreFile, keyStorePassword);
		return getPrivateKeyFromKeyStore(keyAlias, keyPassword, keystore);
	}

	/**
	 * Reads a PrivateKey from a key store in given file path.
	 * 
	 * @param keyStoreFilePath
	 *            path to key store file
	 * @param keyStorePassword
	 *            key store password
	 * @param keyAlias
	 *            name of the key to retrieve
	 * @param keyPassword
	 *            key password
	 * @return The PrivateKey
	 * @throws FileNotFoundException
	 * @throws KeyStoreException
	 */
	public static PrivateKey getPrivateKeyFromKeyStoreFile(String keyStoreFilePath, char[] keyStorePassword,
			String keyAlias, char[] keyPassword)
			throws FileNotFoundException, KeyStoreException, UnrecoverableKeyException {
		return getPrivateKeyFromKeyStoreFile(new File(keyStoreFilePath), keyStorePassword, keyAlias, keyPassword);
	}
	
	/**
	 * Reads a KeyStore from a file.
	 * 
	 * @param keyStoreFile
	 *            key store file
	 * @param keyStorePassword
	 *            key store password
	 * @return The read KeyStore
	 * @throws FileNotFoundException
	 * @throws KeyStoreException
	 */
	private static KeyStore readKeystoreFromFile(File keyStoreFile, char[] keyStorePassword)
			throws FileNotFoundException, KeyStoreException {
		FileInputStream fis = new FileInputStream(keyStoreFile);
		return readKeystoreFromStream(fis, keyStorePassword);
	}

	/**
	 * Reads a KeyStore from a file path.
	 * 
	 * @param keyStoreFilePath
	 *            path to key store file
	 * @param keyStorePassword
	 *            key store password
	 * @return The read KeyStore
	 * @throws FileNotFoundException
	 * @throws KeyStoreException
	 */
	public static KeyStore readKeystoreFromFile(String keyStoreFilePath, char[] keyStorePassword)
			throws FileNotFoundException, KeyStoreException {
		return readKeystoreFromFile(new File(keyStoreFilePath), keyStorePassword);
	}
	
	
	public static byte[] generateRandomNumber(){
		// SecureRandom uses entropy generated from boot time until now (/udev/random)
		SecureRandom random = new SecureRandom();
		byte[] values = new byte[128];
		// No need to seed it, it seeds by default when it's not called
		random.nextBytes(values);
		return values;
	}
	
	/**
	 * Returns a Certificate object given a string with a certificate in the PEM
	 * format.
	 * 
	 * @param certificateString
	 *            the String with the certificate in PEM format.
	 * @return the Certificate
	 * @throws CertificateException
	 */
	public static Certificate getX509CertificateFromPEMString(String certificateString) throws CertificateException {
		byte[] bytes = certificateString.getBytes(StandardCharsets.UTF_8);
		return getX509CertificateFromBytes(bytes);
	}
	
	
}
