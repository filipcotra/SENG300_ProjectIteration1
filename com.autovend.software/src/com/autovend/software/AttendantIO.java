/** 
 * Filip Cotra - 30086750
 * Khondaker Samin Rashid - 30143490
 * Nishan Soni - 30147280
 * Aaron Tigley - 30159927
 * Zainab Bari - 30154224
 */

package com.autovend.software;


/**
 * This interface can be used in testing to simulate attendant interactions in certain use cases.
 *
 */
public interface AttendantIO {
	/**
	 * Simulates a attendant approving/rejecting a weight discrepancy
	 * This interaction is apart of the Weight Discrepancy use case
	 * @return true if approved, false if rejected
	 */
	public boolean approveWeightDiscrepancy();
}
