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
	ArrayList<String> itemNameList;
	ArrayList<String> itemCostList;
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
			public BarcodedUnit scanItem() {
				// TODO Auto-generated method stub
				return null;
			}
		
			@Override
			public BarcodedUnit placeScannedItemInBaggingArea() {
				// TODO Auto-generated method stub
				return null;
			}
		
			@Override
			public void showUpdatedTotal(Double totalRemaining) {
				// TODO Auto-generated method stub	
			}
	
			@Override
			public void thankCustomer() {
				System.out.print("thankCustomer Called");
			}

			@Override
			public void removeBill(BillSlot slot) {
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
		public void changeRemainsNoDenom(double changeLeft) {
			// TODO Auto-generated method stub	
		}
	
		@Override
		public void printDuplicateReceipt() {
			System.out.print("printDuplicate Called");	
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
		itemNameList = new ArrayList<String>();
		itemNameList.add("item 1");
		itemNameList.add("item 2");
		itemNameList.add("item 3");
		// List of item prices:
		itemCostList = new ArrayList<String>();
		itemCostList.add("5.00");
		itemCostList.add("17.00");
		itemCostList.add("20.00");
		
		selfCheckoutStation = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50}, 
				new BigDecimal[] {new BigDecimal(1),new BigDecimal(2)}, 10000, 5);
		customer = new MyCustomerIO();
		attendant = new MyAttendantIO();
		
		receiptPrinterController = new PrintReceipt(selfCheckoutStation.printer, customer, attendant);
		
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
		receiptPrinterController.print(itemNameList, itemCostList, change, amountPaid);
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
		receiptPrinterController.print(itemNameList, itemCostList, change, amountPaid);
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
	 * occuring correctly. Steps 5-6 cannot really be tested.
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
		receiptPrinterController.print(itemNameList, itemCostList, change, amountPaid);
		String expected = "thankCustomer Called";
		assertTrue(expected.equals(baos.toString()));
	}
	
	
	/**
	 * Test: If the printer runs out of ink mid print.
	 * Expected: No receipt should be produced, the attendant should be
	 * informed through IO, and the machine should be suspended (meaning that
	 * a disabled exception should be thrown when attempting to use it
	 * again).
	 * Result:
	 */
	@Test (expected = DisabledException.class)
	public void printRunningOutOfInk_Test(){
		try {
			selfCheckoutStation.printer.addPaper(1024); // Add paper to printer
			selfCheckoutStation.printer.addInk(1); // Add insufficient ink for whole receipt
		} catch (OverloadException e) {}
		change = "0.00";
		amountPaid = "75.00";
		receiptPrinterController.print(itemNameList, itemCostList, change, amountPaid);
		// This is making sure that the printing was aborted, as no receipt is produced
		assertEquals(null, selfCheckoutStation.printer.removeReceipt());
		// This is making sure that the attendantIO was called
		String expected = "printDuplicate Called";
		assertTrue(expected.equals(baos.toString()));
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
	 * Result:
	 */
	@Test
	public void printRunningOutOfPaper_Test(){
		try {
			selfCheckoutStation.printer.addInk(1048576); // Add ink to printer
			selfCheckoutStation.printer.addPaper(1); // Add insufficient paper for whole receipt
		} catch (OverloadException e) {}
		change = "0.00";
		amountPaid = "75.00";
		receiptPrinterController.print(itemNameList, itemCostList, change, amountPaid);
		assertEquals(null, selfCheckoutStation.printer.removeReceipt());
		// This is making sure that the printing was aborted, as no receipt is produced
		assertEquals(null, selfCheckoutStation.printer.removeReceipt());
		// This is making sure that the attendantIO was called
		String expected = "printDuplicate Called";
		assertTrue(expected.equals(baos.toString()));
		// This is making sure that the system is suspended after running out of ink - disabledException should be thrown
		// To test this, BarcodeScanner.scan() will be tried, as that is the intiator of software interactions.
		selfCheckoutStation.mainScanner.scan(null);
	}
}
