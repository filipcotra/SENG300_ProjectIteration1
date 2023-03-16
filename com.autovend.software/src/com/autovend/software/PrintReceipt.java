
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
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.ReceiptPrinterObserver;

/**
 * Control software for the Print Receipt use case.
 */
public class PrintReceipt implements ReceiptPrinterObserver {

	ReceiptPrinter printer;
	int totalVal = 0; // Total value of the items
	String amountPaidbyUser = "";
	String changeNeeded = "";
	CustomerIO customer;
	AttendantIO attendant;

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
	public PrintReceipt(ReceiptPrinter printer, CustomerIO c, AttendantIO a) {
		this.printer = printer;
		// Register this class as an observer of the printer
		this.printer.register(this);
		this.customer = c;
		this.attendant = a;
	}

	/**
	 * The method that prints out the receipt for the customer. Starts by printing
	 * the items and their corresponding prices, and then the total, amount paid by
	 * the customer, and the change they were given.
	 * 
	 * @param items      the items bought by the customer
	 * @param prices     the price of the item bought by the customer
	 * @param change     the change due after the customer has paid
	 * @param amountPaid the amount that the customer paid
	 */
	public void print(ArrayList<String> items, ArrayList<String> prices, String change, String amountPaid) {
		try {

			// Print the items and prices
			for (int i = 0; i < items.size(); i++) {

				for (int j = 0; j < items.get(i).length(); j++) {
					printer.print(items.get(i).charAt(j));
				}
				for (int k = 0; k < items.get(i).length(); k++) {
					printer.print(prices.get(i).charAt(k));
					totalVal += (Double.parseDouble(amountPaid));
				}

				// Print a newline character after each item
				printer.print('\n');
			}

			// Printing the total val
			printer.print('T');
			printer.print('o');
			printer.print('t');
			printer.print('a');
			printer.print('l');
			printer.print(':');
			printer.print(' ');
			// Print the total as a character
			printer.print(Character.forDigit(totalVal, 10));

			// Print a newline character after the total
			printer.print('\n');

			// Printing amount paid
			printer.print('P');
			printer.print('a');
			printer.print('i');
			printer.print('d');
			printer.print(':');
			printer.print(' ');

			// Taking the total amount paid by the user and printing it
			for (int l = 0; l < amountPaid.length(); l++) {
				printer.print(amountPaid.charAt(l));
			}

			// Printing a newline character
			printer.print('\n');

			// Taking the change due for the user and storing it as a string
			for (int l = 0; l < change.length(); l++) {
				changeNeeded += (String.valueOf(change));
			}

			// Printing a newline character
			printer.print('\n');

			// Change Due
			printer.print('C');
			printer.print('h');
			printer.print('a');
			printer.print('n');
			printer.print('g');
			printer.print('e');
			printer.print(':');
			printer.print(' ');

			// Printing the change due
			for (int m = 0; m < change.length(); m++) {
				printer.print(change.charAt(m));
			}

			// Printing a newline character
			printer.print('\n');

			// Cut the paper
			printer.cutPaper();

			// Step 4: Once the receipt is printed, signals to Customer I/O that session is
			// complete.
			this.customer.thankCustomer();
		}
		// Catch any exceptions
		catch (Exception e) {
			System.out.println("Error printing receipt: " + e.getMessage());
		}
	}

	// Implement methods from the ReceiptPrinterObserver interface

	// Print duplicate receipt for the attendanct if the printer is out of paper
	@Override
	public void reactToOutOfInkEvent(ReceiptPrinter printer) {
		this.attendant.printDuplicateReceipt();
	}

	// Print duplicate receipt for the attendanct if the printer is out of ink
	@Override
	public void reactToOutOfPaperEvent(ReceiptPrinter printer) {
		this.attendant.printDuplicateReceipt();
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
