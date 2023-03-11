package com.autovend.software.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.autovend.devices.EmptyException;
import com.autovend.devices.OverloadException;
import com.autovend.devices.ReceiptPrinter;
import com.autovend.devices.SimulationException;

public class ReceiptPrinterTestH{
	public char c;
	public ReceiptPrinter printer;
	public MyReceiptPrinterObserver observer;
	/*
	 * When a new ReceiptPrinter is created:
	 * charactersOfInkRemaining, linesOfPaperRemaining, charactersOnCurrentLine are
	 * all initialized at 0. These values are also private.
	 */
	@Before
	public void setup() {
		printer = new ReceiptPrinter();
		observer = new MyReceiptPrinterObserver("");
		printer.enable();
		printer.register(observer);
	}
	
	@After
	public void teardown() {
		printer.deregister(observer);
		observer = null;
		printer = null;
	}
	
	/*
	 * Test Case: Observer reacts to the device being enabled
	 * Expected Result: The receipt printer observer should call the 
	 * reactToEnabledEvent method.
	 */
	@Test
	public void enabledPrint()
	{
		printer.disable();
		printer.enable();
		printer.register(observer);
		assertEquals(printer,observer.device);
	}
	
	/*
	 * Test Case: Observer reacts to the device being enabled
	 * Expected Result: The receipt printer observer should call the 
	 * reactToDisabledEvent method.
	 */
	@Test
	public void disabledPrint()
	{
		printer.disable();
		printer.register(observer);
		assertEquals(printer,observer.device);
	}
	
	/*
	 * Test Case: The user attempts to add a negative amount of paper to the printer.
	 * Expected Result: A SimulationException should be thrown as the user should not 
	 * be able to add a negative amount of paper.
	 */
	@Test
	public void negPaper() throws OverloadException{
		try {
			printer.addPaper(-1);
		}
		catch(SimulationException e)
		{
			return;
		}
		fail("A SimulationException should have been thrown");
	}
	
	/*
	 * Test Case: The user attempts to add an amount of paper that exceeds the maximum amount of 
	 * paper to the printer.
	 * Expected Result: An OverloadException should be thrown as the user should not 
	 * be able to exceed 1024 units of paper.
	 */
	@Test
	public void exceededPaper(){
		try {
			printer.addPaper(2000);
		}
		catch(OverloadException e)
		{
			return;
		}
		fail("A OverloadException should have been thrown");
	}
	
	/*
	 * Test Case: The user attempts to add an amount of paper that does not exceed 
	 * the maximum amount of paper within the printer.
	 * Expected Result: The receipt printer observer should call the 
	 * reactToPaperAddedEvent method, signifying that paper was added without any problems...
	 */
	@Test
	public void addingPaper(){
		try {
			printer.addPaper(2);
		}
		catch(OverloadException e)
		{
			return;
		}
		assertEquals(printer, observer.device);
	}
	
	/*
	 * Test Case: The user attempts to add a negative amount of ink to the printer.
	 * Expected Result: A SimulationException should be thrown as the user should not 
	 * be able to add a negative amount of ink.
	 */
	@Test
	public void negInk() throws OverloadException{
		try {
			printer.addInk(-12);
		}
		catch(SimulationException e)
		{
			return;
		}
		fail("A SimulationException should have been thrown");
	}
	
	/*
	 * Test Case: The user attempts to add an amount of ink that exceeds the maximum amount of 
	 * ink to the printer.
	 * Expected Result: An OverloadException should be thrown as the user should not 
	 * be able to exceed 1048576 units of ink.
	 */
	@Test
	public void exceededInk(){
		try {
			printer.addInk(1048577);
		}
		catch(OverloadException e)
		{
			return;
		}
		fail("An OverloadException should have been thrown");
	}
	
	/*
	 * Test Case: The user attempts to add an amount of ink that does not exceed 
	 * the maximum amount of ink within the printer.
	 * Expected Result: The receipt printer observer should call the 
	 * reactToInkAddedEvent method, signifying that ink was added without any problems...
	 */
	@Test
	public void addingInk(){
		try {
			printer.addInk(10000);
		}
		catch(OverloadException e)
		{
			return;
		}
		assertEquals(printer, observer.device);
	}
	
	/*
	 * Test Case: The user attempts to print a receipt when there is no ink or paper 
	 * in the receipt printer.
	 * Expected Result: An EmptyException should be thrown as the receipt printer does not 
	 * have paper nor ink within the machine.
	 */
	@Test
	public void printNoPaperNoInk(){
		try {
			try {
				c = 'c';
				printer.print(c);
			} 
			catch (EmptyException e) {
				return;
			}
		}
		catch(OverloadException e)
		{
			return;
		}
		fail("An EmptyException should have been thrown.");
	}
	
	/*
	 * Test Case: The user attempts to print a receipt when there is no paper 
	 * in the receipt printer.
	 * Expected Result: An EmptyException should be thrown as the receipt printer does not 
	 * have paper within the machine.
	 */
	@Test
	public void printNoPaper(){
		try {
			try {
				c = 'c';
				printer.addInk(100);  // Adding ink so we can avoid the case when there is no paper and ink.
				printer.print(c);
			} 
			catch (EmptyException e) {
				return;
			}
		}
		catch(OverloadException e)
		{
			return;
		}
		fail("An EmptyException should have been thrown.");
	}
	
	/*
	 * Test Case: The user attempts to print a receipt when there is a single unit of paper 
	 * in the receipt printer.
	 * Expected Result: The receipt printer observer should call the 
	 * reactToOutOfPaperEvent method, signifying that not enough paper was added to allow 
	 * another character to be printed.
	 */
	@Test
	public void printOnePaper(){
		try {
			try {
				c = 'c';
				printer.addPaper(1);
				printer.addInk(100);  // Adding ink so we can avoid the case when there is no paper and ink.
				printer.print(c);
			} 
			catch (EmptyException e) {
				return;
			}
			assertEquals(printer, observer.device);
		}
		catch(OverloadException e)
		{
			return;
		}
	
	}
	
	/*
	 * Test Case: The user attempts to print a receipt when there is no ink 
	 * in the receipt printer.
	 * 
	 * Expected Result: An EmptyException should be thrown as the receipt printer does not 
	 * have ink within the machine.
	 * 
	 */
	@Test
	public void printNoInk(){
		try {
			try {
				c = 'c';
				printer.addPaper(100); // Adding paper so we can avoid the case when there is no paper 
				printer.print(c);
			} 
			catch (EmptyException e) {
				return;
			}
		}
		catch(OverloadException e)
		{
			return;
		}
		fail("An EmptyException should have been thrown.");
	}
	
	/*
	 * Test Case: The user attempts to print to go to the next line of the receipt using ' '.
	 * Will be using a single unit of paper to simulate running out of paper at the end. Since
	 * using a newline should count as using up one line of paper.
	 * 
	 * Expected Result: The receipt printer observer should call the 
	 * reactToOutOfPaperEvent method, signifying that not enough paper was added to allow 
	 * another character to be printed.
	 * 
	 */
	@Test
	public void printNewLineSpace(){
		try {
			try {
				c = ' ';
				printer.addInk(1);	// Adding ink so an EmptyException is not thrown for the wrong reason.
				printer.addPaper(1); // Adding paper so we can avoid the case when there is no paper 
				printer.print(c);	// Use up the first and only line of paper.
			} 
			catch (EmptyException e) {
				return;
			}
		}
		catch(OverloadException e)
		{
			return;
		}
		assertEquals(printer, observer.device);
	}
	
	/*
	 * Test Case: The user attempts to print to go to the next line of the receipt using '\n'.
	 * Will be using a single unit of paper to simulate running out of paper at the end. Since
	 * using a newline should count as using up one line of paper.
	 * 
	 * Expected Result: The receipt printer observer should call the 
	 * reactToOutOfPaperEvent method, signifying that not enough paper was added to allow 
	 * another character to be printed.
	 * 
	 */
	@Test
	public void printNewLineN(){
		try {
			try {
				c = '\n';
				printer.addInk(1);	// Adding ink so an EmptyException is thrown for the wrong reason.
				printer.addPaper(1); // Adding paper so we can avoid the case when there is no paper 
				printer.print(c);	// Use up the first and only line of paper.
			} 
			catch (EmptyException e) {
				return;
			}
		}
		catch(OverloadException e)
		{
			return;
		}
		assertEquals(printer, observer.device);
	}
	
	/*
	 * Test Case: The user attempts to type in 60 or more characters on a single line
	 * 
	 * Expected Result: A new OverloadException should be thrown as there should be only a maximum
	 * of 60 characters per line.
	 * 
	 */
	@Test (expected = OverloadException.class)
	public void printSpillOff(){
		try {
			try {
				c = 'a';
				printer.addInk(100);	// Adding enough ink so an EmptyException isn't thrown.
				printer.addPaper(1);	// Only 1 line of paper should be needed.
				for(int i = 0; i < 61; i++)
				{
					printer.print(c);	// Use up the a line of paper.
				}
			} 
			catch (EmptyException e) {
				return;
			}
		}
		catch(OverloadException e)
		{
			return;
		}
	}
	
	/*
	 * Test Case: The user attempts to remove a receipt that has not been cut
	 * 
	 * Expected Result: Should return null as no receipt has been cut.
	 */
	@Test
	public void removeBlank() {
		assertEquals(null,printer.removeReceipt());
	}
	
	/*
	 * Test Case: The user attempts to remove a receipt that has been cut.
	 * 
	 * Expected Result: A receipt consisting of three characters should be returned for this 
	 * test case ("abc").
	 */
	@Test
	public void cutThenRemove() {
		String expectedString = "abc";
		try {
			printer.addInk(3);
			printer.addPaper(1);
			try {
				printer.print('a');
				printer.print('b');
				printer.print('c');
				printer.cutPaper();
			} catch (EmptyException e) {
				assertEquals(expectedString,printer.removeReceipt());
				return;
			}
		} catch (OverloadException e) {
			return;
		}
	}
}
