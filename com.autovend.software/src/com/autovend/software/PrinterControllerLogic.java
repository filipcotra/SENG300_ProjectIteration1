package com.autovend.software;

import com.autovend.devices.AbstractDevice;
import com.autovend.devices.EmptyException;
import com.autovend.devices.ReceiptPrinter;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.ReceiptPrinterObserver;

public class PrinterControllerLogic implements ReceiptPrinterObserver{
	SelfCheckoutStation station;
	CustomerIO myCustomer;
	AttendantIO myAttendant;
	String record;
	ReceiptPrinter printer;
	
	/**
	 * Basic constructor
	 * 
	 * @param SCS
	 * 		Self-Checkout Station on which to install the logic
	 * @param customer
	 * 		CustomerIO interface to represent customer session
	 * @param attendant
	 * 		AttendantIO interface that is monitoring the machine
	 */
	public PrinterControllerLogic(SelfCheckoutStation SCS, CustomerIO customer, AttendantIO attendant) {
		this.station = SCS;
		this.station.printer.register(this);
		this.myCustomer = customer;
		this.myAttendant = attendant;
		this.record = "";
		this.printer = station.printer;
	}
	
	/**
	 * Installation method. Just calls constructor and returns newly 
	 * made instance of the class which is installed on the given
	 * station.
	 */
	public static PrinterControllerLogic installPrinterController(SelfCheckoutStation SCS, CustomerIO customer, AttendantIO attendant) {
		return new PrinterControllerLogic(SCS, customer, attendant);
	}

	/** 
	 * Method to add details of payment to customers receipt. Specifically,
	 * will add the item and the cost of said item. This will have to come
	 * from AddItemByScanning so that items can be removed in future iterations.
	 * (Step 1)
	 * 
	 *@param itemName
	 *		Name of the item in String format
	 *@param itemCost
	 *		Cost of the item in double format
	 */
	public void addToRecord(String itemName, double itemCost) {
		String tempStr = "" + itemName + "\t" + itemCost;
		this.record += tempStr + '\n';
	}
	
	/**
	 * Method to print the record. The logic will not handle input of ink
	 * and paper obviously, so it is up to the test cases to simulated
	 * these being added. This method will simply iterate through the 
	 * record, printing character by character. (Step 2-6)
	 */
	public void printRecord() {
		for(int charIndex = 0; charIndex < record.length(); charIndex++) {
			try {
				this.printer.print(record.charAt(charIndex));
			}
			catch(Exception e) {
				// Unspecified functionality
			}
		}
		/** After printing everything, signal to CustomerIO that session is complete (Step 4)*/
		this.myCustomer.sessionComplete();
	}
	
	@Override
	public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// Ignoring in this iteration	
	}

	@Override
	public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// Ignoring in this iteration
	}

	@Override
	public void reactToOutOfPaperEvent(ReceiptPrinter printer) {
		this.myAttendant.giveDuplicateReceipt();
		// Suspend machine
	}

	@Override
	public void reactToOutOfInkEvent(ReceiptPrinter printer) {
		this.myAttendant.giveDuplicateReceipt();
		// Suspend machine
	}

	@Override
	public void reactToPaperAddedEvent(ReceiptPrinter printer) {
		// Ignoring in this iteration
	}

	@Override
	public void reactToInkAddedEvent(ReceiptPrinter printer) {
		// Ignoring in this iteration
	}
}
