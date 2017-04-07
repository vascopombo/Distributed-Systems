package org.komparator.mediator.ws.it;

import java.util.List;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.*;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.ItemView;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.BadQuantity_Exception;
import org.komparator.supplier.ws.InsufficientQuantity_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;


/**
 * Test suite
 */
public class GetItemsIT extends BaseIT {

	// initialization and clean-up for each test
	static SupplierClient supp1;
	static SupplierClient supp2;
	
	@BeforeClass
	public static void oneTimeSetUp() {
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
			// (since getItemsIT is read-only the initialization below
			// can be done once for all tests in this suite)
			{
				ProductView product = new ProductView();
				product.setId("P1");
				product.setDesc("Product 1");
				product.setPrice(10);
				product.setQuantity(10);
				supp1.createProduct(product);
				product.setPrice(9);
				supp2.createProduct(product);
			}
			{
				ProductView product = new ProductView();
				product.setId("H2");
				product.setDesc("Product 2");
				product.setPrice(20);
				product.setQuantity(20);
				supp1.createProduct(product);
			}
			
		} catch (UDDINamingException | SupplierClientException | BadProductId_Exception | BadProduct_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void oneTimeTearDown() throws UDDINamingException {
		
		supp1.clear();
		supp2.clear();

	}


	// bad input tests

	@Test(expected = InvalidItemId_Exception.class)
	public void getItemsNullTest() throws InvalidItemId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		mediatorClient.getItems(null);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void getItemsEmptyTest() throws InvalidItemId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		mediatorClient.getItems("");
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void getItemsWhitespaceTest() throws InvalidItemId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		mediatorClient.getItems(" ");
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void getItemsTabTest() throws InvalidItemId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		mediatorClient.getItems("\t");
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void getItemsNewlineTest() throws InvalidItemId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		mediatorClient.getItems("\n");
	}
	

	



	
	
	//success tests
	@Test
	public void itemDoesntExistInOnlyOneSupplierTest() throws InvalidItemId_Exception, BadProductId_Exception{
		List<ItemView> products = mediatorClient.getItems("H2");
		assertEquals(products.get(0).getItemId().getProductId(),supp1.getProduct("H2").getId());
		assertEquals(products.size(),1);
	}
	
	@Test
	public void itemDoesntExistInAllSuppliersTest() throws InvalidItemId_Exception, BadProductId_Exception{
		List<ItemView> products = mediatorClient.getItems("N0");
		assertEquals(products.size(),0);		
	}
	
	@Test
	public void itemExistsInAllSuppliersTest() throws InvalidItemId_Exception, BadProductId_Exception{
		List<ItemView> products = mediatorClient.getItems("P1");
		assertEquals(products.get(0).getItemId().getProductId(),supp1.getProduct("P1").getId());
		assertEquals(products.get(1).getItemId().getProductId(),supp2.getProduct("P1").getId());
		assertEquals(products.size(),2);
	}

	@Test
	public void itemsAreSortedCorrectlyTest() throws InvalidItemId_Exception{
		List<ItemView> products = mediatorClient.getItems("P1");
		assertEquals(products.get(0).getPrice(),9);
		assertEquals(products.get(1).getPrice(),10);
	}


}
