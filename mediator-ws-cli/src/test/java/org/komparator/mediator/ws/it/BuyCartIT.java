package org.komparator.mediator.ws.it;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.komparator.mediator.ws.EmptyCart_Exception;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidCreditCard_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
import org.komparator.mediator.ws.Result;
import org.komparator.mediator.ws.ShoppingResultView;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;


/**
 * Test suite
 */
public class BuyCartIT extends BaseIT {

	// initialization and clean-up for each test
	static SupplierClient supp1;
	static SupplierClient supp2;
	
	@Before
	public void setUp() {
		String uddiURL = testProps.getProperty("uddi.url");
		try {
			UDDINaming uddi = new UDDINaming(uddiURL);
			//we assume two suppliers with these names for clarity
			supp1 = new SupplierClient(uddi.lookup("T35_Supplier1"));
			supp2 = new SupplierClient(uddi.lookup("T35_Supplier2"));
			
			

			
			// clear remote service state before all tests
			supp1.clear();
			supp2.clear();

			// fill-in test products
			{
				ProductView product = new ProductView();
				product.setId("a");
				product.setDesc("alfa");
				product.setPrice(100);
				product.setQuantity(10);
				supp1.createProduct(product);
			}
			{
				ProductView product = new ProductView();
				product.setId("vg");
				product.setDesc("vasco da gama");
				product.setPrice(10);
				product.setQuantity(20);
				supp2.createProduct(product);
			}
			
			
		} catch (UDDINamingException | SupplierClientException | BadProductId_Exception | BadProduct_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@After
	public void tearDown() throws UDDINamingException {

		mediatorClient.clear();

	}

	
	// bad input tests

	@Test(expected = InvalidCartId_Exception.class)
	public void cartIdNullTest() throws BadProductId_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
		mediatorClient.addToCart(null, idview, 10);
	}

	@Test(expected = InvalidCartId_Exception.class)
	public void cartIdEmptyTest() throws BadProductId_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
		mediatorClient.addToCart("",idview, 10);
	}

	@Test(expected = InvalidCartId_Exception.class)
	public void cartIdWhitespaceTest() throws BadProductId_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
		mediatorClient.addToCart(" ",idview, 10);
	}

	@Test(expected = InvalidCartId_Exception.class)
	public void cartIdTabTest() throws BadProductId_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
		mediatorClient.addToCart("\t",idview, 10);
	}

	@Test(expected = InvalidCartId_Exception.class)
	public void cartIdNewlineTest() throws BadProductId_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
		mediatorClient.addToCart("\n", idview, 10);
	}
	@Test(expected = InvalidCreditCard_Exception.class)
	public void invalidCreditCardTest() throws BadProductId_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
		mediatorClient.addToCart("johnny", idview, 10);
		mediatorClient.buyCart("johnny", "!!!!!!");
	}
	@Test(expected = InvalidCartId_Exception.class)
	public void cartIdDoesNotExistTest() throws BadProductId_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
		mediatorClient.addToCart("johnny", idview, 10);
		mediatorClient.buyCart("johnny2", "4024007102923926");
	}
	
	@Test(expected = EmptyCart_Exception.class)
	public void cartIsEmptyTest() throws BadProductId_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
		mediatorClient.addToCart("johnny", idview, 10);
		mediatorClient.buyCart("johnny", "4024007102923926");
		mediatorClient.buyCart("johnny", "4024007102923926");
	}	

	@Test
	public void resultIsCOMPLETETest() throws BadProductId_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
		
		ItemIdView idview2 = new ItemIdView();
		idview2.setProductId("a");
		idview2.setSupplierId("T35_Supplier1");		
		mediatorClient.addToCart("johnny", idview, 10);
		mediatorClient.addToCart("johnny", idview2, 5);
		ShoppingResultView result = mediatorClient.buyCart("johnny", "4024007102923926");
		
		assertEquals(result.getDroppedItems().size(),0);
		assertEquals(result.getPurchasedItems().size(),2);
		assertEquals(result.getTotalPrice(),600);
		assertEquals(result.getResult(),Result.COMPLETE);
		
	}	

	@Test
	public void resultIsPARTIALTest() throws BadProductId_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
		
		ItemIdView idview2 = new ItemIdView();
		idview2.setProductId("a");
		idview2.setSupplierId("T35_Supplier1");		
		mediatorClient.addToCart("johnny", idview, 10);
		mediatorClient.addToCart("johnny", idview2, 10);
		
		//this purchase exists to lower stock
		mediatorClient.addToCart("impulsebuyer", idview2, 5);
		mediatorClient.buyCart("impulsebuyer", "4024007102923926");
		
		ShoppingResultView result = mediatorClient.buyCart("johnny", "4024007102923926");
		
		assertEquals(result.getDroppedItems().size(),1);
		assertEquals(result.getPurchasedItems().size(),1);
		assertEquals(result.getTotalPrice(),100);
		assertEquals(result.getResult(),Result.PARTIAL);
		
	}
	
	@Test
	public void resultIsEMPTYTest() throws BadProductId_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
		
		ItemIdView idview2 = new ItemIdView();
		idview2.setProductId("a");
		idview2.setSupplierId("T35_Supplier1");		
		mediatorClient.addToCart("johnny", idview, 15);
		mediatorClient.addToCart("johnny", idview2, 10);
		
		//this purchase exists to empty stock
		mediatorClient.addToCart("impulsebuyer", idview, 12);
		mediatorClient.addToCart("impulsebuyer", idview2, 10);
		mediatorClient.buyCart("impulsebuyer", "4024007102923926");
		
		ShoppingResultView result = mediatorClient.buyCart("johnny", "4024007102923926");
		
		assertEquals(result.getDroppedItems().size(),2);
		assertEquals(result.getPurchasedItems().size(),0);
		assertEquals(result.getTotalPrice(),0);
		assertEquals(result.getResult(),Result.EMPTY);
		
	}		
	
}
