package com.autovend.software.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
			System.out.println("The attendant has printed a duplicate receipt");
			
		}
	}
	
	@Before
	public void setUp() {
		// Set up string array lists for items and their respective prices.
		// List of items:
		itemNameList = new ArrayList<String>();
		itemNameList.add("item1");
		itemNameList.add("item2");
		itemNameList.add("item3");
		// List of item prices:
		itemCostList = new ArrayList<String>();
		itemCostList.add("5.00");
		itemCostList.add("17.00");
		itemCostList.add("20.00");
		
		selfCheckoutStation = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50}, 
				new BigDecimal[] {new BigDecimal(1),new BigDecimal(2)}, 10000, 5);
		customer = new MyCustomerIO();
		attendant = new MyAttendantIO();
		
		// Add ink and paper to the receipt printer.
		try {
			selfCheckoutStation.printer.addInk(1048576);
			selfCheckoutStation.printer.addPaper(1024);
		} catch (OverloadException e) {
		}
		receiptPrinterController = new PrintReceipt(selfCheckoutStation.printer, customer, attendant);
	}
	
	@Test
	public void printNoChange(){
		change = "0.00";
		amountPaid = "75.00";
		receiptPrinterController.print(itemNameList, itemCostList, change, amountPaid);
		//assertEquals("32.00", receiptPrinterController.getTotalVal());
		assertEquals("item1\nitem2\nitem3\nTotal: 42.00\nPaid: 75.00\n\nChange: 0.00", selfCheckoutStation.printer.removeReceipt());
	}
}
