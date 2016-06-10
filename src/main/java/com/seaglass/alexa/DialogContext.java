package com.seaglass.alexa;

import java.io.Serializable;

public class DialogContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /* Constants to refer to context names for session attributes. */
    public static final String REQUESTED_SECTION = "requestedSection";
    public static final String LAST_STARTING_ITEM = "lastStartingItem";
    public static final String NEXT_ITEM = "nextItem";
    public static final String LIST_LENGTH = "listLength";
    public static final String CURRENT_STATE = "currentState";

    private String requestedSection;
    private int lastStartingItem;
    private int nextItem;
    private int listLength;
    private DialogManager.State currentState = DialogManager.State.INIT;

    public String getRequestedSection() {
        return requestedSection;
    }
    public void setRequestedSection(String requestedSection) {
        this.requestedSection = requestedSection;
    }
    public int getLastStartingItem() {
        return lastStartingItem;
    }
    public void setLastStartingItem(int nextItem) {
        this.lastStartingItem = nextItem;
    }
    public int getListLength() {
        return listLength;
    }
    public void setListLength(int listLength) {
        this.listLength = listLength;
    }
    public DialogManager.State getCurrentState() {
        return currentState;
    }
    public void setCurrentState(DialogManager.State currentState) {
        this.currentState = currentState;
    }
    public void setNextItem(int nextItem) {
        this.nextItem = nextItem;
    }
    public int getNextItem() {
        return nextItem;
    }

    public String toString() {
        return "{requestedSection: " + requestedSection + ", lastStartingItem: " + 
                lastStartingItem + ", nextItem: " + nextItem + ", listLength: " + listLength + ", currentState: " + currentState + "}";
    }
}
