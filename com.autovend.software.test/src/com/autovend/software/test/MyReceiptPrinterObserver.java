package com.autovend.software.test;

import com.autovend.devices.AbstractDevice;
import com.autovend.devices.ReceiptPrinter;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.ReceiptPrinterObserver;

public class MyReceiptPrinterObserver implements ReceiptPrinterObserver{

	public AbstractDevice<? extends AbstractDeviceObserver> device = null;
	public String name;
	public MyReceiptPrinterObserver(String name) {
		this.name = name;
	}
	@Override
	public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		this.device = device;
	}

	@Override
	public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {

		this.device = device;
		System.out.println("This device is disabled");
	}

	@Override
	public void reactToOutOfPaperEvent(ReceiptPrinter printer) {
	
		this.device = printer;
		System.out.println("Receipt printer is currently out of paper.");
		
	}

	@Override
	public void reactToOutOfInkEvent(ReceiptPrinter printer) {
		
		this.device = printer;
		System.out.println("Receipt printer is currently out of ink.");
		
	}

	@Override
	public void reactToPaperAddedEvent(ReceiptPrinter printer) {
		
		this.device = printer;
		System.out.println("Paper has been added to the receipt printer.");
		
	}

	@Override
	public void reactToInkAddedEvent(ReceiptPrinter printer) {
		
		this.device = printer;
		System.out.println("Ink has been added to the receipt printer.");
		
	}

}
