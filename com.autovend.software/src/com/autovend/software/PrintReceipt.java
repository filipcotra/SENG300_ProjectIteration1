
/** 
 * Filip Cotra - 30086750
 * Khondaker Samin Rashid - 30143490
 * Nishan Soni - 30147280
 * Aaron Tigley - 30159927
 * Zainab Bari - 30154224
 */

package com.autovend.software;

import java.util.ArrayList;

import com.autovend.devices.AbstractDevice;
import com.autovend.devices.ReceiptPrinter;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.ReceiptPrinterObserver;

/**
 * Control software for the Print Receipt use case.
 */
public class PrintReceipt implements ReceiptPrinterObserver {

	ReceiptPrinter printer;
	double totalVal; // Total value of the items
	String amountPaidbyUser = "";
	String changeNeeded = "";
	CustomerIO customer;
	AttendantIO attendant;
	SelfCheckoutStation station;
	boolean flagInk = true;
	boolean flagPaper = true;
	char[] totalChars = {'T','o','t','a','l',':',' ','$'};
	char[] paidChars = {'P','a','i','d',':',' ','$'};
	char[] changeChars = {'C','h','a','n','g','e',':',' ','$'};
	char[] priceSpace = {' ',' ',' ',' ',' ',' ','$'};

	/**
	 * Initialize a printer for the Print Receipt use case. Also registers this
	 * class as an observer for the station's main scanner.
	 * 
	 * @param printer The Receipt Printer to be used
	 * @param c       The customer that is interacting with the Print Receipt use
	 *                case
	 * @param a       The attendant that is interacting with the Print Receipt use
	 *                case
	 */
	public PrintReceipt(SelfCheckoutStation station, ReceiptPrinter printer, CustomerIO c, AttendantIO a) {
		this.station = station;
		this.printer = printer;
		// Register this class as an observer of the printer
		this.printer.register(this);
		this.customer = c;
		this.attendant = a;
	}

	public void setTotalVal(double totalVal) {
		this.totalVal = totalVal;
	}

	public double getTotalVal() {
		return this.totalVal;
	}

	/**
	 * Method to suspend all the hardware components of the self checkout system
	 */
	public void suspendSystem() {
		this.station.printer.disable();
		this.station.baggingArea.disable();
		this.station.mainScanner.disable();
		this.station.handheldScanner.disable();
		this.station.billInput.disable();
		this.station.billOutput.disable();
		this.station.billStorage.disable();
		this.station.billValidator.disable();
	}

	/**
	 * The method that prints out the receipt for the customer. Starts by printing
	 * the items and their corresponding prices, and then the total, amount paid by
	 * the customer, and the change they were given. Before print is called, each time
	 * flags for paper and ink should be checked.
	 * 
	 * @param items      the items bought by the customer
	 * @param prices     the price of the item bought by the customer
	 * @param change     the change due after the customer has paid
	 * @param amountPaid the amount that the customer paid
	 */
	public void print(ArrayList<String> items, ArrayList<String> prices, String change, String amountPaid) {
		try {
			for (int i = 0; i < items.size(); i++) {
				for (int k = 0; k < items.get(i).length(); k++) {
					this.callPrint(items.get(i).charAt(k));
				}

				// Creating some spacing between the items and their respective prices.
				for(char ch : this.priceSpace) {
					this.callPrint(ch);
				}

				for (int k = 0; k < prices.get(i).length(); k++) {
					this.callPrint(prices.get(i).charAt(k));
				}
				setTotalVal(totalVal += (Double.parseDouble(prices.get(i))));
				// Print a newline character after each item
				this.callPrint('\n');
			}

			// Printing the total val
			for(char ch : this.totalChars) {
				this.callPrint(ch);
			}
			
			// Print the total as a character
			String strTotalVal = Double.toString(totalVal);
			if (strTotalVal.charAt(strTotalVal.length() - 1) == '0') {
				strTotalVal += '0';
			}
			for (int i = 0; i < strTotalVal.length(); i++) {
				this.callPrint(strTotalVal.charAt(i));
			}

			// Print a newline character after the total
			this.callPrint('\n');

			// Printing amount paid
			for(char ch : this.paidChars) {
				this.callPrint(ch);
			}

			// Taking the total amount paid by the user and printing it
			for (int l = 0; l < amountPaid.length(); l++) {
				this.callPrint(amountPaid.charAt(l));
			}

			// Printing a newline character
			this.callPrint('\n');

			// Taking the change due for the user and storing it as a string
			for (int l = 0; l < change.length(); l++) {
				changeNeeded += (String.valueOf(change));
			}

			// Printing a newline character
			this.callPrint('\n');

			// Change Due
			for(char ch : this.changeChars) {
				this.callPrint(ch);
			}

			// Printing the change due
			for (int m = 0; m < change.length(); m++) {
				this.callPrint(change.charAt(m));
			}

			// Printing a newline character
			this.callPrint('\n');

			// Cut the paper
			printer.cutPaper();

			// Step 4: Once the receipt is printed, signals to Customer I/O that session is
			// complete.
			this.customer.thankCustomer();
		}
		catch(Exception e) {
			// Unspecified function
		}
	}

	// Method for calling print specifically, connecting software to hardware. This will
	// be easier than doing it as above.
	public void callPrint(char ch) throws Exception {
		if(!(this.flagPaper == false || this.flagInk == false)) {
			try {
				this.printer.print(ch);
			}
			catch(Exception e) {
				throw e;
			}
		}
		else {
			// This should hopefully abort the print method
			throw new Exception();
		}
	}
	
	// Implement methods from the ReceiptPrinterObserver interface

	// Print duplicate receipt for the attendant if the printer is out of paper
	@Override
	public void reactToOutOfInkEvent(ReceiptPrinter printer) {
		this.flagInk = false;
		this.suspendSystem();
		this.attendant.printDuplicateReceipt();
	}

	// Print duplicate receipt for the attendant if the printer is out of ink
	@Override
	public void reactToOutOfPaperEvent(ReceiptPrinter printer) {
		this.flagPaper = false;
		this.suspendSystem();
		this.attendant.printDuplicateReceipt();
	}

	@Override
	public void reactToInkAddedEvent(ReceiptPrinter printer) {
		flagInk = true;
	}

	// Implement methods from the AbstractDeviceObserver interface (unused in this
	// class)
	@Override
	public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// TODO Auto-generated method stub
	}

	@Override
	public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// TODO Auto-generated method stub
	}

	@Override
	public void reactToPaperAddedEvent(ReceiptPrinter printer) {
		flagPaper = true;
	}
}