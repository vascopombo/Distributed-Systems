package org.komparator.mediator.ws.cli;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.komparator.mediator.ws.CartItemView;
import org.komparator.mediator.ws.CartView;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
import org.komparator.mediator.ws.ShoppingResultView;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

public class MediatorClientApp {

	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	
    public static void main(String[] args) throws Exception {
        // Check arguments
        if (args.length == 0) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + MediatorClientApp.class.getName()
                    + " wsURL OR uddiURL wsName");
            return;
        }
        String uddiURL = null;
        String wsName = null;
        String wsURL = null;
        if (args.length == 1) {
            wsURL = args[0];
        } else if (args.length >= 2) {
            uddiURL = args[0];
            wsName = args[1];
        }

        // Create client
        MediatorClient client = null;

        if (wsURL != null) {
            System.out.printf("Creating client for server at %s%n", wsURL);
            client = new MediatorClient(wsURL);
        } else if (uddiURL != null) {
            System.out.printf("Creating client using UDDI at %s for server with name %s%n",
                uddiURL, wsName);
            client = new MediatorClient(uddiURL, wsName);
        }

        // the following remote invocations are just basic examples
        // the actual tests are made using JUnit

		System.out.println(ANSI_BLUE + "\nShowcase commencing." + ANSI_RESET);
		System.out.println(ANSI_YELLOW + "The following features will be shown:" + ANSI_RESET);
		System.out.println(ANSI_YELLOW +"#1:" + ANSI_RESET + " The mediator client applies at-most-once semantics.");
		System.out.println(ANSI_YELLOW +"#2:" + ANSI_RESET + " The mediator correctly handles duplicate requests.");
		System.out.println(ANSI_YELLOW +"#3:" + ANSI_RESET + " Shopping history and cart states are equal between mediators.");		
		System.out.println(ANSI_YELLOW +"#4:" + ANSI_RESET + " The secondary mediator takes over the primary role when needed.");		
        waitForInput();
        

		UDDINaming uddi = new UDDINaming(uddiURL);
		//we assume two suppliers with these names for clarity
		SupplierClient supp1 = new SupplierClient(uddi.lookup("T35_Supplier1"));
		SupplierClient supp2 = new SupplierClient(uddi.lookup("T35_Supplier2"));        
		populateSuppliers(supp1,supp2);
        addShowcase(client);
        
        System.out.println("\n\nSuppliers and cart have been populated.");
        System.out.println("buyCart operation will now be performed.");
        System.out.println("The buyCart method in the mediator sleeps for a few seconds.");
        System.out.println(ANSI_YELLOW + "This will showcase client timeout/retry and handling of multiple equal"
        		+ " requests by the mediator." + ANSI_RESET);
        waitForInput();
        
        ShoppingResultView result = client.buyCart("johnny", "4024007102923926");
        System.out.println(ANSI_YELLOW + "\n\nThese are the contents of the result from the previous buyCart:" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "SRV ID: " + ANSI_RESET + result.getId());
        System.out.println(ANSI_YELLOW + "SRV Total Price: "+ ANSI_RESET + result.getTotalPrice());
        System.out.println(ANSI_YELLOW + "Result: "+ ANSI_RESET +  result.getResult());
        System.out.println(ANSI_YELLOW + "Purchased items:");
        for(CartItemView item: result.getPurchasedItems()){
        	System.out.println(ANSI_YELLOW + "ProductId: "+ ANSI_RESET + item.getItem().getItemId().getProductId() 
        			+ ANSI_YELLOW + " Quantity: "+ ANSI_RESET + item.getQuantity());
        }
        System.out.println("I will now repopulate the suppliers and the carts.");
        waitForInput();
        populateSuppliers(supp1,supp2);
        addShowcase(client);

        List<CartView> carts = client.listCarts();
        List<ShoppingResultView> hist = client.shopHistory();
        ShoppingResultView shop = hist.get(0);
        System.out.println(ANSI_YELLOW + "\n\nThese are the current contents in the carts and shopping history of the primary "
        		+ "mediator:" + ANSI_RESET);        
        System.out.println(ANSI_BLUE + "Cart 0:" + ANSI_RESET);
        for(CartItemView item: carts.get(0).getItems()){
        	System.out.println(ANSI_YELLOW + "ProductId: "+ ANSI_RESET + item.getItem().getItemId().getProductId() 
        			+ ANSI_YELLOW + " Quantity: "+ ANSI_RESET + item.getQuantity());		}
        
        System.out.println(ANSI_BLUE + "shopHistory 0:" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "SRV ID: " + ANSI_RESET + shop.getId());
        System.out.println(ANSI_YELLOW + "SRV Total Price: "+ ANSI_RESET + shop.getTotalPrice());
        System.out.println(ANSI_YELLOW + "Result: "+ ANSI_RESET +  shop.getResult());
        System.out.println(ANSI_YELLOW + "Purchased items:");
        for(CartItemView item: shop.getPurchasedItems()){
        	System.out.println(ANSI_YELLOW + "ProductId: "+ ANSI_RESET + item.getItem().getItemId().getProductId() 
        			+ ANSI_YELLOW + " Quantity: "+ ANSI_RESET + item.getQuantity());
        }
        System.out.println("\nI will now show that the two mediators share the same state.");
        System.out.println("This will also showcase that the secondary mediator replaces the primary one if "
        		+ "it does not detect it.");
        System.out.println("\u001B[32m" + "Press Ctrl-C on your primary mediator." + "\u001B[0m");
        System.out.println("I will print the contents of the carts and the shopping history in the \"new\" "
        		+ "mediator afterwards. ");
        waitForInput();
        
        
        carts = client.listCarts();
        hist = client.shopHistory();
        shop = hist.get(0);
		System.out.println("\nThese are the contents of the cart and the shopping history on the \"new\" mediator:");
        System.out.println(ANSI_BLUE + "Cart 0:" + ANSI_RESET);
        for(CartItemView item: carts.get(0).getItems()){
        	System.out.println(ANSI_YELLOW + "ProductId: "+ ANSI_RESET + item.getItem().getItemId().getProductId() 
        			+ ANSI_YELLOW + " Quantity: "+ ANSI_RESET + item.getQuantity());		}
        
        System.out.println(ANSI_BLUE + "shopHistory 0:" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "SRV ID: " + ANSI_RESET + shop.getId());
        System.out.println(ANSI_YELLOW + "SRV Total Price: "+ ANSI_RESET + shop.getTotalPrice());
        System.out.println(ANSI_YELLOW + "Result: "+ ANSI_RESET +  shop.getResult());
        System.out.println(ANSI_YELLOW + "Purchased items:");
        for(CartItemView item: shop.getPurchasedItems()){
        	System.out.println(ANSI_YELLOW + "ProductId: "+ ANSI_RESET + item.getItem().getItemId().getProductId() 
        			+ ANSI_YELLOW + " Quantity: "+ ANSI_RESET + item.getQuantity());
        }      
        
        System.out.println(ANSI_BLUE + "Showcase complete." + ANSI_RESET);
        
        /*
        System.out.println("Invoke ping()...");
        String pingResult = client.ping("client");
        System.out.println(pingResult);
		*/
    }
    
    public static void populateSuppliers(SupplierClient supp1, SupplierClient supp2){
		supp1.clear();
		supp2.clear();

		// fill-in test products
		{
			ProductView product = new ProductView();
			product.setId("a");
			product.setDesc("alfa");
			product.setPrice(100);
			product.setQuantity(10);
			try {
				supp1.createProduct(product);
			} catch (BadProductId_Exception | BadProduct_Exception e) {
			}
		}
		{
			ProductView product = new ProductView();
			product.setId("vg");
			product.setDesc("vasco da gama");
			product.setPrice(10);
			product.setQuantity(20);
			try {
				supp2.createProduct(product);
			} catch (BadProductId_Exception | BadProduct_Exception e) {
			}
		}		
    }
    
    public static void addShowcase(MediatorClient client){
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
		ItemIdView idview2 = new ItemIdView();
		idview2.setProductId("a");
		idview2.setSupplierId("T35_Supplier1");		
		try {
			client.addToCart("johnny", idview, 10);
		} catch (InvalidCartId_Exception | InvalidItemId_Exception | InvalidQuantity_Exception
				| NotEnoughItems_Exception e) {
		}
		try {
			client.addToCart("johnny", idview2, 5);
		} catch (InvalidCartId_Exception | InvalidItemId_Exception | InvalidQuantity_Exception
				| NotEnoughItems_Exception e) {
		}
    }

    public static void waitForInput() {
		System.out.println(ANSI_GREEN +"Press enter to continue" + ANSI_RESET);
	try {
		System.in.read();
	} catch (IOException e) {}
}
}


