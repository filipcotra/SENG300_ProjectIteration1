/** Filip Cotra - 30086750
 *  Khondaker Samin Rashid - 30143490
 *  Nishan Soni - 30147289
 *  Aaron Tigley - 30159927
 *  Zainab Bari - 30154224
 */
package com.autovend.software;

/** 
 * Control software for cash payment. Will be controlled
 * by notifications from observers (as indicated in the
 * use-case model) which will provide inputs and receive
 * outputs. Represents the "Pay with Cash" use-case.
 *  
 */
public class PayWithCash {
	/**
	 * amountDue - Essentially just the total cost of the added items.
	 * Default value is zero, as nothing has been added yet.
	 * changeDue - The amount of change due to the customer. Default zero.
	 */
	private double amountDue = 0;
	private double changeDue = 0;
	
	/**
	 * Method to update amountDue by item cost, representing
	 * the addition of items. Will return nothing, as it is just 
	 * updating a field. Not setting restrictions on the cost,
	 * which can presumably be negative.
	 * 
	 * @param costOfItem
	 * 				The cost of the item added to the bill.
	 */
	public void addToTotal(double costOfItem) {
		this.amountDue += costOfItem;
	}
	
	/**
	 * Method to get the current amountDue so that it can be
	 * displayed. The documentation is fuzzy on how this will
	 * work, but for the time being I am assuming that it will
	 * be displayed through some logic in "Add Item by Scanning"
	 * software, but should be stored here for future use. Will
	 * also have to be shared between payment methods as it updates
	 * if this ends up being a required implementation (see Open
	 * issues of this use-case).
	 */
	public double getCurrentTotal() {
		return this.amountDue;
	}
	
	/**
	 * Method to set the amount of change due to the customer.
	 * 
	 * @param change
	 * 			Double representing the amount of change due
	 */
	public void setChange(double change) {
		this.changeDue = change;
	}

	/**
	 * Method to get the amount of change due to the customer.
	 */
	public double getChange() {
		return this.changeDue;
	}
	
	/**
	 * Step 2&3 of use-case. Method to decrement amount with 
	 * insertion of valid bill by int representing bill value.
	 * Then, signals to Customer I/O the updated amount
	 * NOTE: I have no idea what customer I/O is.
	 * 
	 * @param billValue
	 * 				The value to be decremented from the bill.
	 */
	public void payWithBill(double billValue) {
		this.amountDue -= billValue;
		// CustomerIO.giveUpdatedAmount(this.amountDue);
		/**
		 * If the running total is 0 or less, signal the amount
		 * of change due.
		 */
		if(this.getCurrentTotal()<=0) {
			// This sets the change due as a positive double
			this.setChange(0.0 - this.getCurrentTotal());
			if(this.getChange() == 0.0) {
				// I assume something to do with printReceipt -
				// in any case no change is required
			}
			// Do not have to check for the case that change
			// is greater than 0, as that is impossible.
			else {
				// CashIO.dispenseChange(this.getChange())
				// As above, something to do with printReceipt
			}
		}
	}
	
}
