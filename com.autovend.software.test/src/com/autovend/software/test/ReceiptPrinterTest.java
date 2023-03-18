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

import org.junit.Before;
import org.junit.Test;

import com.autovend.BarcodedUnit;
import com.autovend.Bill;
import com.autovend.devices.BillSlot;
import com.autovend.devices.OverloadException;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.software.AttendantIO;
import com.autovend.software.CustomerIO;
import com.autovend.software.PrintReceipt;
import com.autovend.software.test.PaymentWithCashTest.MyBillSlotObserver;
import com.autovend.devices.DisabledException;

public class ReceiptPrinterTest {

	PrintReceipt receiptPrinterController;
	SelfCheckoutStation selfCheckoutStation;
	MyBillSlotObserver billObserver;
	MyCustomerIO customer;
	MyAttendantIO attendant;
	ArrayList<String> itemNameList = new ArrayList<String>();
	ArrayList<String> itemCostList = new ArrayList<String>();
	String change;
	String amountPaid;
	String itemFmt1;
	String itemFmt2;
	String itemFmt3;
	final PrintStream originalOut = System.out;
	ByteArrayOutputStream baos;
	PrintStream ps;
	class MyCustomerIO implements CustomerIO {
	
			@Override
			public void thankCustomer() {
				System.out.print("thankCustomer Called");
			}

			@Override
			public void removeBill(BillSlot slot) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void scanItem(BarcodedUnit item) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void notifyPlaceItemCustomerIO() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void placeScannedItemInBaggingArea(BarcodedUnit item) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void showUpdatedTotal(BigDecimal total) {
				// TODO Auto-generated method stub
				
			}
		
		}
	
	class MyAttendantIO implements AttendantIO {
	
		@Override
		public boolean approveWeightDiscrepancy() {
			// TODO Auto-generated method stub
			return false;
		}
	
		@Override
		public void printDuplicateReceipt() {
			System.out.print("printDuplicate Called");	
		}

		@Override
		public void changeRemainsNoDenom(BigDecimal changeLeft) {
			// TODO Auto-generated method stub
			
		}
	}
	
	@Before
	public void setUp() {
		// Setting up new print stream to catch printed output, used to test terminal output
		baos = new ByteArrayOutputStream();
		ps = new PrintStream(baos);
		System.setOut(ps);
		// Set up string array lists for items and their respective prices.
		// List of items:
		this.itemNameList.add("item 1");
		this.itemNameList.add("item 2");
		this.itemNameList.add("item 3");
		// List of item prices:
		this.itemCostList.add("5.00");
		this.itemCostList.add("17.00");
		this.itemCostList.add("20.00");
		
		selfCheckoutStation = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50}, 
				new BigDecimal[] {new BigDecimal(1),new BigDecimal(2)}, 10000, 5);
		customer = new MyCustomerIO();
		attendant = new MyAttendantIO();
		
		receiptPrinterController = new PrintReceipt(selfCheckoutStation, selfCheckoutStation.printer, customer, attendant);
		
	}
	
	/**
	 * Test: Given sufficient paper and ink, does the receipt get correctly printer
	 * when items and costs are provided, but there is no change.
	 * Expected: Receipt output in expected format (Which was undefined and so
	 * designed arbitrarily) with change equal to $0.00.
	 * Result: Test passes. This largely shows that steps 1-3 are occurring 
	 * correctly. They cannot really be individually tested because of how the
	 * software is working, but the end result is accurate.
	 */
	@Test
	public void printNoChange_Test(){
		// Add ink and paper to the receipt printer.
		try {
			selfCheckoutStation.printer.addInk(1048576);
			selfCheckoutStation.printer.addPaper(1024);
		} catch (OverloadException e) {}
		change = "0.00";
		amountPaid = "75.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		assertEquals(
				  "item 1      $5.00\n"
				+ "item 2      $17.00\n"
				+ "item 3      $20.00\n"
				+ "Total: $42.00\n"
				+ "Paid: $75.00\n\n"
				+ "Change: $0.00\n",
				selfCheckoutStation.printer.removeReceipt());
	}
	
	/**
	 * Test: Given sufficient paper and ink, does the receipt get correctly printer
	 * when items and costs are provided, and there was change dispensed.
	 * Expected: Receipt output in expected format (Which was undefined and so
	 * designed arbitrarily) with change equal to the total amount dispensed ($3.00).
	 * Result: Test passes. This largely shows that steps 1-3 are occurring 
	 * correctly. They cannot really be individually tested because of how the
	 * software is working, but the end result is accurate.
	 */
	@Test
	public void printWithChange_Test(){
		// Add ink and paper to the receipt printer.
		try {
			selfCheckoutStation.printer.addInk(1048576);
			selfCheckoutStation.printer.addPaper(1024);
		} catch (OverloadException e) {}
		change = "3.00";
		amountPaid = "45.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		assertEquals(
				  "item 1      $5.00\n"
				+ "item 2      $17.00\n"
				+ "item 3      $20.00\n"
				+ "Total: $42.00\n"
				+ "Paid: $45.00\n\n"
				+ "Change: $3.00\n",
				selfCheckoutStation.printer.removeReceipt());;
	}
	
	/**
	 * Test: Given a successful print, the CustomerIO should be informed.
	 * Expected: A call to CustomerIO.thankCustomer(), eliciting specified
	 * print.
	 * Result: Test passes. This shows that CustomerIO is successfully notified
	 * of the customers session being completed, demonstrating that step 4 is
	 * occurring correctly. Steps 5-6 cannot really be tested.
	 */
	@Test
	public void successfulPrintThankCustomer_Test(){
		// Add ink and paper to the receipt printer.
		try {
			selfCheckoutStation.printer.addInk(1048576);
			selfCheckoutStation.printer.addPaper(1024);
		} catch (OverloadException e) {}
		change = "3.00";
		amountPaid = "45.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		String expected = "thankCustomer Called";
		assertEquals(expected,baos.toString());
	}
	
	
	/**
	 * Test: If the printer runs out of ink mid print.
	 * Expected: No receipt should be produced, the attendant should be
	 * informed through IO, and the machine should be suspended (meaning that
	 * a disabled exception should be thrown when attempting to use it
	 * again).
	 * Result: Test passes.
	 */
	@Test (expected = DisabledException.class)
	public void printRunningOutOfInk_Test(){
		try {
			selfCheckoutStation.printer.addPaper(1024); // Add paper to printer
			selfCheckoutStation.printer.addInk(1); // Add insufficient ink for whole receipt
		} catch (OverloadException e) {}
		change = "0.00";
		amountPaid = "75.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		// This is making sure that the printing was aborted, as no receipt is produced
		assertEquals(null, selfCheckoutStation.printer.removeReceipt());
		// This is making sure that the attendantIO was called
		String expected = "printDuplicate Called";
		assertEquals(expected, baos.toString());
		// This is making sure that the system is suspended after running out of ink - disabledException should be thrown
		// To test this, BarcodeScanner.scan() will be tried, as that is the initiator of software interactions.
		selfCheckoutStation.mainScanner.scan(null);
	}
	
	/**
	 * Test: If the printer runs out of paper mid print.
	 * Expected: No receipt should be produced, the attendant should be
	 * informed through IO, and the machine should be suspended (meaning that
	 * a disabled exception should be thrown when attempting to use it
	 * again).
	 * Result: Test passes.
	 */
	@Test (expected = DisabledException.class)
	public void printRunningOutOfPaper_Test(){
		try {
			selfCheckoutStation.printer.addInk(1048576); // Add ink to printer
			selfCheckoutStation.printer.addPaper(1); // Add insufficient paper for whole receipt
		} catch (OverloadException e) {}
		change = "0.00";
		amountPaid = "75.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		assertEquals(null, selfCheckoutStation.printer.removeReceipt());
		// This is making sure that the printing was aborted, as no receipt is produced
		assertEquals(null, selfCheckoutStation.printer.removeReceipt());
		// This is making sure that the attendantIO was called
		String expected = "printDuplicate Called";
		assertEquals(expected,baos.toString());
		// This is making sure that the system is suspended after running out of ink - disabledException should be thrown
		// To test this, BarcodeScanner.scan() will be tried, as that is the intiator of software interactions.
		selfCheckoutStation.mainScanner.scan(null);
	}
	
	/**
	 * Test: Just to improve branch coverage. Looking for a normal print
	 * where the total cost does not end in a 0.
	 * Expected: Appropriate receipt, no exceptions.
	 * Result: Test passes and improved coverage.
	 */
	@Test
	public void totalCostNotEndingZero_Test(){
		// Add ink and paper to the receipt printer.
		try {
			selfCheckoutStation.printer.addInk(1048576);
			selfCheckoutStation.printer.addPaper(1024);
		} catch (OverloadException e) {}
		// Changing the price of one item cost so that it doesn't end in 0
		this.itemCostList.set(0,"5.35");
		change = "3.00";
		amountPaid = "45.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		assertEquals(
				  "item 1      $5.35\n"
				+ "item 2      $17.00\n"
				+ "item 3      $20.00\n"
				+ "Total: $42.35\n"
				+ "Paid: $45.00\n\n"
				+ "Change: $3.00\n",
				selfCheckoutStation.printer.removeReceipt());;
	}
	
	/**
	 * Test: To see if getTotalVal returns appropriate value.
	 * Expected: That the total cost will match the expected value.
	 * Result: Test passes. This is a bit of a redundant test, but improves
	 * coverage and ensures for once and all that totalVal is being
	 * correctly updated.
	 */
	@Test
	public void totalVal_Test() {
		// Add ink and paper to the receipt printer.
		try {
			selfCheckoutStation.printer.addInk(1048576);
			selfCheckoutStation.printer.addPaper(1024);
		} catch (OverloadException e) {}
		change = "3.00";
		amountPaid = "45.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		assertTrue(42.0==receiptPrinterController.getTotalVal());
	}
}
