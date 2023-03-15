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
		
		// load one hundred, $5, $10, $20, $50 bills into the dispensers so we can dispense change during tests.
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
			assertEquals(100,selfCheckoutStation.billDispensers.get(5).size());
			assertEquals(100,selfCheckoutStation.billDispensers.get(10).size());
			assertEquals(100,selfCheckoutStation.billDispensers.get(20).size());
			assertEquals(100,selfCheckoutStation.billDispensers.get(50).size());
		} catch (DisabledException e) {
			return;
		} catch (OverloadException e) {
			return;
		}
	}
	
	@Test
	public void signals() {
	}

	

}
