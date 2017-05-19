package org.komparator.mediator.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadQuantity;
import org.komparator.supplier.ws.BadQuantity_Exception;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.InsufficientQuantity;
import org.komparator.supplier.ws.InsufficientQuantity_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClientException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

@HandlerChain(file = "/mediator-ws_handler-chain.xml")
@WebService(
		endpointInterface = "org.komparator.mediator.ws.MediatorPortType", 
		wsdlLocation = "mediator.2_0.wsdl", 
		name = "MediatorWebService", 
		portName = "MediatorPort", 
		targetNamespace = "http://ws.mediator.komparator.org/", 
		serviceName = "MediatorService"
		)
public class MediatorPortImpl implements MediatorPortType {

	// end point manager
	private MediatorEndpointManager endpointManager;
	private static long idCounter = 0;
	private Map<String,CartView> cartMap = new ConcurrentHashMap<String,CartView>();
	private List<ShoppingResultView> shopRecords = new CopyOnWriteArrayList<ShoppingResultView>();
	private Map<Long,ShoppingResultView> shopIdentifierMap= new ConcurrentHashMap<Long,ShoppingResultView>();
	private List<Long> usedIdentifiers = new ArrayList<>();
	
	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}
	
	@Override
	public void clear() {
		//clear suppliers
		UDDINaming uddi = endpointManager.getUddiNaming();
		try {
		
			//clear local variables
			idCounter = 0;
			cartMap.clear();
			shopRecords.clear();
			//new: clear secondary
			if(LifeProof.iAmPrimary){
				Collection<UDDIRecord> supplierRecords = uddi.listRecords("T35_Supplier%");
				for(UDDIRecord record: supplierRecords){
					SupplierClient clnt = new SupplierClient(record.getUrl());
					clnt.clear();
				}
				
				if(LifeProof.primaryIsAlive){
					LifeProof.mc.clear();
				}
			}
		} catch (UDDINamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SupplierClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}

	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		List<ItemView> results = new ArrayList<ItemView>();
		UDDINaming uddi = endpointManager.getUddiNaming();
		try {
			Collection<UDDIRecord> supplierRecords = uddi.listRecords("T35_Supplier%");
			
			for(UDDIRecord record: supplierRecords){
				try {
				SupplierClient clnt = new SupplierClient(record.getUrl());
				ProductView currentProd = clnt.getProduct(productId);
				if(currentProd != null){
					ItemView currentItem = newItemView(currentProd,record.getOrgName());
					results.add(currentItem);
				}
			}  catch (SupplierClientException e) {
				continue;
			}
			}
			
			Collections.sort(results, (item1, item2) -> item1.getPrice() - item2.getPrice());
			
		} catch (UDDINamingException e) {
		} catch (BadProductId_Exception e) {
			throwInvalidItemId("Item ID is invalid!");
		}
		return results;
	}

	@Override
	public List<CartView> listCarts() {
		List<CartView> carts = new ArrayList<CartView>(cartMap.values());
		return carts;
	}

	@Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
		UDDINaming uddi = endpointManager.getUddiNaming();
		List<ItemView> results = new ArrayList<ItemView>();		
		try {
			Collection<UDDIRecord> supplierRecords = uddi.listRecords("T35_Supplier%");

			for(UDDIRecord record: supplierRecords){
				SupplierClient clnt = new SupplierClient(record.getUrl());
				List<ProductView> currentList = clnt.searchProducts(descText);
				for(ProductView currentProd: currentList){
					if(currentProd != null){
						ItemView currentItem = newItemView(currentProd,record.getOrgName());
						results.add(currentItem);
					}
				}
			}
			
			orderByProdIdThenPrice(results);
		} catch (UDDINamingException e) {
		} catch (SupplierClientException e) {
		} catch (BadText_Exception e) {
			throwInvalidText("Text is invalid!");
		}
		return results;		
	}

	@Override
	public ShoppingResultView buyCart2(String cartId, String creditCardNr, Long identifier)
			throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {	
		
		if(usedIdentifiers.contains(identifier)){
			//resend same output as last time
			return shopIdentifierMap.get(identifier);
		}
		else {
			usedIdentifiers.add(identifier);
			
		}
		if (cartId == null)
			throwInvalidCartId("cart identifier cannot be null!");
		cartId = cartId.trim();
		if (cartId.length() == 0)
			throwInvalidCartId("cart identifier cannot be empty or whitespace!");
		
		if (cartMap.get(cartId) == null){
			throwInvalidCartId("This cart does not exist!");
		}
		
		if(cartMap.get(cartId).getItems().size() == 0){
			throwEmptyCart("Cart is empty!");
		}
		
		
		UDDINaming uddi = endpointManager.getUddiNaming();
		ShoppingResultView shopResult = new ShoppingResultView();
		try {
			String ccURL = uddi.lookup("CreditCard");
			CreditCardClient client = new CreditCardClient(ccURL);
			if(!client.validateNumber(creditCardNr)){
				throwInvalidCreditCard("This credit card number is not valid!");
			}

					
		CartView cart = cartMap.get(cartId);
		synchronized (cart){
		int totalItems = cart.getItems().size();
		shopResult.setResult(Result.EMPTY);
		int purchasedPrice = 0;

		List<CartItemView> items = new ArrayList<CartItemView>();
		items.addAll(cart.getItems());
		for(CartItemView item: items){
			try {
			String supplierURL = uddi.lookup(item.getItem().getItemId().getSupplierId());
			
				SupplierClient currentClient = new SupplierClient(supplierURL);
				currentClient.buyProduct(item.getItem().getItemId().getProductId(), item.getQuantity());
				
				//if it ever gets here then a product has been bought
				shopResult.getPurchasedItems().add(item);
				cart.getItems().remove(item);
				purchasedPrice += item.getItem().getPrice() * item.getQuantity();
				
			} catch (BadProductId_Exception | BadQuantity_Exception | 
					InsufficientQuantity_Exception | SupplierClientException | UDDINamingException e) {
				shopResult.getDroppedItems().add(item);
				continue;
			}
			
		}
		
		int totalBought = shopResult.getPurchasedItems().size();
		if(totalBought > 0 && totalBought < totalItems){
			shopResult.setResult(Result.PARTIAL);
		}
		else if(totalBought == totalItems){ shopResult.setResult(Result.COMPLETE);}
		
		shopResult.setId(createID());
		shopResult.setTotalPrice(purchasedPrice);
		shopRecords.add(shopResult);
		shopIdentifierMap.put(identifier,shopResult);
		//new: update secondary mediator
		LifeProof.updateShoppingHistory(shopResult);
		LifeProof.updateCart(cartId, cart);
		}

		
		
		
		} catch (CreditCardClientException e) {
		} catch (UDDINamingException e){
		}
		/*
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}*/
		return shopResult;
	}

	@Override
	public void addToCart2(String cartId, ItemIdView itemId, int itemQty, long identifier) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		
		if(usedIdentifiers.contains(identifier)){
			//reject message
			throw new RuntimeException();
			
		}

		else {
			usedIdentifiers.add(identifier);
		}
		
		if (cartId == null)
			throwInvalidCartId("cart identifier cannot be null!");
		cartId = cartId.trim();
		if (cartId.length() == 0)
			throwInvalidCartId("cart identifier cannot be empty or whitespace!");		
		
		if(itemQty <= 0){
			throwInvalidQuantity("Invalid quantity!");
		}
		
		if (itemId == null || itemId.getProductId() == null || itemId.getSupplierId() == null)
			throwInvalidItemId("cart identifier cannot be null!");
		itemId.setProductId(itemId.getProductId().trim());
		itemId.setSupplierId(itemId.getSupplierId().trim());
		if (itemId.getProductId().length() == 0 || itemId.getSupplierId().length() == 0)
			throwInvalidItemId("cart identifier cannot be empty or whitespace!");
		
		int totalQuantity = 0;
		CartView cart = cartMap.get(cartId);
		
		Boolean alreadyExists = false;
		try {
			SupplierClient suppl = new SupplierClient(endpointManager.getUddiNaming().lookup(itemId.getSupplierId()));
			ProductView current_prod = suppl.getProduct(itemId.getProductId());
			if(current_prod == null){
				throwInvalidItemId("This item does not exist on this supplier!");
			}

		if(cart == null){
			cart = new CartView();
			cart.setCartId(cartId);
			cartMap.put(cartId, cart); 
		}
		synchronized (cart) {
		//List<CartItemView> items = cart.getItems();
		for(CartItemView item: cart.getItems()){
			if(item.getItem().getItemId().getProductId().equals(itemId.getProductId()) && 
					item.getItem().getItemId().getSupplierId().equals(itemId.getSupplierId())){	
				alreadyExists = true;
				totalQuantity = item.getQuantity() + itemQty;
				
				try {
					if(totalQuantity > suppl.getProduct(itemId.getProductId()).getQuantity()){
						throwNotEnoughItems("Not enough quantity in this supplier!");
					}
				} catch (BadProductId_Exception e) {
					throwInvalidItemId("itemId is not valid!");
				}
				item.setQuantity(totalQuantity);
			}
		}
		
		if(alreadyExists == false){
			ProductView product = newProductView(itemId);
			ItemView item = newItemView(product,itemId.getSupplierId());
			CartItemView cartitem = new CartItemView();
			cartitem.setItem(item);
			try {
				if(itemQty > suppl.getProduct(itemId.getProductId()).getQuantity()){
					throwNotEnoughItems("Not enough quantity in this supplier!");
				}
			} catch (BadProductId_Exception e) {
				throwInvalidItemId("ItemId is invalid!");
			}			
			cartitem.setQuantity(itemQty);
			cart.getItems().add(cartitem);
		}
		}
		
		LifeProof.updateCart(cartId, cart);
		
		} catch (SupplierClientException e) {
			throwInvalidItemId("SupplierId is invalid!");
		} catch (UDDINamingException e) {
			throwInvalidItemId("SupplierId is invalid!");
		} catch (BadProductId_Exception e1) {
			throwInvalidItemId("ItemId is invalid!");
		}
		
	}

	@Override
	public String ping(String arg0) {
		//Fetch all suppliers from UDDI
		UDDINaming uddi = endpointManager.getUddiNaming();
		try {
			Collection<UDDIRecord> supplierRecords = uddi.listRecords("T35_Supplier%");
			String result = "";
			for(UDDIRecord record: supplierRecords){
				SupplierClient clnt = new SupplierClient(record.getUrl());
				String temp_str = clnt.ping("mediator");
								
				result = result + "Input from " + record.getOrgName() + ": " + temp_str + "\n";
				
			}
		
		return result;	
			
		} catch (UDDINamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SupplierClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ShoppingResultView> shopHistory() {
		ShoppingResultView result = new ShoppingResultView();
		result.setId("history");
		Collections.sort(shopRecords, (item1, item2) -> Integer.compare(Integer.parseInt(item2.getId()), 
				Integer.parseInt(item1.getId())));
		return shopRecords;
	}

	// Main operations -------------------------------------------------------

    // TODO
	
    
	// Auxiliary operations --------------------------------------------------	
	
	private static void orderByProdIdThenPrice(List<ItemView> items) {

	    Collections.sort(items, new Comparator<ItemView>() {

	        public int compare(ItemView o1, ItemView o2) {

	            String prodId1 = o1.getItemId().getProductId();
	            String prodId2 = o2.getItemId().getProductId();
	            int sComp = prodId1.compareTo(prodId2);

	            if (sComp != 0) {
	               return sComp;
	            } else {
	               Integer price1 = o1.getPrice();
	               Integer price2 = o2.getPrice();
	               return price1.compareTo(price2);
	            }
	    }});
	}

	private static synchronized String createID(){
		return String.valueOf(idCounter++);
	}
	
	
	// View helpers -----------------------------------------------------
	
	private ItemView newItemView(ProductView product, String supplier) {
		ItemIdView idview = new ItemIdView();
		idview.setProductId(product.getId());
		idview.setSupplierId(supplier);
		
		ItemView view = new ItemView();		
		view.setDesc(product.getDesc());
		view.setItemId(idview);
		view.setPrice(product.getPrice());
		return view;
	}
	
	private ProductView newProductView(ItemIdView item){
		UDDINaming uddi = endpointManager.getUddiNaming();		
		UDDIRecord record;
		try {
			record = uddi.lookupRecord(item.getSupplierId());		
			SupplierClient clnt = new SupplierClient(record.getUrl());
			ProductView result = clnt.getProduct(item.getProductId());
			return result;
		} catch (UDDINamingException e) {;
		} catch (SupplierClientException e) {
		} catch (BadProductId_Exception e) {
		}

		
		return null;
	}
	

    
	// Exception helpers -----------------------------------------------------
	/** Helper method to throw invalidcreditcard */
	private void throwInvalidCreditCard(final String message) throws InvalidCreditCard_Exception {
		InvalidCreditCard faultInfo = new InvalidCreditCard();
		faultInfo.message = message;
		throw new InvalidCreditCard_Exception(message, faultInfo);
	}
	
	
	private void throwInvalidItemId(final String message) throws InvalidItemId_Exception {
		InvalidItemId faultInfo = new InvalidItemId();
		faultInfo.message = message;
		throw new InvalidItemId_Exception(message, faultInfo);
	}
	
	/** Helper method to throw new InsufficientQuantity exception */
	private void throwInvalidQuantity(final String message) throws InvalidQuantity_Exception {
		InvalidQuantity faultInfo = new InvalidQuantity();
		faultInfo.setMessage(message);
		throw new InvalidQuantity_Exception(message, faultInfo);
	}
	
	private void throwNotEnoughItems(final String message) throws NotEnoughItems_Exception {
		NotEnoughItems faultInfo = new NotEnoughItems();
		faultInfo.setMessage(message);
		throw new NotEnoughItems_Exception(message, faultInfo);
	}	
	private void throwInvalidCartId(final String message) throws InvalidCartId_Exception {
		InvalidCartId faultInfo = new InvalidCartId();
		faultInfo.message = message;
		throw new InvalidCartId_Exception(message, faultInfo);
	}
	private void throwEmptyCart(final String message) throws EmptyCart_Exception {
		EmptyCart faultInfo = new EmptyCart();
		faultInfo.message = message;
		throw new EmptyCart_Exception(message, faultInfo);
	}
	private void throwInvalidText(final String message) throws InvalidText_Exception {
		InvalidText faultInfo = new InvalidText();
		faultInfo.message = message;
		throw new InvalidText_Exception(message, faultInfo);
	}	

	/** staying alive */
	@Override
	public void imAlive(){
		if(!LifeProof.iAmPrimary){
			LifeProof.primaryIsAlive = true;
			LifeProof.lastPing = System.currentTimeMillis();
			System.out.println("Primary Mediator is alive.");
		}
	}
	
	@Override
	public void updateShoppingHistory(ShoppingResultView object){
		if(!LifeProof.iAmPrimary){
			shopRecords.add(object);
			idCounter++;
			System.out.println("New shopping history received.");
		}
	}
	
	@Override
	public void updateCart(String cartId, CartView cart){
		if(!LifeProof.iAmPrimary){
			cartMap.put(cartId, cart);
			System.out.println("New cart received.");
		}
	}

}
