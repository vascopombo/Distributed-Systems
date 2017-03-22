package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Test suite
 */
public class BuyProductIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws BadProductId_Exception, BadProduct_Exception {
	}

	@AfterClass
	public static void oneTimeTearDown() {
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() throws BadProductId_Exception, BadProduct_Exception {
		// clear remote service state before all tests
		client.clear();

		// fill-in test products
		// (since getProduct is read-only the initialization below
		// can be done once for all tests in this suite)
		{
			ProductView product = new ProductView();
			product.setId("X1");
			product.setDesc("Basketball");
			product.setPrice(10);
			product.setQuantity(10);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Baseball");
			product.setPrice(20);
			product.setQuantity(20);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Z3");
			product.setDesc("Soccer ball");
			product.setPrice(30);
			product.setQuantity(30);
			client.createProduct(product);
		}		
	}

	@After
	public void tearDown() {
	}

	// tests
	// assertEquals(expected, actual);

	// public String buyProduct(String productId, int quantity)
	// throws BadProductId_Exception, BadQuantity_Exception,
	// InsufficientQuantity_Exception {

	// bad input tests

	@Test(expected = BadProductId_Exception.class)
	public void buyProductNullTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct(null,1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductEmptyTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("",1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductWhitespaceTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct(" ",1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductTabTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\t",1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductNewlineTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\n",1);
	}
	
	@Test(expected = BadQuantity_Exception.class)
	public void buyProductNegativeQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1",-5);
	}	

	@Test(expected = BadQuantity_Exception.class)
	public void buyProductZeroQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", 0);
	}
	
	@Test(expected = InsufficientQuantity_Exception.class)
	public void buyProductInsufficientQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", 12);
	}
	
	@Test(expected = InsufficientQuantity_Exception.class)
	public void buyProductInsufficientQuantityTwiceTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", 6);
		client.buyProduct("X1", 6);
	}		
	
	@Test(expected = BadProductId_Exception.class)
	public void buyProductNonExistentProductTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X5", 12);
	}		
	
	@Test(expected = BadProductId_Exception.class)
	public void buyProductLowercaseNotExistsTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("x1",1);
	}
	
	
	// main tests
	
	@Test
	public void buyProductSuccessTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		String result = client.buyProduct("X1",6);
		List<PurchaseView> prc = client.listPurchases();
		String prcId = prc.get(0).getId();
		String prdId = prc.get(0).getProductId();
		assertEquals(result,prcId);
		assertEquals(4,client.getProduct("X1").getQuantity());
		assertEquals(prdId,"X1");
	}
	
	@Test
	public void buyProductSameTwiceTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		String id1 = client.buyProduct("X1",2);
		String id2 = client.buyProduct("X1", 2);
		List<PurchaseView> prc = client.listPurchases();
		assertNotEquals(id1,id2);
		assertEquals(2,prc.size());
		assertEquals(6,client.getProduct("X1").getQuantity());
	}
	
	@Test
	public void buyProductSeveralDifferentTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", 1);
		client.buyProduct("Y2", 3);
		client.buyProduct("X1", 2);
		client.buyProduct("Y2", 3);
		client.buyProduct("Z3", 4);
		client.buyProduct("Z3", 5);
		client.buyProduct("Z3", 3);
		List<PurchaseView> prc = client.listPurchases();
		assertEquals(7,prc.size());
	}
	
	@Test
	public void buyProductExactQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", 10);
		assertEquals(0,client.getProduct("X1").getQuantity());
	}
}
