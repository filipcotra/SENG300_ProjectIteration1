/** 
 * Filip Cotra - 30086750
 * Khondaker Samin Rashid - 30143490
 * Nishan Soni - 30147280
 * Aaron Tigley - 30159927
 * Zainab Bari - 30154224
 */

package com.autovend.software.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Currency;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.autovend.Barcode;
import com.autovend.BarcodedUnit;
import com.autovend.Numeral;
import com.autovend.devices.BillSlot;
import com.autovend.devices.DisabledException;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.external.ProductDatabases;
import com.autovend.products.BarcodedProduct;
import com.autovend.software.AddItemByScanningController;
import com.autovend.software.AttendantIO;
import com.autovend.software.CustomerIO;
import com.autovend.software.PaymentControllerLogic;
import com.autovend.software.PrintReceipt;

/**
 * Test cases for AddItemByScanning
 */
public class AddItemByScanningTest {
	
	PrintReceipt receiptPrinterController;
	AddItemByScanningController addItemByScanningController;
	PaymentControllerLogic paymentController;
	SelfCheckoutStation selfCheckoutStation;
	BarcodedProduct testProduct;
	MyCustomerIO customer;
	MyAttendantIO attendant;
	Barcode barcode;
	BarcodedUnit scannedItem;
	BarcodedUnit placedItem;
	Boolean customerNotified;
	final PrintStream originalOut = System.out;
	ByteArrayOutputStream baos;
	PrintStream ps;
	boolean approveFlag;
	BarcodedProduct actualProduct;
	class MyCustomerIO implements CustomerIO {
		
		
		@Override
		public void scanItem(BarcodedUnit item) {
			/* 
			 * This is EXTREMELY important. This is why the tests were
			 * inconsistent - there is a random number generation to test
			 * for failed scans, and this is needed to ensure that if a 
			 * failed scan happens, the customer will scan again. This is
			 * why some tests were failing on some runs but not others.
			 * This should be included in all future stubbed attempts
			 * to simulate customer scanning, or else the same issues
			 * will arise.
			 */
			while(selfCheckoutStation.mainScanner.scan(item) == false);
		}

		@Override
		public void placeScannedItemInBaggingArea(BarcodedUnit item) {
			selfCheckoutStation.baggingArea.add(item);
		}

		@Override
		public void thankCustomer() {
			// TODO Auto-generated method stub
		
		}

		@Override
		public void removeBill(BillSlot slot) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyPlaceItemCustomerIO() {
			// TODO Auto-generated method stub
			System.out.print("Customer Notified");
			customerNotified = true;
		}

		@Override
		public void showUpdatedTotal(BigDecimal total) {
			// TODO Auto-generated method stub
			
		}


		
	}
	
	class MyAttendantIO implements AttendantIO {

		@Override
		public boolean approveWeightDiscrepancy() {
			return approveFlag;
		}

		@Override
		public void printDuplicateReceipt() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void changeRemainsNoDenom(BigDecimal changeLeft) {
			// TODO Auto-generated method stub
			
		}

	}
	/**
	 * An example setup that runs a normal execution of the use case
	 * You can delete this method later
	 */
	@Before
	public void setup() {
		ProductDatabases.BARCODED_PRODUCT_DATABASE.clear();
		// Setting up new print stream to catch printed output, used to test terminal output
		baos = new ByteArrayOutputStream();
		ps = new PrintStream(baos);
		System.setOut(ps);		
		customerNotified = false;
		barcode = new Barcode(Numeral.three, Numeral.zero, Numeral.one, Numeral.five, Numeral.nine, Numeral.nine, Numeral.two, Numeral.seven);
		scannedItem = new BarcodedUnit(barcode, 12.0);
		placedItem = scannedItem;
		selfCheckoutStation = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20}, 
				new BigDecimal[] {new BigDecimal(1),new BigDecimal(2)}, 10000, 5);
		testProduct = new BarcodedProduct(barcode, "example", new BigDecimal(10), 
				12);
		
		ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode, testProduct);
		customer = new MyCustomerIO();
		attendant = new MyAttendantIO();
		
		receiptPrinterController = new PrintReceipt(selfCheckoutStation, selfCheckoutStation.printer, customer, attendant);
		paymentController = new PaymentControllerLogic(selfCheckoutStation, customer, attendant, receiptPrinterController);
		
		addItemByScanningController = new AddItemByScanningController(selfCheckoutStation, customer, 
				attendant, paymentController);
		approveFlag = true;
	}
	
	@Test
	public void detectBarcode() {
		/**
		 *  Step 1: Laser Scanner: Detects a barcode and signals this to the System.
		 */	
		customer.scanItem(scannedItem);
		actualProduct = addItemByScanningController.getProduct();
		assertEquals(testProduct.getBarcode(),actualProduct.getBarcode());
	}
	
	@Test
	public void disabled() {
		/**
		 *	Step 2: Blocks the self checkout station from further customer interaction.
		 *	A DisabledException should be thrown as all devices involved in this use case
		 *	become disabled during this step.
		 */
		approveFlag = false;
		try {
			customer.scanItem(scannedItem);
			customer.scanItem(scannedItem);
		}
		catch(DisabledException e) {
			return;
		}
		fail("A DisabledException should have been thrown.");
	} 
	
	@Test
	public void properCharacteristics() {
		/**
		 * Step 3: System: Determines the characteristics (weight and cost) of the product associated with the 
		 * barcode.
		 */
		customer.scanItem(scannedItem);
		actualProduct = addItemByScanningController.getProduct();
		assertEquals(12.0,actualProduct.getExpectedWeight(),0.00);
		BigDecimal expectedPrice = new BigDecimal(10);
		assertEquals(expectedPrice,actualProduct.getPrice());
	}
	
	@Test
	public void updateExpectedWeight() {
		/**
		 * Step 4: System: Updates the expected weight from the Bagging Area
		 */
		assertEquals(0, addItemByScanningController.getExpectedWeight(), 0.00);
		customer.scanItem(scannedItem);
		assertEquals(12.0, addItemByScanningController.getExpectedWeight(), 0.00);
	}
	
	@Test
	public void notifyCustomer() {
		/**
		 * Step 5: System: Updates the expected weight from the Bagging Area
		 */
		customer.scanItem(scannedItem);
		assertEquals(true, customerNotified);
	}
	
	@Test
	public void notifyCustomerOnce() {
		/**
		 * Step 5: System: Updates the expected weight from the Bagging Area
		 */
		customer.scanItem(scannedItem);
		String expected = "Customer Notified";
		assertEquals(expected, baos.toString());
	}
	
	@Test
	public void weightChange() {
		/**
		 * Step 6: Bagging Area: Signals to the System that the weight has changed.
		 */
		assertEquals(0, addItemByScanningController.getActualWeight(), 0.00);
		customer.scanItem(scannedItem);
		customer.placeScannedItemInBaggingArea(placedItem);
		assertEquals(12, addItemByScanningController.getActualWeight(), 0.00);
	}
	
	@Test
	public void weightChange2() {
		/**
		 * Step 6: Bagging Area: Signals to the System that the weight has changed.
		 */
		placedItem = new BarcodedUnit(barcode, 15);
		assertEquals(0, addItemByScanningController.getActualWeight(), 0.00);
		customer.scanItem(scannedItem);
		customer.placeScannedItemInBaggingArea(placedItem);
		assertEquals(15, addItemByScanningController.getActualWeight(), 0.00);
	}
	
	
	@Test
	public void reenabled() {
		/**
		 *	Step 7: re-enables the system when there is no weight discrepancies 
		 *	found in the system
		 */
		try {
			customer.scanItem(scannedItem);
			customer.placeScannedItemInBaggingArea(placedItem);
			// should unblock the system afterwards.
			customer.scanItem(scannedItem);
		}
		catch(DisabledException e) {
			fail("A DisabledException should not have been thrown.");
			return;
		}
	}
	
	@Test
	public void weightDiscrepency() {
		approveFlag = false;
		placedItem = new BarcodedUnit(barcode, 15); // Different weight
		try {
			selfCheckoutStation.mainScanner.scan(scannedItem);
			selfCheckoutStation.baggingArea.add(placedItem);
			// should not block the system afterwards.
			selfCheckoutStation.mainScanner.scan(scannedItem);
		}
		catch(DisabledException e) {
			return;
		}
		fail("A DisabledException should have been thrown.");

	} 
}
