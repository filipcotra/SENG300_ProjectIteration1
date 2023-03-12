package com.autovend.software;

import com.autovend.SellableUnit;

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
	public SellableUnit scanItem();

	/**
	 * Simulates a customer placing their scanned item in the bagging area.
	 * This interaction is on Step 5 of add item by scanning.
	 * @return The item that the customer will place in the bagging area. 
	 * This item can be the same as the one they scanned or some random item.
	 */
	public SellableUnit placeScannedItemInBaggingArea();

}
