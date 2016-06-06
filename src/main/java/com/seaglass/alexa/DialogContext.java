package com.seaglass.alexa;

import java.io.Serializable;

import com.seaglass.alexa.DialogManager.State;

public class DialogContext implements Serializable {

    private static final long serialVersionUID = 1L;
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
    public void setCurrentState(DialogManager.State currentNode) {
        this.currentState = currentNode;
    }
    public void setCurrentState(String nodeName) {
        try {
            currentState = State.valueOf(nodeName);
        } catch (IllegalArgumentException ex) {
            currentState = State.UNKNOWN;
        }
    }
    public void setNextItem(int nextItem) {
        this.nextItem = nextItem;
    }
    public int getNextItem() {
        return nextItem;
    }

    public String toString() {
        return "{requestedSection: " + requestedSection + ", lastStartingItem: " + 
                lastStartingItem + ", nextItem: " + nextItem + ", listLength: " + listLength + ", currentNode: " + currentState + "}";
    }

}
