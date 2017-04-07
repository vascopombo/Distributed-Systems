package org.komparator.mediator.ws.it;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.mediator.ws.CartView;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.InvalidText_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.ItemView;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
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
public class AddToCartIT extends BaseIT {

	
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
	
    @Test(expected=NotEnoughItems_Exception.class)
    public void cartGoesOverLimitOnFirstAdditionTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception{
    	
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");			
    	mediatorClient.addToCart("johnny", idview, 21);
    }   
    
    @Test(expected=NotEnoughItems_Exception.class)
    public void cartGoesOverLimitOnSecondAdditionTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception{
    	
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");			
    	mediatorClient.addToCart("johnny", idview, 5);
    	mediatorClient.addToCart("johnny", idview, 20);
    }  
   
    
    
    
	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartProductNullTest() throws InvalidText_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId(null);
		idview.setSupplierId("T35_Supplier2");		
		mediatorClient.addToCart("john",idview,10);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartProductEmptyTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("");
		idview.setSupplierId("T35_Supplier2");		
		mediatorClient.addToCart("john",idview,10);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartProductWhitespaceTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId(" ");
		idview.setSupplierId("T35_Supplier2");		
		mediatorClient.addToCart("john",idview,10);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartProductTabTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("\t");
		idview.setSupplierId("T35_Supplier2");		
		mediatorClient.addToCart("john",idview,10);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartProductNewlineTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("\n");
		idview.setSupplierId("T35_Supplier2");		
		mediatorClient.addToCart("john",idview,10);
	}
	
    
	
	
	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartSupplierNullTest() throws InvalidText_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("a");
		idview.setSupplierId(null);		
		mediatorClient.addToCart("john",idview,10);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartSupplierEmptyTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("a");
		idview.setSupplierId("");		
		mediatorClient.addToCart("john",idview,10);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartSupplierWhitespaceTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("a");
		idview.setSupplierId(" ");		
		mediatorClient.addToCart("john",idview,10);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartSupplierTabTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("a");
		idview.setSupplierId("\t");		
		mediatorClient.addToCart("john",idview,10);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void addToCartSupplierNewlineTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("a");
		idview.setSupplierId("\n");		
		mediatorClient.addToCart("john",idview,10);
	}
	
	
	
	
	@Test(expected = InvalidQuantity_Exception.class)
	public void addToCartQuantityBelowZeroTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
		mediatorClient.addToCart("john",idview,-1);
	}
	@Test(expected = InvalidQuantity_Exception.class)
	public void addToCartQuantityZeroTest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
		mediatorClient.addToCart("john",idview,0);
	}	
	
    
    
	
    @Test
    public void cartNotYetCreatedTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception{
    	
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
			
    	mediatorClient.addToCart("johnny", idview, 10);
    	List<CartView> carts = mediatorClient.listCarts();
    	assertEquals(carts.get(0).getCartId(),"johnny");
    	assertEquals(carts.get(0).getItems().get(0).getItem().getItemId().getProductId(),"vg");
    	assertEquals(carts.get(0).getItems().get(0).getQuantity(),10);
    }
    @Test
    public void cartAlreadyCreatedTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception{
    	
    	ItemIdView idview = new ItemIdView();
		idview.setProductId("vg");
		idview.setSupplierId("T35_Supplier2");
			
    	mediatorClient.addToCart("johnny", idview, 5);
    	mediatorClient.addToCart("johnny", idview, 10);
    	List<CartView> carts = mediatorClient.listCarts();
    	assertEquals(carts.get(0).getCartId(),"johnny");
    	assertEquals(carts.get(0).getItems().get(0).getItem().getItemId().getProductId(),"vg");
    	assertEquals(carts.get(0).getItems().get(0).getQuantity(),15);
    }
    
    @Test
    public void twoDifferentCartsTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception{
       	ItemIdView idview = new ItemIdView();
    	idview.setProductId("vg");
    	idview.setSupplierId("T35_Supplier2");
    			
        mediatorClient.addToCart("johnny", idview, 5);
        mediatorClient.addToCart("susan", idview, 10);
        List<CartView> carts = mediatorClient.listCarts();
    	assertEquals(carts.size(),2);        
    	assertNotEquals(carts.get(0),carts.get(1));
    }

    @Test
    public void twoItemsInOneCartTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception{
       
    	ItemIdView idview = new ItemIdView();
    	idview.setProductId("vg");
    	idview.setSupplierId("T35_Supplier2");
    	
       	ItemIdView idview2 = new ItemIdView();
    	idview2.setProductId("a");
    	idview2.setSupplierId("T35_Supplier1");    	
    			
        mediatorClient.addToCart("johnny", idview, 5);
        mediatorClient.addToCart("johnny", idview2, 5);
        List<CartView> carts = mediatorClient.listCarts();
    	assertEquals(carts.size(),1);        
    	assertEquals(carts.get(0).getItems().size(),2);
    }    
}
