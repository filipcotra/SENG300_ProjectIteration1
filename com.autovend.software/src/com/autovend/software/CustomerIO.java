/** 
 * Filip Cotra - 30086750
 * Khondaker Samin Rashid - 30143490
 * Nishan Soni - 30147280
 * Aaron Tigley - 30159927
 * Zainab Bari - 30154224
 */

package com.autovend.software;

import com.autovend.BarcodedUnit;

/**
 * This interface can be used in testing to simulate customer interactions in certain use cases.
 *
 */
public interface CustomerIO {
		
	/**
	 * Simulates a customer scanning an item.
	 * This interaction is on Step 1 of add item by scanning.
	 * @return The item that the customer will scan. 
	 */
	public BarcodedUnit scanItem();

	/**
	 * Simulates a customer placing their scanned item in the bagging area.
	 * This interaction is on Step 5 of add item by scanning.
	 * @return The item that the customer will place in the bagging area. 
	 * This item can be the same as the one they scanned or some random item.
	 */
	public BarcodedUnit placeScannedItemInBaggingArea();

	/**
	 * Simulates a customer being informed of the updated total due for their
	 * cart based on how much they have paid. This is step 4 in "Pay with Cash."
	 * Returns nothing.
	 * 
	 * @param totalRemaining
	 * 					The total remaining to pay from the customers cart
	 */
	public void showUpdatedTotal(Double totalRemaining);
}
