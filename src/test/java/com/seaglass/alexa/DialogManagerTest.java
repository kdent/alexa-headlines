package com.seaglass.alexa;

import static org.junit.Assert.*;

import org.junit.Test;

public class DialogManagerTest {

	@Test
	public void testSetState() {
		DialogContext ds = new DialogContext();
		ds.setCurrentState("IN_LIST");
		assertEquals(DialogManager.State.IN_LIST, ds.getCurrentState());
	}

	@Test
	public void testSetInvalidState() {
		DialogContext ds = new DialogContext();
		ds.setCurrentState("INVALID_STATE");
		assertEquals(DialogManager.State.UNKNOWN, ds.getCurrentState());
	}
}
