package com.autovend.software.test;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;

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
import com.autovend.software.AttendantIO;
import com.autovend.software.CustomerIO;
import com.autovend.software.PaymentControllerLogic;
import com.autovend.software.PrintReceipt;
import com.autovend.software.test.PaymentWithCashTest.DispenserStub;
import com.autovend.software.test.PaymentWithCashTest.MyAttendantIO;
import com.autovend.software.test.PaymentWithCashTest.MyBillSlotObserver;
import com.autovend.software.test.PaymentWithCashTest.MyCustomerIO;

public class AllTogether {
	
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
	Barcode barcode;
	BarcodedUnit scannedItem;
	BarcodedUnit placedItem;
	
	
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
				
				barcode = new Barcode(Numeral.three, Numeral.zero, Numeral.one, Numeral.five, Numeral.nine, Numeral.nine, Numeral.two, Numeral.seven);
				scannedItem = new BarcodedUnit(barcode, 12);
				placedItem = scannedItem;
				
				receiptPrinterController = new PrintReceipt(selfCheckoutStation, selfCheckoutStation.printer, customer, attendant);
				paymentController = new PaymentControllerLogic(selfCheckoutStation, customer, attendant, receiptPrinterController);
				selfCheckoutStation.mainScanner.scan(scannedItem);

	}
}
