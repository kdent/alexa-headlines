package com.seaglass.alexa;

import static org.junit.Assert.*;

import org.junit.Test;

import com.seaglass.alexa.DialogManager.State;
import com.seaglass.alexa.DialogManager.Symbol;

public class DialogManagerTest {

	@Test
	public void testRequestIntent() {
	    Symbol symbol = DialogManager.getSymbol("Request");
	    assertEquals(Symbol.RequestList, symbol);
	}

	@Test
	public void testRequestList() {
	    DialogContext dialogContext = new DialogContext();
	    dialogContext.setCurrentState(State.INIT);
	    DialogManager.State nextState = DialogManager.getNextState(dialogContext, Symbol.RequestList);
	    assertEquals(DialogManager.State.REQUEST, nextState);
	}

}
