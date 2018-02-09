package org.celllife.mobilisr.service.wasp;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class RetryCommandTests {

	@Test
	public void testRetryCommand(){
		RetryCommand command = new RetryCommand() {
			
			@Override
			protected boolean execute(int attempt) throws Exception {
				return attempt == 3;
			}
			
			@Override
			protected boolean handleError(Exception e, int attempt) {
				Assert.fail("Unexpected error: " + e.getMessage());
				return true;
			}
			
			@Override
			protected boolean handleInterrupt(InterruptedException e, int attempt) {
				Assert.fail("Unexpected interrupt: " + e.getMessage());
				return true;
			}
			
			@Override
			protected void retriesExceeded() {
				Assert.fail("Unexpected retries exceeded");
			}
		};
		
		command.setRetryDelay(0);
		command.run();
		
		Assert.assertEquals(3, command.getAttempts());
	}
	
	@Test
	public void testRetryCommand_exception(){
		final AtomicInteger numerrors = new AtomicInteger(0);
		RetryCommand command = new RetryCommand() {
			
			@Override
			protected boolean execute(int attempt) throws Exception {
				if (attempt == 3){
					return true;
				}
				throw new Exception();
			}
			
			@Override
			protected boolean handleError(Exception e, int attempt) {
				numerrors.incrementAndGet();
				return true;
			}
			
			@Override
			protected boolean handleInterrupt(InterruptedException e, int attempt) {
				Assert.fail("Unexpected interrupt: " + e.getMessage());
				return true;
			}
			
			@Override
			protected void retriesExceeded() {
				Assert.fail("Unexpected retries exceeded");
			}
		};
		
		command.setRetryDelay(0);
		command.run();
		
		Assert.assertEquals(2, numerrors.get());
	}
	
	@Test
	public void testRetryCommand_retriesExceeded(){
		final AtomicInteger retriesExceededCalled = new AtomicInteger(0);
		RetryCommand command = new RetryCommand() {
			
			@Override
			protected boolean execute(int attempt) throws Exception {
				return false;
			}
			
			@Override
			protected boolean handleError(Exception e, int attempt) {
				Assert.fail("Unexpected error: " + e.getMessage());
				return true;
			}
			
			@Override
			protected boolean handleInterrupt(InterruptedException e, int attempt) {
				Assert.fail("Unexpected interrupt: " + e.getMessage());
				return true;
			}
			
			@Override
			protected void retriesExceeded() {
				retriesExceededCalled.incrementAndGet();
			}
		};
		
		command.setRetryDelay(0);
		command.run();
		
		Assert.assertEquals(1, retriesExceededCalled.get());
	}
}
