package com.seaglass.alexa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DialogContextTest {

    @Test
    public void testSetStateAsString() {
        DialogContext dialogContext = new DialogContext();
        dialogContext.setCurrentState("IN_LIST");
        assertEquals(DialogManager.State.DELIVER_LIST, dialogContext.getCurrentState());
    }

    @Test
    public void testSetInvalidState() {
        DialogContext dialogContext = new DialogContext();
        dialogContext.setCurrentState("INVALID_STATE");
        assertEquals(DialogManager.State.END, dialogContext.getCurrentState());
    }
}
