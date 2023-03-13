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
import com.autovend.devices.BillSlot;
import com.autovend.devices.BillValidator;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.BillSlotObserver;
import com.autovend.devices.observers.BillValidatorObserver;
import com.autovend.devices.observers.BillDispenserObserver;
import com.autovend.devices.observers.BillStorageObserver;
import com.autovend.devices.SelfCheckoutStation;

/**
 * Control software for payment use-cases in self checkout station.
 * 
 * @author Filip Cotra
 */
public class PaymentControllerLogic implements BillSlotObserver, BillValidatorObserver, BillDispenserObserver {
	private double cartTotal;
	private double changeDue;
	private SelfCheckoutStation station;
	private int[] denominations;
	private Map<Integer, BillDispenser> dispensers;
	private int maxDenom;
	
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
	public PaymentControllerLogic(SelfCheckoutStation SCS) {
		station = SCS;
		station.billInput.register(this);
		station.billOutput.register(this);
		station.billValidator.register(this);
		this.denominations = station.billDenominations;
		Arrays.sort(this.denominations);
		this.maxDenom = Arrays.stream(this.denominations).max().getAsInt();
		this.dispensers = station.billDispensers;
		for (int value : this.denominations) {
			this.dispensers.get(value).register(this);
		}
	}
	
	/**
	 * Installation method. Just calls constructor and returns newly 
	 * made instance of the class which is installed on the given
	 * station.
	 */
	public static PaymentControllerLogic installPaymentControll(SelfCheckoutStation SCS) {
		return new PaymentControllerLogic(SCS);
	}

	/**
	 * Updates cartTotal. Takes any double. Would presumably be updated
	 * by ScannerControllerLogic, as that is where the cost is being found.
	 * Updates by item, not all at once, so will have to be called numerous
	 * times.
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
	 * This is broadly tackling use-case scenario 7
	 */
	public void dispenseChange() {
		int nextValue;
		BillDispenser dispenser;
		for(int value : denominations) {
			dispenser = dispensers.get(value);
			if((value < this.getChangeDue())&&(dispenser.size() > 0)) {
				if(value == this.maxDenom) {
					try {
						dispenser.emit();
					}
					catch(Exception e) {
						// May add functionality later
					}
				}
				else {
					nextValue = denominations[value+1];
					if((nextValue < this.getChangeDue())&&(dispensers.get(nextValue).size()>0)) {
						continue;
					}
					else {
						try {
							dispenser.emit();
						}
						catch(Exception e) {
							// May add functionality later
						}
					}
				}
			}
		}
	}
	
	/**
	 * Subtracts value from the cart based on the value of the bill
	 * added. This is tackling scenario 5&6 as it checks if the cart
	 * is paid.
	 */
	public void payBill(int billValue) {
		this.setCartTotal(this.getCartTotal() - billValue);
		// Notify Customer I/O about the remaining amount... Maybe
		//System.out.println("Amount Remaining: " + this.cartTotal);
		if(this.getCartTotal() <= 0) {
			this.setChangeDue(0.0 - this.getCartTotal());
			if(this.getChangeDue() > 0.0) {
				this.dispenseChange();
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

	@Override
	public void reactToValidBillDetectedEvent(BillValidator validator, Currency currency, int value) {
		this.payBill(value);
		
	}

	@Override
	public void reactToInvalidBillDetectedEvent(BillValidator validator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToBillInsertedEvent(BillSlot slot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToBillEjectedEvent(BillSlot slot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToBillRemovedEvent(BillSlot slot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToBillsFullEvent(BillDispenser dispenser) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToBillsEmptyEvent(BillDispenser dispenser) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToBillAddedEvent(BillDispenser dispenser, Bill bill) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToBillRemovedEvent(BillDispenser dispenser, Bill bill) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToBillsLoadedEvent(BillDispenser dispenser, Bill... bills) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToBillsUnloadedEvent(BillDispenser dispenser, Bill... bills) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
