/** Filip Cotra - 30086750
 *  Khondaker Samin Rashid - 30143490
 *  Nishan Soni - 30147289
 *  Aaron Tigley - 30159927
 *  Zainab Bari - 30154224
 */

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
	 * updating a field.
	 * 
	 * @param costOfItem
	 * 			The cost of the item added to the bill.
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
	 * Since BillValidatorObserver is the only one that actually
	 * tracks the value of the currency, this must be called 
	 * from there.
	 * 
	 * @param billValue
	 * 			The value to be decremented from the bill.
	 */
	public void payWithBill(double billValue) {
		this.amountDue -= billValue;
		// CustomerIO.giveUpdatedAmount(this.amountDue);
		/**
		 * If the running total is 0 or less, signal the amount
		 * of change due. This check is broadly covering step
		 * 5&6.
		 */
		if(this.getCurrentTotal()<=0) {
			this.setChange(0.0 - this.getCurrentTotal());
			/**
			 * If there is change due, signal to cashIO that
			 * it must be dispensed. This will likely go through
			 * billDispenser.
			 */
			if(this.getChange() > 0.0){
				// Signal to cashIO that there is change 
				// due, but how?
			}
		}
	}
	
}
