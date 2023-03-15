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
import java.util.Currency;

import org.junit.Before;
import org.junit.Test;

import com.autovend.BarcodedUnit;
import com.autovend.Bill;
import com.autovend.devices.BillSlot;
import com.autovend.devices.DisabledException;
import com.autovend.devices.OverloadException;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.software.AttendantIO;
import com.autovend.software.CustomerIO;
import com.autovend.software.PaymentControllerLogic;
//import com.autovend.software.test.ReceiptPrinterTestH.MyReceiptPrinterObserver;
import com.autovend.devices.AbstractDevice;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.BillSlotObserver;

public class PaymentWithCashTest {

	PaymentControllerLogic paymentController;
	SelfCheckoutStation selfCheckoutStation;
	public MyBillSlotObserver billObserver;
	BillSlot billSlot;
	Bill bill;
	
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
		bill = new Bill(100, Currency.getInstance("CAD"));
		selfCheckoutStation = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50}, 
				new BigDecimal[] {new BigDecimal(1),new BigDecimal(2)}, 10000, 5);
		paymentController = new PaymentControllerLogic(selfCheckoutStation, new MyCustomerIO(), new MyAttendantIO(), null);
		
		
		
	}
	
	@Test
	public void addInvalidBill() {
		billObserver = new MyBillSlotObserver();
		try {
			selfCheckoutStation.billInput.register(billObserver);
			selfCheckoutStation.billInput.accept(bill);
			assertEquals(selfCheckoutStation.billInput,billObserver.device);
		} catch (DisabledException e) {
			return;
		} catch (OverloadException e) {
			return;
		}
	}

	

}
