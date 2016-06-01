package com.seaglass.alexa;

import java.io.Serializable;

import com.seaglass.alexa.DialogManager.Node;

public class DialogStateObj implements Serializable {

	private static final long serialVersionUID = 1L;
	private String requestedSection;
	private int lastStartingItem;
	private int nextItem;
	private int listLength;
	private DialogManager.Node currentNode = DialogManager.Node.INIT;

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
	public DialogManager.Node getCurrentNode() {
		return currentNode;
	}
	public void setCurrentNode(DialogManager.Node currentNode) {
		this.currentNode = currentNode;
	}
	public void setCurrentNode(String nodeName) {
		try {
			currentNode = Node.valueOf(nodeName);
		} catch (IllegalArgumentException ex) {
			currentNode = Node.UNKNOWN;
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
				lastStartingItem + ", nextItem: " + nextItem + ", listLength: " + listLength + ", currentNode: " + currentNode + "}";
	}

}
