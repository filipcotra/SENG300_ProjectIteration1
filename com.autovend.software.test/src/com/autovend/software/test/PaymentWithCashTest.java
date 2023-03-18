/** 
 * Filip Cotra - 30086750
 * Khondaker Samin Rashid - 30143490
 * Nishan Soni - 30147280
 * Aaron Tigley - 30159927
 * Zainab Bari - 30154224
 */

package com.autovend.software.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;

import org.junit.Before;
import org.junit.Test;

import com.autovend.BarcodedUnit;
import com.autovend.Bill;
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
import com.autovend.software.AttendantIO;
import com.autovend.software.CustomerIO;
import com.autovend.software.PaymentControllerLogic;
import com.autovend.software.PrintReceipt;

public class PaymentWithCashTest {

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
	Boolean attendantSignalled;
	
	class DispenserStub implements BillDispenserObserver {

		@Override
		public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToBillsFullEvent(BillDispenser dispenser) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToBillsEmptyEvent(BillDispenser dispenser) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToBillAddedEvent(BillDispenser dispenser, Bill bill) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToBillRemovedEvent(BillDispenser dispenser, Bill bill) {
			ejectedBills.add(bill.getValue());		
			
		}

		@Override
		public void reactToBillsLoadedEvent(BillDispenser dispenser, Bill... bills) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToBillsUnloadedEvent(BillDispenser dispenser, Bill... bills) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	class MyCustomerIO implements CustomerIO {

		@Override
		public void thankCustomer() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void removeBill(BillSlot slot) {
			slot.removeDanglingBill();
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
				// TODO Auto-generated method stub
				
			}

			@Override
			public void changeRemainsNoDenom(BigDecimal changeLeft) {
				System.out.print("changeRemainsNoDenom Called: " + changeLeft);
				attendantSignalled = true;
				
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
			slot.removeDanglingBill();
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
		attendantSignalled = false;
		// Setting up new print stream to catch printed output, used to test terminal output
		baos = new ByteArrayOutputStream();
		ps = new PrintStream(baos);
		System.setOut(ps);
		billFive = new Bill(5, Currency.getInstance("CAD"));
		billTen = new Bill(10, Currency.getInstance("CAD"));
		billTwenty = new Bill(20, Currency.getInstance("CAD"));
		billFifty = new Bill(50, Currency.getInstance("CAD"));
		billHundred = new Bill(100, Currency.getInstance("CAD"));
		selfCheckoutStation = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50}, 
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
		} catch (SimulationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OverloadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		receiptPrinterController = new PrintReceipt(selfCheckoutStation, selfCheckoutStation.printer, customer, attendant);
		paymentController = new PaymentControllerLogic(selfCheckoutStation, customer, attendant, receiptPrinterController);
		paymentController.setCartTotal(BigDecimal.ZERO);
		
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
	public void addInvalidBill_Test() {
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
	public void payLessThanTotal_Test() {
		
		paymentController.setCartTotal(BigDecimal.valueOf(100.00));
		try {
			selfCheckoutStation.billInput.accept(billTwenty);
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		} catch (OverloadException e) {
			fail("An OverloadException should not have been thrown");
		}
		assertTrue(BigDecimal.valueOf(80).compareTo(paymentController.getCartTotal()) == 0);
		assertEquals("20.0",paymentController.getAmountPaid());
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
		
		paymentController.setCartTotal(BigDecimal.valueOf(100.00));
		try {
			// The customer first inserts a twenty dollar bill into the bill slot.
			selfCheckoutStation.billInput.accept(billTwenty);
			assertTrue(BigDecimal.valueOf(80.00).compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("20.0",paymentController.getAmountPaid());
			
			// The customer then inserts a five dollar bill into the bill slot.
			selfCheckoutStation.billInput.accept(billFive);
			assertTrue(BigDecimal.valueOf(75.00).compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("25.0",paymentController.getAmountPaid());
			
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
		
		paymentController.setCartTotal(BigDecimal.valueOf(50.00));
		try {
			// The customer pays the full fifty dollars using a single fifty dollar bill.
			selfCheckoutStation.billInput.accept(billFifty);
			assertTrue(BigDecimal.ZERO.compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("50.0",paymentController.getAmountPaid());
			assertEquals("0.0",paymentController.getTotalChange());
			
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
	public void payFullWithChange_Test(){
		
		paymentController.setCartTotal(BigDecimal.valueOf(10.00));
		try {
			// The customer pays the full fifty dollars using a single fifty dollar bill.
			System.out.println("Payment: " + billFifty.getValue());
			selfCheckoutStation.billInput.accept(billFifty);
			assertTrue(BigDecimal.ZERO.compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("50.0",paymentController.getAmountPaid());
			assertEquals("40.0",paymentController.getTotalChange());
			
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		} catch (OverloadException e) {
			fail("An OverloadException should not have been thrown");
		} 
	}

	/* Test Case: The customer pays over the total cart amount by 30 dollars and the total change is dispensed. 
	 * 
	 * Description: The cart total is set at $20. $50 is paid in a single bill.
	 *    
	 * There is no need to test for payments that would require coins, credit, or crypto.
	 * Whether or not the cart Total is dropping has been tested already. So its not tested here.
	 * 
	 * Expected Result: The total change is calculated to 50-20 = 30
	 * Checking the total change should return a string value of "30.0".
	 * Checking the change due should return a double value of be "0.0" which is converted to string.
	 */
	@Test
	public void totalChangeDueThirtyDollars_Test() throws DisabledException, OverloadException{
		billObserverStub = new DispenserStub();
		selfCheckoutStation.billDispensers.get(20).register(billObserverStub);
		selfCheckoutStation.billDispensers.get(10).register(billObserverStub);
		paymentController.setCartTotal(BigDecimal.valueOf(20.00));		
		selfCheckoutStation.billInput.accept(billFifty);
		assertEquals("30.0",paymentController.getTotalChange());
		assertEquals("0.0",""+paymentController.getChangeDue());
		assertEquals("[10, 20]",ejectedBills.toString());
	}

	/* Test Case: To see if updateCartTotal functions properly.
	 * 
	 * Description: Update cart total will be called twice with 20.
	 * 
	 * Expected Result: The cart total should be 40.
	 */
	@Test
	public void cartUpdate_Test() {
		paymentController.updateCartTotal(BigDecimal.valueOf(20.00));
		paymentController.updateCartTotal(BigDecimal.valueOf(20.00));
		assertTrue(BigDecimal.valueOf(40.00).compareTo(paymentController.getCartTotal()) == 0);
	}
	
	/* Test Case: Testing dispenseChange when change is 0.
	 * 
	 * Description: Change will be set to zero and then dispense change will be called.
	 * 
	 * Expected Result: A Simulation Exception will be thrown
	 */
	@Test (expected = SimulationException.class)
	public void dispenseZeroChange_Test() {
		paymentController.setChangeDue(BigDecimal.ZERO);
		paymentController.dispenseChange();
	}
	
	/* Test Case: To see if the AttendantIO is being properly notified
	 * when change is below the minimum denom of 5.
	 * 
	 * Description: Will pay $5 when cost is 3, causing change of 2
	 * 
	 * Expected Result: AttendantIO should be called.
	 */
	@Test
	public void changeTooSmallAttendantIO_Test() throws OverloadException {
		paymentController.setCartTotal(BigDecimal.valueOf(3.00));
		selfCheckoutStation.billInput.accept(billFive);
		String expected = "changeRemainsNoDenom Called: 2.0";
		assertEquals(expected,baos.toString());
	}
	
	/* Test Case: When the denom that should be emitted is empty, but this
	 * is not the smallest denom.
	 * 
	 * Description: Will pay $50 when the charge is $30, so that the change is
	 * $20. Thus, the denom $20 should be attempted to be ejected, but will be
	 * empty.
	 * 
	 * Expected: Expecting two tens to be emitted, and coverage to be improved.
	 */
	@Test
	public void emitEmptyNotSmallest_Test() throws OverloadException {
		// Emptying billDispenser(20)
		selfCheckoutStation.billDispensers.get(20).unload();
		billObserverStub = new DispenserStub();
		selfCheckoutStation.billDispensers.get(20).register(billObserverStub);
		selfCheckoutStation.billDispensers.get(10).register(billObserverStub);
		paymentController.setCartTotal(BigDecimal.valueOf(30.00));		
		selfCheckoutStation.billInput.accept(billFifty);
		assertEquals("20.0",paymentController.getTotalChange());
		assertEquals("0.0",""+paymentController.getChangeDue());
		assertEquals("[10, 10]",ejectedBills.toString());
	}
	
	/* Test Case: When the denom that should be emitted is empty, but this
	 * is the smallest denom.
	 * 
	 * Description: Will pay $10 when the charge is $5, which should lead to 
	 * $5 being emitted. However, it will be empty.
	 * 
	 * Expected: AttendantIO should be called and disabledException should
	 * be thrown as the machine will be suspended.
	 */
	@Test
	public void emitEmptySmallest_Test() throws OverloadException {
		// Emptying billDispenser(20)
		selfCheckoutStation.billDispensers.get(5).unload();
		billObserverStub = new DispenserStub();
		selfCheckoutStation.billDispensers.get(20).register(billObserverStub);
		selfCheckoutStation.billDispensers.get(10).register(billObserverStub);
		paymentController.setCartTotal(BigDecimal.valueOf(5.00));		
		try{
			selfCheckoutStation.billInput.accept(billTen);
			fail();
		}
		catch(DisabledException e) {
			assertEquals("5.0",paymentController.getTotalChange());
			assertEquals("5.0",""+paymentController.getChangeDue());
			String expected = "changeRemainsNoDenom Called: 5.0";
			assertEquals(expected,baos.toString());
		}
	}
	/* Test Case: The customer pays over the total cart amount by 5 dollars and the total change is dispensed. 
	 * 
	 * Description: The cart total is set at $45. $50 is paid in a single bill.So one 5 dollar bill is ejected for
	 * the customer.
	 *    
	 * There is no need to test for payments that would require coins, credit, or crypto.
	 * Whether or not the cart Total is dropping has been tested already. So its not tested here.
	 * The attendant should also not be notified in this boundary case, which is being tested here.
	 * 
	 * Expected Result: The total change is calculated to 50-45 = 5
	 * Checking the total change should return a string value of "5.0".
	 * Checking the change due should return a double value of be "0.0" which is converted to string.
	 */
	@Test
	public void totalChangeDueFiveDollars() throws DisabledException, OverloadException{
		billObserverStub = new DispenserStub();
		selfCheckoutStation.billDispensers.get(5).register(billObserverStub);
		paymentController.setCartTotal(BigDecimal.valueOf(45.00));		
		selfCheckoutStation.billInput.accept(billFifty);
		assertEquals("5.0",paymentController.getTotalChange());	
		assertEquals("0.0",""+paymentController.getChangeDue());	
		assertEquals("[5]",ejectedBills.toString());
		assertFalse(attendantSignalled);
	}
	
}
