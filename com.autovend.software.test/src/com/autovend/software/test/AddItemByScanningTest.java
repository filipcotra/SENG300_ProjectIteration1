/** 
 * Filip Cotra - 30086750
 * Khondaker Samin Rashid - 30143490
 * Nishan Soni - 30147280
 * Aaron Tigley - 30159927
 * Zainab Bari - 30154224
 */

package com.autovend.software.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.autovend.Barcode;
import com.autovend.BarcodedUnit;
import com.autovend.Numeral;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.external.ProductDatabases;
import com.autovend.products.BarcodedProduct;
import com.autovend.software.AddItemByScanningController;
import com.autovend.software.AttendantIO;
import com.autovend.software.CustomerIO;

/**
 * Test cases for AddItemByScanning
 */
public class AddItemByScanningTest {
	
	AddItemByScanningController addItemByScanningController;
	SelfCheckoutStation selfCheckoutStation;
	
	/**
	 * An example test case that runs a normal execution of the use case
	 * You can delete this method later
	 */
	@Test
	public void testAddItemByScanningNormal() {
		
		Barcode barcode = new Barcode(Numeral.one, Numeral.two);
		BarcodedUnit scannedItem = new BarcodedUnit(barcode, 50);
		BarcodedUnit placedItem = scannedItem;
		selfCheckoutStation = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20}, 
				new BigDecimal[] {new BigDecimal(1),new BigDecimal(2)}, 10000, 5);
		BarcodedProduct product = new BarcodedProduct(barcode, "example", new BigDecimal(10), 
				scannedItem.getWeight());
		
		ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode, product);
		
		class MyCustomerIO implements CustomerIO {
			
			
			@Override
			public BarcodedUnit scanItem() {
				return scannedItem;
			}

			@Override
			public BarcodedUnit placeScannedItemInBaggingArea() {
				return placedItem;
			}

			@Override
			public void showUpdatedTotal(Double totalRemaining) {
				// Implement some basic function
			}

			@Override
			public void sessionComplete() {
				// TODO Auto-generated method stub
				
			}


			
		}
		
		class MyAttendantIO implements AttendantIO {

			@Override
			public boolean approveWeightDiscrepancy() {
				return true;
			}

			@Override
			public void changeRemainsNoDenom(double changeLeft) {
				// Implement some basic function		
			}

			@Override
			public void giveDuplicateReceipt() {
				// TODO Auto-generated method stub
				
			}

		}
		
		addItemByScanningController = new AddItemByScanningController(selfCheckoutStation, new MyCustomerIO(), 
				new MyAttendantIO());
		
		addItemByScanningController.addItemByScanning();
	}
	
	

}
