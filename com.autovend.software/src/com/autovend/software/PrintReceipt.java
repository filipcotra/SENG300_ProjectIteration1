
/** 
 * Filip Cotra - 30086750
 * Khondaker Samin Rashid - 30143490
 * Nishan Soni - 30147280
 * Aaron Tigley - 30159927
 * Zainab Bari - 30154224
 */

package com.autovend.software;

import com.autovend.devices.AbstractDevice;
import com.autovend.devices.ReceiptPrinter;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.ReceiptPrinterObserver;

/**
 * Control software for the Print Receipt use case.
 */
public class PrintReceipt implements ReceiptPrinterObserver {

	private ReceiptPrinter printer;
	private int totalVal = 0; // Total value of the items
	private String amountPaidbyUser = "";
	private String changeNeeded = "";

	/**
	 * Initialize a printer for the Print Receipt use case. Also registers this
	 * class as an observer for the station's main scanner.
	 * 
	 * @param printer The Receipt Printer to be used
	 */
	public PrintReceipt(ReceiptPrinter printer) {
		this.printer = printer;
		this.printer.register(this); // Register this class as an observer of the printer
	}

	/**
	 * Prints out the receipt for the customer.
	 * 
	 * @param items      the items bought by the customer
	 * @param prices     the price of the item bought by the customer
	 * @param change     the change due after the customer has paid
	 * @param amountPaid the amount that the customer paid
	 */
	public void print(char[] items, char[] prices, char[] change, char[] amountPaid) {
		try {
			// Print the items and prices
			for (int i = 0; i < items.length; i++) {
				// Print each item
				printer.print(items[i]);
				printer.print(' ');
				// Print the price of the item
				printer.print(prices[i]);
				// Adds the value of that item to the total
				totalVal += (Character.getNumericValue(prices[i]));
				// Print a newline character after each item
				printer.print('\n');
			}

			// Printing the total val
			printer.print('T');
			printer.print(' ');
			// Print the total as a character
			printer.print(Character.forDigit(totalVal, 10));
			printer.print('\n'); // Print a newline character after the total

			// Taking the amount paid by the user and storing it as a string
			for (int j = 0; j < amountPaid.length; j++) {
				amountPaidbyUser += (String.valueOf(amountPaid[j]));
			}

			// Printing amount paid
			printer.print('A');
			printer.print('P');
			printer.print(' ');
			printer.print(' ');

			// Taking the total amount paid by the user and printing it
			for (int k = 0; k < amountPaidbyUser.length(); k++) {
				printer.print(amountPaidbyUser.charAt(k));
			}

			// Change Due
			printer.print('\n'); // Print a newline character after each item
			printer.print('C');
			printer.print(' ');

			// Taking the change due for the user and storing it as a string
			for (int l = 0; l < change.length; l++) {
				changeNeeded += (String.valueOf(change[l]));
			}

			// Printing the change due
			for (int m = 0; m < amountPaidbyUser.length(); m++) {
				printer.print(changeNeeded.charAt(m));
			}

			// Printing a newline character
			printer.print('\n');

			// Cut the paper
			printer.cutPaper();
		}
		// Catch any exceptions
		catch (Exception e) {
			System.out.println("Error printing receipt: " + e.getMessage());
		}
	}

	// Implement methods from the ReceiptPrinterObserver interface
	@Override
	public void reactToOutOfInkEvent(ReceiptPrinter printer) {
		System.out.println("Printer is out of ink");
	}

	@Override
	public void reactToOutOfPaperEvent(ReceiptPrinter printer) {
		System.out.println("Printer is out of paper");
	}

	@Override
	public void reactToInkAddedEvent(ReceiptPrinter printer) {
		System.out.println("Ink added to printer");
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
		// TODO Auto-generated method stub
	}
}