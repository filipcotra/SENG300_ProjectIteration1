/** 
 * Filip Cotra - 30086750
 * Khondaker Samin Rashid - 30143490
 * Nishan Soni - 30147280
 * Aaron Tigley - 30159927
 * Zainab Bari - 30154224
 */

package com.autovend.software.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.autovend.Barcode;
import com.autovend.BarcodedUnit;
import com.autovend.Bill;
import com.autovend.Numeral;
import com.autovend.devices.AbstractDevice;
import com.autovend.devices.BillDispenser;
import com.autovend.devices.BillSlot;
import com.autovend.devices.DisabledException;
import com.autovend.devices.OverloadException;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.SimulationException;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.BillDispenserObserver;
import com.autovend.devices.observers.BillSlotObserver;
import com.autovend.external.ProductDatabases;
import com.autovend.products.BarcodedProduct;
import com.autovend.software.AddItemByScanningController;
import com.autovend.software.AttendantIO;
import com.autovend.software.CustomerIO;
import com.autovend.software.PaymentControllerLogic;
import com.autovend.software.PrintReceipt;
import com.autovend.software.test.PaymentWithCashTest.DispenserStub;
import com.autovend.software.test.PaymentWithCashTest.MyAttendantIO;
import com.autovend.software.test.PaymentWithCashTest.MyBillSlotObserver;
import com.autovend.software.test.PaymentWithCashTest.MyCustomerIO;

public class AllTogether {
	
	AddItemByScanningController addItemByScanningController;
	PaymentControllerLogic paymentController;
	PrintReceipt receiptPrinterController;
	SelfCheckoutStation selfCheckoutStation;
	MyBillSlotObserver billObserver;
	MyCustomerIO customer;
	MyAttendantIO attendant;
	Bill[] fiveDollarBills;
	Bill[] tenDollarBills;
	Bill[] twentyDollarBills;
	Bill[] fiftyDollarBills;
	BillSlot billSlot;
	Bill billFive;
	Bill billTen;
	Bill billTwenty;
	Bill billFifty;
	Bill billHundred;
	ArrayList<Integer> ejectedBills; 
	DispenserStub billObserverStub;
	final PrintStream originalOut = System.out;
	ByteArrayOutputStream baos;
	PrintStream ps;
	Barcode barcode1;
	Barcode barcode2;
	Barcode barcode3;
	Barcode barcode4;
	// Create scanned items:
	BarcodedUnit scannedItem1;
	BarcodedUnit scannedItem2;
	BarcodedUnit scannedItem3;
	BarcodedUnit scannedItem4;
	// Create placed items:
	BarcodedUnit placedItem1;
	BarcodedUnit placedItem2;
	BarcodedUnit placedItem3;
	BarcodedUnit placedItem4;
	// Create test products:
	BarcodedProduct testProduct1;
	BarcodedProduct testProduct2;
	BarcodedProduct testProduct3;
	BarcodedProduct testProduct4;
	
class MyCustomerIO implements CustomerIO {

	@Override
	public void scanItem(BarcodedUnit item) {
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
		slot.removeDanglingBill();
	}

	@Override
	public void notifyPlaceItemCustomerIO() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showUpdatedTotal(BigDecimal bigDecimal) {
		// TODO Auto-generated method stub
		
	}
		
		
	}
	
	class MyAttendantIO implements AttendantIO {

		@Override
		public boolean approveWeightDiscrepancy() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public void printDuplicateReceipt() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void changeRemainsNoDenom(BigDecimal bigDecimal) {
			// TODO Auto-generated method stub
			
		}

		
	}
	@Before
	public void setup() {
		// Setting up new print stream to catch printed output, used to test terminal output
				baos = new ByteArrayOutputStream();
				ps = new PrintStream(baos);
				System.setOut(ps);
				billFive = new Bill(5, Currency.getInstance("CAD"));
				billTen = new Bill(10, Currency.getInstance("CAD"));
				billTwenty = new Bill(20, Currency.getInstance("CAD"));
				billFifty = new Bill(50, Currency.getInstance("CAD"));
				billHundred = new Bill(100, Currency.getInstance("CAD"));
				selfCheckoutStation = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50,100}, 
						new BigDecimal[] {new BigDecimal(1),new BigDecimal(2)}, 10000, 5);
				customer = new MyCustomerIO();
				attendant = new MyAttendantIO();
				ejectedBills = new ArrayList<Integer>();		
				/* Load one hundred, $5, $10, $20, $50 bills into the dispensers so we can dispense change during tests.
				 * Every dispenser has a max capacity of 100 
				 */
				fiveDollarBills = new Bill[100];
				tenDollarBills = new Bill[100];
				twentyDollarBills = new Bill[100];
				fiftyDollarBills = new Bill[100];
				for(int i = 0; i < 100; i++) {
					fiveDollarBills[i] = billFive;
					tenDollarBills[i] = billTen;
					twentyDollarBills[i] = billTwenty;
					fiftyDollarBills[i] = billFifty;
				}
				try {
					selfCheckoutStation.billDispensers.get(5).load(fiveDollarBills);
					selfCheckoutStation.billDispensers.get(10).load(tenDollarBills);
					selfCheckoutStation.billDispensers.get(20).load(twentyDollarBills);
					selfCheckoutStation.billDispensers.get(50).load(fiftyDollarBills);
					selfCheckoutStation.printer.addInk(1024);
					selfCheckoutStation.printer.addPaper(1024);
				} catch (SimulationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OverloadException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// Create barcodes:
				barcode1 = new Barcode(Numeral.three, Numeral.zero, Numeral.one, Numeral.five, Numeral.nine, Numeral.nine, Numeral.two, Numeral.seven);
				barcode2 = new Barcode(Numeral.seven, Numeral.nine, Numeral.eight, Numeral.five, Numeral.six, Numeral.eight, Numeral.seven, Numeral.three);
				barcode3 = new Barcode(Numeral.six, Numeral.six, Numeral.eight, Numeral.zero, Numeral.two, Numeral.eight, Numeral.six, Numeral.nine);
				barcode4 = new Barcode(Numeral.seven, Numeral.five, Numeral.two, Numeral.one, Numeral.seven, Numeral.two, Numeral.two, Numeral.four);
				// Create scanned items:
				scannedItem1 = new BarcodedUnit(barcode1, 12);
				scannedItem2 = new BarcodedUnit(barcode2, 48);
				scannedItem3 = new BarcodedUnit(barcode3, 20);
				scannedItem4 = new BarcodedUnit(barcode4, 83);
				// Create placed items:
				placedItem1 = scannedItem1;
				placedItem2 = scannedItem2;
				placedItem3 = scannedItem3;
				placedItem4 = scannedItem4;
				// Create test products:
				testProduct1 = new BarcodedProduct(barcode1, "Item 1", new BigDecimal(10.0), 12);
				testProduct2 = new BarcodedProduct(barcode2, "Item 2", new BigDecimal(68.0), 48);
				testProduct3 = new BarcodedProduct(barcode3, "Item 3", new BigDecimal(50.0), 20);
				double price4 = 23.0;
				testProduct4 = new BarcodedProduct(barcode4, "Item 4", new BigDecimal(25.0), 83);
							
				ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode1, testProduct1);
				ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode2, testProduct2);
				ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode3, testProduct3);
				ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode4, testProduct4);
				
				// Create and attach controllers to the station:
				this.receiptPrinterController = new PrintReceipt(selfCheckoutStation, selfCheckoutStation.printer, customer, attendant);
				this.paymentController = new PaymentControllerLogic(selfCheckoutStation, customer, attendant, receiptPrinterController);
				this.addItemByScanningController = new AddItemByScanningController(selfCheckoutStation, customer, attendant, paymentController);
	}
	/* 
	 * Test Case: The customer scans two items. 
	 * 
	 * Description: This test is to see if scanning an item updates the cart total for the 
	 * payment controller.
	 * 
	 * Expected Result: Before the scan, the cart total should be 0. After scanning scannedItem1,
	 * it should up date to 10 dollars. Then, after bagging the item they scanned, they will then
	 * scan scannedItem2. The final cart total expected should be 78 dollars.
	 */
	@Test
	public void updateCartTotal() {
		assertTrue(BigDecimal.ZERO.compareTo(paymentController.getCartTotal())==0);
		// scan first item
		customer.scanItem(scannedItem1);
		customer.placeScannedItemInBaggingArea(placedItem1);
		// scan second item
		customer.scanItem(scannedItem2);
		customer.placeScannedItemInBaggingArea(placedItem2);
		assertTrue(BigDecimal.valueOf(78).compareTo(paymentController.getCartTotal())==0);
	}
	
	/* 
	 * Test Case: The customer pays the cart total
	 */
	@Test
	public void payForCartTotal() {
		// scan item
		customer.scanItem(scannedItem3);
		customer.placeScannedItemInBaggingArea(placedItem3);
		// The customer inserts a one-hundred dollar bill
		try {
			this.selfCheckoutStation.billInput.accept(billFifty);
		} catch (Exception e) {fail();}
		assertEquals(	  
				  "Item 3      $50\n"
				+ "Total: $50.00\n"
				+ "Paid: $50.0\n\n"
				+ "Change: $0.0\n",
				selfCheckoutStation.printer.removeReceipt());
	}
	
	/* 
	 * Test Case: The customer pays the cart total
	 * with change
	 */
	@Test
	public void payForCartTotalWithChange() {
		// scan first item
		customer.scanItem(scannedItem1);
		customer.placeScannedItemInBaggingArea(placedItem1);
		// scan second item
		customer.scanItem(scannedItem3);
		customer.placeScannedItemInBaggingArea(placedItem3);
		// The customer inserts a one-hundred dollar bill
		try {
			this.selfCheckoutStation.billInput.accept(billHundred);
		} catch (Exception e) {fail();}
		assertEquals(	  
				  "Item 1      $10\n"
				+ "Item 3      $50\n"
				+ "Total: $60.00\n"
				+ "Paid: $100.0\n\n"
				+ "Change: $40.0\n",
				selfCheckoutStation.printer.removeReceipt());
	}
	
	/* 
	 * Test Case: The customer pays the cart total, but not all at once.
	 * 
	 * Description: The user adds an item, then pays, then adds and pays once more.
	 * This was specified as necessary as per the discussion board.
	 */
	@Test
	public void addPayAddPay() {
		// scan first item
		customer.scanItem(scannedItem4);
		customer.placeScannedItemInBaggingArea(placedItem4);
		try {
			
		} catch (Exception e) {fail();}
		// scan second item
		customer.scanItem(scannedItem1);
		customer.placeScannedItemInBaggingArea(placedItem1);
		try {
			this.selfCheckoutStation.billInput.accept(billTwenty);
			this.selfCheckoutStation.billInput.accept(billTwenty);
		} catch (Exception e) {fail();}
	assertEquals(	  
				  "Item 4      $25\n"
				+ "Item 1      $10\n"
				+ "Total: $35.00\n"
				+ "Paid: $40.0\n\n"
				+ "Change: $5.0\n",
				selfCheckoutStation.printer.removeReceipt());
	}
	/*
	 * Given that all of the individual controller classes were tested
	 * individually, these tests should be sufficient to ensure that
	 * they work as a whole. No additional tests are needed, as this
	 * test set is only to see that they all work together, which they
	 * happen to do based on the scenarios observed here.
	 */
	
}
