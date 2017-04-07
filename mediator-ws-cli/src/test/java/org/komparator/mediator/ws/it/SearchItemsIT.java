package org.komparator.mediator.ws.it;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.mediator.ws.InvalidText_Exception;
import org.komparator.mediator.ws.ItemView;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;


/**
 * Test suite
 */
public class SearchItemsIT extends BaseIT {

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
			// (since searchItemsIT is read-only the initialization below
			// can be done once for all tests in this suite)
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
			{
				ProductView product = new ProductView();
				product.setId("vg2");
				product.setDesc("vasco da gama");
				product.setPrice(5);
				product.setQuantity(20);
				supp1.createProduct(product);
			}
			{
				ProductView product = new ProductView();
				product.setId("g");
				product.setDesc("gama");
				product.setPrice(25);
				product.setQuantity(20);
				supp2.createProduct(product);
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
	
	
	@Test(expected = InvalidText_Exception.class)
	public void searchItemsNullTest() throws InvalidText_Exception {
		mediatorClient.searchItems(null);
	}

	@Test(expected = InvalidText_Exception.class)
	public void searchItemsEmptyTest() throws InvalidText_Exception {
		mediatorClient.searchItems("");
	}

	@Test(expected = InvalidText_Exception.class)
	public void searchItemsWhitespaceTest() throws InvalidText_Exception {
		mediatorClient.searchItems(" ");
	}

	@Test(expected = InvalidText_Exception.class)
	public void searchItemsTabTest() throws InvalidText_Exception {
		mediatorClient.searchItems("\t");
	}

	@Test(expected = InvalidText_Exception.class)
	public void searchItemsNewlineTest() throws InvalidText_Exception {
		mediatorClient.searchItems("\n");
	}
	
	
	
	@Test
	public void noItemMatchesTest() throws InvalidText_Exception {
		List<ItemView> products = mediatorClient.searchItems("beta");
		assertEquals(products.size(),0);
	}

	@Test
	public void matchingItemsOnDifferentSuppliersTest() throws InvalidText_Exception {
		List<ItemView> products = mediatorClient.searchItems("vasco da gama");
		assertEquals(products.size(),2);
		assertEquals(products.get(0).getDesc(),"vasco da gama");
		assertEquals(products.get(1).getDesc(),"vasco da gama");
	}	
	
	@Test
	public void ItemMatchesOnOneSupplierOnlyTest() throws InvalidText_Exception {
		List<ItemView> products = mediatorClient.searchItems("alfa");
		assertEquals(products.size(),1);
		assertEquals(products.get(0).getDesc(),"alfa");
	}

	@Test
	public void itemsAreCorrectlyOrderedTest() throws InvalidText_Exception {
		List<ItemView> products = mediatorClient.searchItems("gama");
		assertEquals(products.size(),3);
		assertEquals(products.get(0).getDesc(),"gama");
		assertEquals(products.get(1).getDesc(),"vasco da gama");
		assertEquals(products.get(1).getPrice(),5);
		assertEquals(products.get(2).getDesc(),"vasco da gama");
		assertEquals(products.get(2).getPrice(),10);
	}	

}
