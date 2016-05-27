package com.seaglass.alexa;

import static org.junit.Assert.*;

import org.junit.Test;

public class DialogManagerTest {

	@Test
	public void testSetState() {
		DialogStateObj ds = new DialogStateObj();
		ds.setCurrentNode("IN_LIST");
		assertEquals(DialogManager.Node.IN_LIST, ds.getCurrentNode());
	}

	@Test
	public void testSetInvalidState() {
		DialogStateObj ds = new DialogStateObj();
		ds.setCurrentNode("INVALID_STATE");
		assertEquals(DialogManager.Node.UNKNOWN, ds.getCurrentNode());
	}
}
