package org.komparator.mediator.ws;

import java.util.Date;
import java.util.TimerTask;

import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;

public class LifeProof extends TimerTask{
	public static boolean iAmPrimary = true;
	public static boolean primaryIsAlive = true;
	public static int DELAY = 5000;
	public static long lastPing = 0;
	public static long timeout = 5000;
	public static MediatorEndpointManager endpoint;
	public static MediatorClient mc;
	@Override
	public void run() {
		try {
			if(iAmPrimary && primaryIsAlive){
				mc = new MediatorClient("http://localhost:8072/mediator-ws/endpoint");
				mc.imAlive();
			}
			else{
				if(lastPing != 0 && primaryIsAlive){
				long timePassed =System.currentTimeMillis() - lastPing;
				System.out.println("Last ping was " + timePassed + " miliseconds ago.");
				if(timePassed > timeout){
					System.out.println("Primary Mediator seems to be dead.\nReplacing it now.");
					replaceMediator();
				}
			}
				
				
			}
		} catch (Exception e) {
		}
		
	}	
	
	public static void replaceMediator(){
		try {
			endpoint.publishToUDDI();
		} catch (Exception e) {}
		iAmPrimary = true;
		primaryIsAlive = false;
		System.out.println("Success. This is now the primary mediator.");
	}
	
	
	public static void updateShoppingHistory(ShoppingResultView object){
		if(iAmPrimary && primaryIsAlive){
			mc.updateShoppingHistory(object);
		}
	}
	
	public static void updateCart(String cartId, CartView cart){
		if(iAmPrimary && primaryIsAlive){
			mc.updateCart(cartId, cart);
		}
		
	}
	



}
