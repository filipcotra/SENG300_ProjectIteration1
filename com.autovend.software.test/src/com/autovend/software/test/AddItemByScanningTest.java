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

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.autovend.Barcode;
import com.autovend.BarcodedUnit;
import com.autovend.Numeral;
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
	MyCustomerIO customer;
	MyAttendantIO attendant;
	Barcode barcode;
	BarcodedUnit scannedItem;
	BarcodedUnit placedItem;
	
	class MyCustomerIO implements CustomerIO {
		
		
		@Override
		public BarcodedUnit scanItem() {
			System.out.println("An item has been scanned...");
			System.out.println("Unit ID: " + scannedItem.getBarcode().digits());
			System.out.println("Weight: " + scannedItem.getWeight());
			return scannedItem;
		}

		@Override
		public BarcodedUnit placeScannedItemInBaggingArea() {
			return placedItem;
		}

		@Override
		public void showUpdatedTotal(Double totalRemaining) {
			// Implement some basic function
		}

		@Override
		public void thankCustomer() {
			// TODO Auto-generated method stub
			
		}


		
	}
	
	class MyAttendantIO implements AttendantIO {

		@Override
		public boolean approveWeightDiscrepancy() {
			return true;
		}

		@Override
		public void changeRemainsNoDenom(double changeLeft) {
			// Implement some basic function		
		}

		@Override
		public void printDuplicateReceipt() {
			// TODO Auto-generated method stub
			
		}

	}
	/**
	 * An example test case that runs a normal execution of the use case
	 * You can delete this method later
	 */
	@Before
	public void setup() {
		
		barcode = new Barcode(Numeral.three, Numeral.zero, Numeral.one, Numeral.five, Numeral.nine, Numeral.nine, Numeral.two, Numeral.seven);
		scannedItem = new BarcodedUnit(barcode, 57606.2);
		placedItem = scannedItem;
		selfCheckoutStation = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20}, 
				new BigDecimal[] {new BigDecimal(1),new BigDecimal(2)}, 10000, 5);
		BarcodedProduct product = new BarcodedProduct(barcode, "example", new BigDecimal(10), 
				scannedItem.getWeight());
		
		ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode, product);
		
		customer = new MyCustomerIO();
		attendant = new MyAttendantIO();
		
		receiptPrinterController = new PrintReceipt(selfCheckoutStation.printer, customer, attendant);
		paymentController = new PaymentControllerLogic(selfCheckoutStation, customer, attendant, receiptPrinterController);
		
		addItemByScanningController = new AddItemByScanningController(selfCheckoutStation, customer, 
				attendant, paymentController);
		
	}
	
	@Test
	public void Steps1and2(){
		/**
		 *  Step 1: Laser Scanner: Detects a barcode and signals this to the System.
		 */
		addItemByScanningController.addItemByScanning();
		assertEquals(new MyCustomerIO().scanItem(), this.customer.scanItem());

		/**
		 *	Step 2: Blocks the self checkout station from further customer interaction.
		 *	A DisabledException should be thrown as all devices involved in this use case
		 *	become disabled during this step.
		 */
		try {
			addItemByScanningController.addItemByScanning();
		}
		catch(DisabledException e) {
			return;
		}
		fail("A DisabledException should have been thrown.");
		//assertEquals(new MyCustomerIO().scanItem(), this.customer.scanItem());
	}
	
	

}
