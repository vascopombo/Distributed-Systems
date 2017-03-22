package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;

/**
 * Test suite
 */
public class SearchProductsIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws BadProductId_Exception, BadProduct_Exception {
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
		{
			ProductView product = new ProductView();
			product.setId("W4");
			product.setDesc("Blitz ball");
			product.setPrice(30);
			product.setQuantity(30);
			client.createProduct(product);
		}	
		{
			ProductView product = new ProductView();
			product.setId("V5");
			product.setDesc("Blitz ball");
			product.setPrice(30);
			product.setQuantity(30);
			client.createProduct(product);
		}
	}

	@AfterClass
	public static void oneTimeTearDown() {
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	// tests
	// assertEquals(expected, actual);

	// public List<ProductView> searchProducts(String descText) throws
	// BadText_Exception

	// bad input tests

	@Test(expected = BadText_Exception.class)
	public void searchProductsNullTest() throws BadText_Exception {
		client.searchProducts(null);
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsEmptyTest() throws BadText_Exception {
		client.searchProducts("");
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsWhitespaceTest() throws BadText_Exception {
		client.searchProducts(" ");
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsTabTest() throws BadText_Exception {
		client.searchProducts("\t");
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsNewlineTest() throws BadText_Exception {
		client.searchProducts("\n");
	}

	
	// main tests

	@Test
	public void searchProductsSingleWordTest() throws BadText_Exception {
		List<ProductView> prodList = client.searchProducts("Basketball");
		assertEquals(prodList.get(0).getId(),"X1");
	}

	@Test
	public void searchProductsTwoWordsTest() throws BadText_Exception {
		List<ProductView> prodList = client.searchProducts("Soccer ball");
		assertEquals(prodList.get(0).getDesc(),"Soccer ball");
	}	
	
	@Test
	public void searchProductsTwoInstancesTest() throws BadText_Exception {
		List<ProductView> prodList = client.searchProducts("Blitz ball");
		assertEquals(2,prodList.size());
		assertEquals(prodList.get(0).getDesc(),prodList.get(1).getDesc());
	}
	
	@Test
	public void searchProductsDoesNotExistTest() throws BadText_Exception {
		List<ProductView> prodList = client.searchProducts("Tennis ball");
		assertEquals(0,prodList.size());
	}	
	
	@Test
	public void searchProductsLowercaseNotExistsTest() throws BadText_Exception {
		// product descriptions are case sensitive,
		// so "x1" is not the same as "X1"
		List<ProductView> prodList = client.searchProducts("bLiTz BaLl");
		assertEquals(0,prodList.size());
	}

}
