/** 
 * Filip Cotra - 30086750
 *  Khondaker Samin Rashid - 30143490
 *  Nishan Soni - 30147289
 *  Aaron Tigley - 30159927
 *  Zainab Bari - 30154224
 */

package com.autovend.software;

import java.util.Arrays;
import java.util.Currency;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;

import com.autovend.Bill;
import com.autovend.devices.AbstractDevice;
import com.autovend.devices.BillDispenser;
import com.autovend.devices.BillValidator;
import com.autovend.devices.EmptyException;
import com.autovend.devices.ReceiptPrinter;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.BillValidatorObserver;
import com.autovend.devices.observers.BillDispenserObserver;
import com.autovend.devices.observers.BillStorageObserver;
import com.autovend.devices.SelfCheckoutStation;

/**
 * Control software for payment use-cases in self checkout station.
 * 
 * @author Filip Cotra
 */
public class PaymentControllerLogic implements BillValidatorObserver, BillDispenserObserver {
	private double cartTotal;
	private double changeDue;
	private SelfCheckoutStation station;
	private int[] denominations;
	private Map<Integer, BillDispenser> dispensers;
	private int maxDenom;
	private int minDenom;
	private ReceiptPrinter printer;
	private CustomerIO myCustomer;
	private AttendantIO myAttendant;
	
	/**
	 * Constructor. Takes a Self-Checkout Station  and initializes
	 * fields while also registering the logic as an observer of 
	 * BillSlot, BillValidator, and BillDispenser objects of the 
	 * station. Sorts denominations in ascending order to facilitate
	 * dispensing change later.
	 * 
	 * @param SCS
	 * 		Self-Checkout Station on which to install the logic
	 */
	public PaymentControllerLogic(SelfCheckoutStation SCS, CustomerIO customer, AttendantIO attendant) {
		station = SCS;
		station.billValidator.register(this);
		this.denominations = station.billDenominations;
		Arrays.sort(this.denominations);
		this.maxDenom = Arrays.stream(this.denominations).max().getAsInt();
		this.minDenom = Arrays.stream(this.denominations).min().getAsInt();
		this.dispensers = station.billDispensers;
		this.printer = station.printer;
		for (int value : this.denominations) {
			this.dispensers.get(value).register(this);
		}
		this.myCustomer = customer;
		this.myAttendant = attendant;
	}
	
	/**
	 * Installation method. Just calls constructor and returns newly 
	 * made instance of the class which is installed on the given
	 * station.
	 */
	public static PaymentControllerLogic installPaymentControll(SelfCheckoutStation SCS, CustomerIO customer, AttendantIO attendant) {
		return new PaymentControllerLogic(SCS, customer, attendant);
	}

	/**
	 * Updates cartTotal. Takes any double. Would presumably be updated
	 * by ScannerControllerLogic, as that is where the cost is being found.
	 * Updates by item, not all at once, so will have to be called numerous
	 * times. NEEDS TO BE INTEGRATED
	 * 
	 * @param price
	 * 		amount to be added to running total
	 */
	public void updateCartTotal(double price) {
		this.cartTotal += price;
	}

	/**
	 * Sets cart total. Takes a double. Only to be called within this
	 * class.
	 * 
	 * @param total
	 * 		amount to set the total cost of the cart
	 */
	
	public void setCartTotal(double total) {
		this.cartTotal += total;
	}
	
	/**
	 * Getter for cartTotal. Just returns value.
	 */
	public double getCartTotal() {
		return this.cartTotal;
	}
	
	/**
	 * Setter for changeDue. Takes any double. Is only to be called within this
	 * class.
	 * 
	 * @param change
	 * 		amount of change that is due
	 */
	private void setChangeDue(double change) {
		this.cartTotal = change;
	}
	
	/**
	 * Getter for cartTotal. Returns double. No need to call from any other class.
	 */
	private double getChangeDue() {
		return this.changeDue;
	}
	
	/**
	 * Dispenses change based on denominations. Looks through denominations in the 
	 * installed station and finds dispensers for each based on value, and determines
	 * first whether the denomination is appropriate (the largest bill smaller than the
	 * change left) and then if the dispenser is empty or not. If both conditions are met, 
	 * it dispenses. If not, it finds the next best denomination before dispensing.
	 * (Step 7)
	 */
	public void dispenseChange() {
		BillDispenser dispenser;
		/** Go through denominations backwards, largest to smallest */
		for(int value = denominations.length-1 ; value >= 0 ; value--) {
			dispenser = dispensers.get(value);
			/** If the value of the bill is less than or equal to the change and change is payable */
			if((value <= this.getChangeDue())&&(this.getChangeDue()>this.minDenom)) {
				/** If this dispenser carries the largest denomination, emit immediately */
				if(value == this.maxDenom) {
					try {
						dispenser.emit();
					}
					catch(EmptyException ee) {
						/** If empty, just move on to smaller denom */
						continue;
					}
					catch(Exception e) {
						// Unspecified functionality
					}
				}
				else {
					/** Since this is smaller than the change due but largest as we are moving backwards, just emit */
					try {
						dispenser.emit();
					}
					/** If empty and not the smallest denom, move on. If the smallest denom, inform attendant */
					catch(EmptyException e) {
						if(value == this.minDenom) {
							/** In this case change will be larger than smallest denom but unpayable */
							myAttendant.changeRemainsNoDenom(this.getChangeDue());
							// Suspend machine - Disable everything? Ask in meeting
						}
						else {
							continue;
						}
					}
					catch(Exception e) {
						// Unspecified functionality
					}
				}
			}
			/** If the changeDue is less than the lowest denom, call attendant automatically */
			else if(this.getChangeDue()<this.minDenom) {
				myAttendant.changeRemainsNoDenom(this.getChangeDue());
				/** No need to suspend machine, nothing is empty its just a lack of denoms */
			}
		}
	}
	
	/**
	 * Subtracts value from the cart based on the value of the bill
	 * added. (Step 2, Step 3, Step 5, Step 6)
	 */
	public void payBill(int billValue) {
		this.setCartTotal(this.getCartTotal() - billValue);
		myCustomer.showUpdatedTotal(this.getCartTotal());
		/** If the customer has paid their cart, check for change */
		if(this.getCartTotal() <= 0) {
			this.setChangeDue(0.0 - this.getCartTotal());
			if(this.getChangeDue() > 0) {
				this.dispenseChange();
			}
			else {
				//SHOULD CALL PRINT RECEIPT LOGIC - INTEGRATION 
			}
		}
	}
	
	@Override
	public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// Ignoring in this iteration
		
	}

	@Override
	public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// Ignoring in this iteration
		
	}

	/**
	 * Making this observer call the payment method. This makes sense, as it is the only input
	 * observer that actually has the bill and its value, and it only makes sense that payment
	 * calculations are made after the bill has actually been validated. (Step 1)
	 */
	@Override
	public void reactToValidBillDetectedEvent(BillValidator validator, Currency currency, int value) {
		this.payBill(value);
	}

	/**
	 * Thinking to just call CustomerIO and inform them of invalid bill - Ask in meeting.
	 */
	@Override
	public void reactToInvalidBillDetectedEvent(BillValidator validator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToBillsFullEvent(BillDispenser dispenser) {
		// Ignoring in this iteration
		
	}

	@Override
	public void reactToBillsEmptyEvent(BillDispenser dispenser) {
		// Ignoring in this iteration
		
	}

	@Override
	public void reactToBillAddedEvent(BillDispenser dispenser, Bill bill) {
		// Ignoring in this iteration
		
	}

	/**
	 * Setting this observer to update the amount of change remaining, as it
	 * should not be updated until the actual bill has been dispensed to the
	 * customer. It presumably has to do nothing else, as after the change
	 * has been dispensed control goes over to the print receipt logic.
	 * If after dispensing there is no change left, life is good. If not,
	 * it calls dispense change again. (Step 6)
	 */
	@Override
	public void reactToBillRemovedEvent(BillDispenser dispenser, Bill bill) {
		this.setChangeDue(this.getChangeDue()-bill.getValue());
		if(this.getChangeDue() > 0) {
			this.dispenseChange();
		}
		else {
			// SHOULD CALL RECEIPT PRINTER LOGIC - INTEGRATION
		}
	}

	@Override
	public void reactToBillsLoadedEvent(BillDispenser dispenser, Bill... bills) {
		// Ignoring in this iteration
		
	}

	@Override
	public void reactToBillsUnloadedEvent(BillDispenser dispenser, Bill... bills) {
		// Ignoring in this iteration
		
	}
	
	
	
}
