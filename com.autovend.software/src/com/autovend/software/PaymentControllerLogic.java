/** 
 * Filip Cotra - 30086750
 *  Khondaker Samin Rashid - 30143490
 *  Nishan Soni - 30147289
 *  Aaron Tigley - 30159927
 *  Zainab Bari - 30154224
 */

package com.autovend.software;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.autovend.devices.EmptyException;
import com.autovend.devices.ReceiptPrinter;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.BillValidatorObserver;
import com.autovend.devices.observers.BillDispenserObserver;
import com.autovend.devices.observers.BillSlotObserver;
import com.autovend.devices.observers.BillStorageObserver;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.SimulationException;

/**
 * Control software for payment use-cases in self checkout station.
 * 
 * @author Filip Cotra
 */
public class PaymentControllerLogic implements BillValidatorObserver, BillDispenserObserver, BillSlotObserver {
	private BigDecimal cartTotal;
	private BigDecimal changeDue;
	private SelfCheckoutStation station;
	private int[] denominations;
	private Map<Integer, BillDispenser> dispensers;
	private BigDecimal maxDenom;
	private BigDecimal minDenom;
	private ReceiptPrinter printer;
	private CustomerIO myCustomer;
	private AttendantIO myAttendant;
	private PrintReceipt printerLogic;
	private ArrayList<String> itemNameList = new ArrayList<String>();
	private ArrayList<String> itemCostList = new ArrayList<String>();
	private BigDecimal amountPaid;
	private BigDecimal totalChange;
	private BillSlot output;
	
	/**
	 * Constructor. Takes a Self-Checkout Station  and initializes
	 * fields while also registering the logic as an observer of 
	 * BillSlot, BillValidator, and BillDispenser objects of the 
	 * station. Sorts denominations in ascending order to facilitate
	 * dispensing change later.
	 * 
	 * @param SCS
	 * 		Self-Checkout Station on which to install the logic
	 * @param customer
	 * 		CustomerIO interface to represent customer session
	 * @param attendant
	 * 		AttendantIO interface that is monitoring the machine
	 */
	public PaymentControllerLogic(SelfCheckoutStation SCS, CustomerIO customer, AttendantIO attendant, PrintReceipt printerLogic) {
		this.station = SCS;
		this.station.billValidator.register(this);
		this.denominations = station.billDenominations;
		Arrays.sort(this.denominations);
		this.maxDenom = BigDecimal.valueOf(Arrays.stream(this.denominations).max().getAsInt());
		this.minDenom = BigDecimal.valueOf(Arrays.stream(this.denominations).min().getAsInt());
		this.dispensers = station.billDispensers;
		this.printer = station.printer;
		for (int value : this.denominations) {
			this.dispensers.get(value).register(this);
		}
		this.myCustomer = customer;
		this.myAttendant = attendant;
		this.printerLogic = printerLogic;
		this.amountPaid = BigDecimal.valueOf(0.0);
		this.output = station.billOutput;
		this.output.register(this);
		this.cartTotal = BigDecimal.valueOf(0.0);
		this.totalChange = BigDecimal.valueOf(0.0);
		this.changeDue = BigDecimal.valueOf(0.0);
	}

	/**
	 * Updates the amount paid by the customer.
	 * 
	 * @param billValue
	 * 			The value of the bill inserted
	 */
	public void updateAmountPaid(BigDecimal value) {
		this.amountPaid = this.amountPaid.add(value);
	}
	
	/**
	 * Basic getter to return amount paid field.
	 */
	public String getAmountPaid() {
		return "" + this.amountPaid;
	}

	/**
	 * Sets the total amount of change due to the customer.
	 * This value will not be updated, and is related to 
	 * printing the receipt.
	 * 
	 * @param amount
	 * 			Amount of change to be due
	 */
	public void setTotalChange(BigDecimal amount) {
		this.totalChange = amount;
	}
	
	/**
	 * Basic getter to return total change.
	 */
	public String getTotalChange() {
		return "" + this.totalChange;
	}
	
	/**
	 * Updates cartTotal. Takes any double. Should be called by AddItemByScanningController
	 * to update, as that is where the cost of each item is being determined.
	 * Updates by item, not all at once, so will have to be called numerous
	 * times.
	 * 
	 * @param price
	 * 		amount to be added to running total
	 */
	public void updateCartTotal(BigDecimal price) {
		this.cartTotal = this.cartTotal.add(price);
	}

	/**
	 * Sets cart total. Takes a double. Only to be called within this
	 * class.
	 * 
	 * @param total
	 * 		amount to set the total cost of the cart
	 */
	
	public void setCartTotal(BigDecimal total) {
		this.cartTotal = total;
	}

	/**
	 * Builds the lists for the item names and costs in the cart. Should
	 * be called by AddItemByScanningController as needed to update
	 * this info.
	 * 
	 * @param itemName
	 * 			The name of the item added
	 * @param itemCost
	 * 			The cost of the item added
	 */
	public void updateItemCostList(String itemName, String itemCost) {
		this.itemNameList.add(itemName);
		this.itemCostList.add(itemCost);
	}
	
	/**
	 * Getter for cartTotal. Just returns value.
	 */
	public BigDecimal getCartTotal() {
		return this.cartTotal;
	}
	
	/**
	 * Setter for changeDue. Takes any double. Is only to be called within this
	 * class.
	 * 
	 * @param change
	 * 		amount of change that is due
	 */
	public void setChangeDue(BigDecimal change) {
		this.changeDue = change;
	}
	
	/**
	 * Getter for cartTotal. Returns double. No need to call from any other class.
	 */
	public BigDecimal getChangeDue() {
		return this.changeDue;
	}

	/**
	 * Suspends machine
	 */
	private void suspendMachine() {
		this.station.baggingArea.disable();
		for(int denom : this.denominations) {
			this.station.billDispensers.get(denom).disable();
		}
		this.station.billInput.disable();
		this.station.billOutput.disable();
		this.station.billStorage.disable();
		this.station.billValidator.disable();
		this.station.handheldScanner.disable();
		this.station.mainScanner.disable();
		this.station.printer.disable();
		this.station.scale.disable();
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
		if(this.getChangeDue().compareTo(BigDecimal.valueOf(0.0)) == 0) {
			throw new SimulationException(new Exception("This should never happen"));
		}
		/** If the changeDue is less than the lowest denom, call attendant automatically */
		else if(this.getChangeDue().compareTo(this.minDenom) == -1) {
			this.myAttendant.changeRemainsNoDenom(this.getChangeDue());
			/** No need to suspend machine, nothing is empty its just a lack of denoms */
		}
		BillDispenser dispenser;
		/** Go through denominations backwards, largest to smallest */
		for(int index = this.denominations.length-1 ; index >= 0 ; index--) {
			dispenser = this.dispensers.get(this.denominations[index]);
			/** If the value of the bill is less than or equal to the change and change is payable */
			if(BigDecimal.valueOf(this.denominations[index]).compareTo(this.getChangeDue()) <= 0) {
				try {
					dispenser.emit();
					index++;
				}
				/** If empty and not the smallest denom, move on. If the smallest denom, inform attendant */
				catch(EmptyException e) {
					if(BigDecimal.valueOf(this.denominations[index]).compareTo(this.minDenom) == 0) {
						/** In this case change will be larger than smallest denom but unpayable */
						this.myAttendant.changeRemainsNoDenom(this.getChangeDue());
						this.suspendMachine();
						break;
					}
					else {
						continue;
					}
				}
				catch(Exception e) {
					e.printStackTrace();
					// Unspecified functionality
				}
			}
		}
	}
	
	/**
	 * Subtracts value from the cart based on the value of the bill
	 * added. (Step 2, Step 3, Step 5, Step 6)
	 */
	public void payBill(BigDecimal billValue) {
		this.updateAmountPaid(billValue);
		this.setCartTotal(this.getCartTotal().subtract(billValue));
		myCustomer.showUpdatedTotal(this.getCartTotal());
		/** If the customer has paid their cart, check for change */
		if(this.getCartTotal().compareTo(BigDecimal.valueOf(0.0)) <= 0) {
			this.setChangeDue(BigDecimal.valueOf(0.0).subtract(this.getCartTotal()));
			this.setTotalChange(this.getChangeDue());
			this.setCartTotal(BigDecimal.valueOf(0.0)); //Set cart total after change has been calculated.
			if(this.getChangeDue().compareTo(BigDecimal.valueOf(0.0)) > 0) {
				this.dispenseChange();
			}
			else {
				this.printerLogic.print(this.itemNameList,this.itemCostList,this.getTotalChange(),this.getAmountPaid());
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
		this.payBill(BigDecimal.valueOf(value));
	}

	@Override
	public void reactToInvalidBillDetectedEvent(BillValidator validator) {
		// Ignoring in this iteration
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
		this.setChangeDue(this.getChangeDue().subtract(BigDecimal.valueOf(bill.getValue())));
		if(this.getChangeDue().compareTo(BigDecimal.valueOf(0.0)) > 0) {
			this.dispenseChange();
		}
		else {
			this.printerLogic.print(this.itemNameList,this.itemCostList,this.getTotalChange(),this.getAmountPaid());
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

	@Override
	public void reactToBillInsertedEvent(BillSlot slot) {
		// TODO Auto-generated method stub	
	}

	/**
	 * This is informing the customer that they have to remove the
	 * bill dangling from the slot.
	 */
	@Override
	public void reactToBillEjectedEvent(BillSlot slot) {
		this.myCustomer.removeBill(slot);
	}

	@Override
	public void reactToBillRemovedEvent(BillSlot slot) {
		// TODO Auto-generated method stub
	}
}
