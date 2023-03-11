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
			}
		}
		catch(OverloadException e)
		{
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
	 * Actual Result: Due to the structure of the code, this error will never be thrown in such a case.
	 * What's happening is that the inputed character will still be appended to the StringBuilder.
	 * Solution: Change the else if statement on line 66 of ReceiptPrinter.java to a normal if statement.
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
	 * Things noticed: Due to the structure of the code, the condition on line 66 of ReceiptPrinter.java does not
	 * account for ' '. the only reason the test passed is because the linesOfPaperRemaining decrements by 1 on line 61.
	 * What's happening is that the else if statement on line 66 has a condition that c != ' ', meaning that
	 * the user typing in ' ' will never lead to a new line as intended.
	 * Solution: Change the conditions in the else if statement on line 66 of ReceiptPrinter.java
	 * such that c == ' ' || Character.isWhitespace(c).
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
	 * Things noticed: Since the it is an "else if" statement on line 66, and not an "if" statement. 
	 * This will never be reached since the previous if and else if statement rely on the amount of
	 * paper within the machine. The condition on line 66 of ReceiptPrinter.java will never be reached
	 * and the only reason the test passed is because the linesOfPaperRemaining decrements by 1 on line 61.
	 * 
	 * Solution: Change the conditions in the else if statement on line 66 of ReceiptPrinter.java
	 * such that c == ' ' || Character.isWhitespace(c) and change the else if statement into an if statement.
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
	 * Actual Result: An OverloadException is not thrown. This is because the statement on line 66 is an 
	 * "else if" statement and not an "if" statement. This means that this condition will never 
	 * be reached since the first two statements, are dependent on the paper in the machine. 
	 * One of these two conditions will inevitably be met, skipping the following conditions
	 * that are dependent on the amount of ink and the characters on the current line of paper.
	 * 
	 * Solution: Change the else if statement on line 66 to an if statement. Additionally, change the condition on line 68
	 * from "charactersOnCurrentLine == CHARACTERS_PER_LINE" to "charactersOnCurrentLine > CHARACTERS_PER_LINE" as the current
	 * condition only ensures that a maximum of 59 characters are on the current line of paper, and not the desired value of 60.
	 * One final thing to note is that if there is paper in the machine, a new line of paper will be used even if the user does not
	 * want to create a new line of paper. Therefore, "--linesOfPaperRemaining" should be removed from line 61, and placed below the
	 * statement on line 66.
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
	 * 
	 * Actual Result: An EmptyException was thrown. Because a the remaining lines of paper decrements regardless if the user
	 * types in " " or "\n", the single line of paper is consumed. 
	 * 
	 * Solutions: Once again, we will suggest some edits as well as some new ones.
	 * These should be the final list of edits we would recommend:
	 * 1. Remove "--linesOfPaperRemaining" from line 61 and place it below the
	 * 		if statement on line 66.
	 * 2. Change the "else if" statement on line 66 into an "if" statement
	 * 3. Change the conditions on line 66 to "if(c == ' ' || c == "\n")".
	 * 4. Change the condition on line 68 to "else if(charactersOnCurrentLine > CHARACTERS_PER_LINE)"
	 * 5. Remove "charactersOnCurrentLine++;" from line 77 and move it under the else if statement on line 70
	 * 		(Below the nested if statement on line 71 as well).
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
