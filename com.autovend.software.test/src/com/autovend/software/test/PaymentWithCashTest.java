package com.autovend.software.test;
import static org.junit.Assert.assertEquals;
/*
 * Open issues:
 * 1. The hardware has to handle invalid cash, to reject it without involving the control software.
 * 2. Should mixed modes of payment be supported?
 * 
 * Exceptions:
 * 1. If the customer inserts cash that is deemed unacceptable, this will be returned to the customer 
 * without involving the System, presumably handled in the hardware.
 * 2. If insufficient change is available, the attendant should be signaled so that maintenance can be conducted on it.
 */
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;

import com.autovend.BarcodedUnit;
import com.autovend.Bill;
import com.autovend.devices.BillSlot;
import com.autovend.devices.DisabledException;
import com.autovend.devices.OverloadException;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.SimulationException;
import com.autovend.software.AttendantIO;
import com.autovend.software.CustomerIO;
import com.autovend.software.PaymentControllerLogic;
import com.autovend.devices.AbstractDevice;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.BillSlotObserver;

public class PaymentWithCashTest {

	PaymentControllerLogic paymentController;
	SelfCheckoutStation selfCheckoutStation;
	MyBillSlotObserver billObserver;
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
				// TODO Auto-generated method stub
				
			}
	}
	
	class MyBillSlotObserver implements BillSlotObserver{

		public AbstractDevice<? extends AbstractDeviceObserver> device = null;
		
		@Override
		public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			this.device = device;
		}

		@Override
		public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			this.device = device;
		}

		@Override
		public void reactToBillInsertedEvent(BillSlot slot) {
			// TODO Auto-generated method stub
			this.device = slot;
		}

		@Override
		public void reactToBillEjectedEvent(BillSlot slot) {
			// TODO Auto-generated method stub
			this.device = slot;
			System.out.println("Bill has been ejected from the bill slot.");
			
			
		}

		@Override
		public void reactToBillRemovedEvent(BillSlot slot) {
			// TODO Auto-generated method stub
			this.device = slot;
		}
		
	}
	
	@Before
	public void setUp() {
		
		billFive = new Bill(5, Currency.getInstance("CAD"));
		billTen = new Bill(10, Currency.getInstance("CAD"));
		billTwenty = new Bill(20, Currency.getInstance("CAD"));
		billFifty = new Bill(50, Currency.getInstance("CAD"));
		billHundred = new Bill(100, Currency.getInstance("CAD"));
		selfCheckoutStation = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50}, 
				new BigDecimal[] {new BigDecimal(1),new BigDecimal(2)}, 10000, 5);
		attendant = new MyAttendantIO();
		
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
		} catch (SimulationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OverloadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		paymentController = new PaymentControllerLogic(selfCheckoutStation, new MyCustomerIO(), attendant, null);
		paymentController.setCartTotal(0);
		
	}
	
	/* Test Case: Inserting an invalid bill to the self-checkout machine
	 * 
	 * Description: "If the customer inserts cash that is deemed unacceptable, 
	 * this will be returned to the customer without involving the System,
	 * presumably handled in hardware." What I mean by "invalid bill" is
	 * a bill that does not meet the set denominations of bills that the machine
	 * can accept.
	 * 
	 * Expected Result: The bill slot observer should call the 
	 * reactToBillEjectedEvent method an eject the bill from the machine.
	 */
	@Test
	public void addInvalidBill() {
		billObserver = new MyBillSlotObserver();
		try {
			selfCheckoutStation.billInput.register(billObserver);
			selfCheckoutStation.billInput.accept(billHundred);
			assertEquals(selfCheckoutStation.billInput,billObserver.device);
		} catch (DisabledException e) {
			return;
		} catch (OverloadException e) {
			return;
		}
	}
	
	/* Test Case: An amount less than the cart total is paid. 
	 * 
	 * Description: The cart total is set at $100. $20 is paid in a single bill. 
	 * 
	 * There is no need to test for payments that would require coins, credit, or crypto.
	 * 
	 * Expected Result: The cart total should drop from $100 to $80. Checking the amount paid
	 * should return a string value of "20".
	 */
	@Test
	public void payLessThanTotal() {
		
		paymentController.setCartTotal(100.00);
		try {
			selfCheckoutStation.billInput.accept(billTwenty);
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		} catch (OverloadException e) {
			fail("An OverloadException should not have been thrown");
		}
		assertEquals(80.00,paymentController.getCartTotal(),0.00);
		assertEquals("20",paymentController.getAmountPaid());
	}
	
	/* Test Case: The customer pays with a single bill on two separate instances. 
	 * 
	 * Description: The cart total is set at $100. $20 is paid in a single bill and
	 * then $5 dollars is paid in a single bill. 
	 * 
	 * The purpose of this test is to see if any weird behaviors/occurrences happen to 
	 * either the cart total or the amount paid.
	 *    
	 * There is no need to test for payments that would require coins, credit, or crypto.
	 * 
	 * Expected Result: The cart total should drop from $100 to $80. Then it should drop 
	 * to $75 on the second bill insertion. Checking the amount paid should return a string
	 * value of "20", then after the second bill insertion update to a string value of "25".
	 */
	@Test
	public void payTwice() {
		
		paymentController.setCartTotal(100.00);
		try {
			// The customer first inserts a twenty dollar bill into the bill slot.
			selfCheckoutStation.billInput.accept(billTwenty);
			assertEquals(80.00,paymentController.getCartTotal(),0.00);
			assertEquals("20",paymentController.getAmountPaid());
			
			// The customer then inserts a five dollar bill into the bill slot.
			selfCheckoutStation.billInput.accept(billFive);
			assertEquals(75.00,paymentController.getCartTotal(),0.00);
			assertEquals("25",paymentController.getAmountPaid());
			
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		} catch (OverloadException e) {
			fail("An OverloadException should not have been thrown");
		}
	}
	
	/* Test Case: The customer pays exactly the total cart amount. 
	 * 
	 * Description: The cart total is set at $50. $50 is paid in a single bill.
	 * 
	 * There shouldn't be a need to test this with multiple instances of paying with cash
	 * as the previous test had covered any weird behaviors that could have occurred.
	 *    
	 * There is no need to test for payments that would require coins, credit, or crypto.
	 * 
	 * Expected Result: The cart total should drop from $50 to $0. 
	 * Checking the amount paid should return a string value of "50".
	 * Checking the total change should return a string value of be "0.00'.
	 */
	@Test
	public void payFullNoChange() {
		
		paymentController.setCartTotal(50.00);
		try {
			// The customer pays the full fifty dollars using a single fifty dollar bill.
			selfCheckoutStation.billInput.accept(billFifty);
			assertEquals(0.00,paymentController.getCartTotal(),0.00);
			assertEquals("50",paymentController.getAmountPaid());
			assertEquals("0.00",paymentController.getTotalChange());
			
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		} catch (OverloadException e) {
			fail("An OverloadException should not have been thrown");
		}
	}
	
	/* Test Case: The customer pays over the total cart amount. 
	 * 
	 * Description: The cart total is set at $10. $50 is paid in a single bill.
	 * 
	 * There shouldn't be a need to test this with multiple instances of paying with cash
	 * as the previous test had covered any weird behaviors that could have occurred.
	 *    
	 * There is no need to test for payments that would require coins, credit, or crypto.
	 * 
	 * Expected Result: The cart total should drop from $10 to $0. 
	 * Checking the amount paid should return a string value of "50".
	 * Checking the total change should return a string value of be "40.00".
	 */
	@Test
	public void payFullWithChange() {
		
		paymentController.setCartTotal(10.00);
		try {
			// The customer pays the full fifty dollars using a single fifty dollar bill.
			selfCheckoutStation.billInput.accept(billFifty);
			assertEquals(0.00,paymentController.getCartTotal(),0.00);
			assertEquals("50",paymentController.getAmountPaid());
			assertEquals("40.00",paymentController.getTotalChange());
			
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		} catch (OverloadException e) {
			fail("An OverloadException should not have been thrown");
		}
	}

	

}
