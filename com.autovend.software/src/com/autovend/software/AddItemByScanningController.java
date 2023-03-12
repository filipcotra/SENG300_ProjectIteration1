/** 
 * Filip Cotra - 30086750
 * Khondaker Samin Rashid - 30143490
 * Nishan Soni - 30147280
 * Aaron Tigley - 30159927
 * Zainab Bari - 30154224
 */

package com.autovend.software;

import java.math.BigDecimal;

import com.autovend.Barcode;
import com.autovend.BarcodedUnit;
import com.autovend.SellableUnit;
import com.autovend.devices.AbstractDevice;
import com.autovend.devices.BarcodeScanner;
import com.autovend.devices.OverloadException;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.BarcodeScannerObserver;
import com.autovend.external.ProductDatabases;
import com.autovend.products.BarcodedProduct;

/**
 * Control software for the Add Item By Scanning use case.
 */
public class AddItemByScanningController implements BarcodeScannerObserver {
	
	SelfCheckoutStation station;

	/**
	 * Initialize a controller for the Add Item by Scanning use case. 
	 * Also registers this class as an observer for the station's main scanner.
	 * @param station
	 */
	public AddItemByScanningController(SelfCheckoutStation station) {
		this.station = station;
		this.station.mainScanner.register(this);

	}
	
	/**
	 * Add Item by Scanning use case
	 * @param item
	 */
	public void addItemByScan(SellableUnit item) {
		this.station.mainScanner.scan(item); // Call the scan method of the stations main scanner (Step 1)
		
		// Step 3
		BarcodedUnit barcodeItem = (BarcodedUnit) item;
		Barcode barcode = barcodeItem.getBarcode();
		BarcodedProduct product = ProductDatabases.BARCODED_PRODUCT_DATABASE.get(barcode);
		BigDecimal price = product.getPrice();
		double weight = product.getExpectedWeight();
		
		// Step 4
		double expectedWeight;
		
		try {
			expectedWeight = this.station.baggingArea.getCurrentWeight() + weight;
		} catch (OverloadException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToBarcodeScannedEvent(BarcodeScanner barcodeScanner, Barcode barcode) {
		// Block the self checkout station by disabling all abstract devices. (Step 2)
		this.station.scale.disable();
		this.station.baggingArea.disable();
		this.station.printer.disable();
		this.station.mainScanner.disable();
		this.station.handheldScanner.disable();
		this.station.billInput.disable();
		this.station.billOutput.disable();
		this.station.billStorage.disable();
		this.station.billValidator.disable();
		
	}
}
