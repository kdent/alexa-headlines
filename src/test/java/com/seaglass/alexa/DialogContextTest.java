package com.seaglass.alexa;

import static org.junit.Assert.*;

import org.junit.Test;

public class DialogContextTest {

    @Test
    public void testSetStateAsString() {
        DialogContext dialogContext = new DialogContext();
        dialogContext.setCurrentState(DialogManager.getState("DELIVER_LIST"));
        assertEquals(DialogManager.State.DELIVER_LIST, dialogContext.getCurrentState());
    }

    @Test
    public void testSetInvalidState() {
        DialogContext dialogContext = new DialogContext();
        dialogContext.setCurrentState(DialogManager.getState("INVALID_STATE"));
        assertNull(dialogContext.getCurrentState());
    }
}
