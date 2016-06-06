package com.seaglass.alexa;

import static org.junit.Assert.*;

import org.junit.Test;

public class DialogManagerTest {

	@Test
	public void testSetState() {
		DialogContext dialogContext = new DialogContext();
		dialogContext.setCurrentState("IN_LIST");
		assertEquals(DialogManager.State.IN_LIST, dialogContext.getCurrentState());
	}

	@Test
	public void testRequestIntent() {
	}

	@Test
	public void testSetInvalidState() {
		DialogContext ds = new DialogContext();
		ds.setCurrentState("INVALID_STATE");
		assertEquals(DialogManager.State.UNKNOWN, ds.getCurrentState());
	}
}
