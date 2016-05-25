package com.seaglass.alexa;

import static org.junit.Assert.*;

import org.junit.Test;

public class DialogManagerTest {

	@Test
	public void testSetState() {
		DialogState ds = new DialogState();
		ds.setCurrentNode("IN_LIST");
		assertEquals(DialogManager.Node.IN_LIST, ds.getCurrentNode());
	}

	@Test
	public void testSetInvalidState() {
		DialogState ds = new DialogState();
		ds.setCurrentNode("INVALID_STATE");
		assertEquals(DialogManager.Node.UNKNOWN, ds.getCurrentNode());
	}
}
