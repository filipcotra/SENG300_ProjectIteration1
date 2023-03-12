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
import com.autovend.SellableUnit;
import com.autovend.devices.AbstractDevice;
import com.autovend.devices.BarcodeScanner;
import com.autovend.devices.ElectronicScale;
import com.autovend.devices.OverloadException;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.BarcodeScannerObserver;
import com.autovend.devices.observers.ElectronicScaleObserver;
import com.autovend.external.ProductDatabases;
import com.autovend.products.BarcodedProduct;

/**
 * Control software for the Add Item By Scanning use case.
 */
public class AddItemByScanningController implements BarcodeScannerObserver, ElectronicScaleObserver {
	
	SelfCheckoutStation station;
	double expectedWeight;
	CustomerIO customerIO;

	/**
	 * Initialize a controller for the Add Item by Scanning use case. 
	 * Also registers this class as an observer for the station's main scanner.
	 * @param station The self checkout station
	 * @param customerIO The customer interacting with the Add Item by Scanning use case.
	 */
	public AddItemByScanningController(SelfCheckoutStation station, CustomerIO customerIO) {
		this.station = station;
		this.customerIO = customerIO;
		this.station.mainScanner.register(this);
		this.station.baggingArea.register(this);
	}
	
	/**
	 * Blocks the system by disabling all devices besides the bagging area.
	 */
	private void blockSystem() {
		this.station.printer.disable();
		this.station.mainScanner.disable();
		this.station.handheldScanner.disable();
		this.station.billInput.disable();
		this.station.billOutput.disable();
		this.station.billStorage.disable();
		this.station.billValidator.disable();
	}
	
	/**
	 * Unblocks the system by enabling all devices besides the bagging area.
	 */
	private void unblockSystem() {
		this.station.printer.enable();
		this.station.mainScanner.enable();
		this.station.handheldScanner.enable();
		this.station.billInput.enable();
		this.station.billOutput.enable();
		this.station.billStorage.enable();
		this.station.billValidator.enable();
	}
	
	/**
	 * Scan the item that CustomerIO chooses to scan (Step 1)
	 * Requires customer input: scanItem
	 */
	public void addItemByScanning() {
		// Call the scan method of the stations main scanner (Step 1)
		this.station.mainScanner.scan(customerIO.scanItem());
		// Go to reactToBarcodeScannedEvent
	}
	
	/**
	 * Notify the customer to place the scanned item in the bagging area (Step 5)
	 * Requires customer input: placeScannedItemInBaggingArea
	 */
	public SellableUnit notifyCustomerIO() {
		return customerIO.placeScannedItemInBaggingArea();
	}

	@Override
	public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Occurs after mainScanner scans an item
	 */
	@Override
	public void reactToBarcodeScannedEvent(BarcodeScanner barcodeScanner, Barcode barcode) {
		// Block the self checkout station by disabling all abstract devices. (Step 2)
		this.blockSystem();
		
		// Get product details from the barcode (Step 3)
		BarcodedProduct product = ProductDatabases.BARCODED_PRODUCT_DATABASE.get(barcode);
		BigDecimal price = product.getPrice();
		double weight = product.getExpectedWeight();
		
		// Calculating the expected weight of the bagging area (Step 4)
		try {
			this.expectedWeight = this.station.baggingArea.getCurrentWeight() + weight;
		} catch (OverloadException e) {
			e.printStackTrace();
		}
		
		// Notify Customer I/O to place scanned item in bagging area (Step 5)
		// And notify weight change (Step 6)
		this.station.baggingArea.add(this.notifyCustomerIO());
		// Go to reactToWeightChangedEvent
		
	}

	@Override
	public void reactToWeightChangedEvent(ElectronicScale scale, double weightInGrams) {
		// Check for weight discrepancy (Exception 1)
		if (weightInGrams!= this.expectedWeight) {
			// WEIGHT DISCREPANCY ERROR
		} else {
			this.unblockSystem();
		}
		
	}

	@Override
	public void reactToOverloadEvent(ElectronicScale scale) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToOutOfOverloadEvent(ElectronicScale scale) {
		// TODO Auto-generated method stub
		
	}
}
