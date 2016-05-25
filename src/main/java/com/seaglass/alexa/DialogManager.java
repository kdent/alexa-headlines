package com.seaglass.alexa;

public class DialogManager {

	public static Node getNextState(Node currentNode, Symbol currentSymbol, DialogState dialogState) {
		Node nextState = null;
		if (dialogState.getNextItem() > dialogState.getListLength()) {
			nextState = Node.INIT;
		} else {
			nextState = transitionTable[currentNode.ordinal()][currentSymbol.ordinal()];
		}
		return nextState;
	}

	public static Symbol getSymbol(String intent) {
		Symbol symbol = null;
		if (intent.equals("StartList")) {
			symbol = Symbol.RequestList;
		} else if (intent.equals("AMAZON.YesIntent")) {
			symbol = Symbol.Yes;
		} else if (intent.equals("AMAZON.NoIntent")) {
			symbol = Symbol.No;
		} else if (intent.equals("AMAZON.HelpIntent")) {
			symbol = Symbol.Help;
		}
		return symbol;
	}

	public enum Node {
		INIT,
		LAUNCH,
		IN_LIST,
		HELP,
		UNKNOWN
	}

	public enum Symbol {
		Launch,
		RequestList,
		Yes,
		No,
		Help
	}

	private static Node[][] transitionTable = {
	    {Node.LAUNCH, Node.IN_LIST, Node.INIT, Node.INIT, Node.HELP},		// INIT
	    {Node.LAUNCH, Node.IN_LIST, Node.IN_LIST, Node.INIT, Node.HELP},	// LAUNCH
	    {Node.LAUNCH, Node.IN_LIST, Node.IN_LIST, Node.INIT, Node.HELP},	// IN_LIST
	    {Node.INIT, Node.INIT, Node.INIT, Node.INIT, Node.INIT},			// HELP
	    {Node.LAUNCH, Node.IN_LIST, Node.INIT, Node.INIT, Node.HELP}		// UNKNOWN
	};

}
