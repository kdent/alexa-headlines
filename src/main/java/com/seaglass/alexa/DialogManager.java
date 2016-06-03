package com.seaglass.alexa;

public class DialogManager {

	public static State getNextState(State currentNode, Symbol currentSymbol, DialogContext dialogState) {
		State nextState = null;
		if (dialogState.getLastStartingItem() > dialogState.getListLength()) {
			nextState = State.INIT;
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

	public enum State {
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

	private static State[][] transitionTable = {
	    {State.LAUNCH, State.IN_LIST, State.INIT,    State.INIT, State.HELP},	// INIT
	    {State.LAUNCH, State.IN_LIST, State.IN_LIST, State.INIT, State.HELP},	// LAUNCH
	    {State.LAUNCH, State.IN_LIST, State.IN_LIST, State.INIT, State.HELP},	// IN_LIST
	    {State.INIT,   State.INIT,    State.INIT,    State.INIT, State.INIT},	// HELP
	    {State.LAUNCH, State.IN_LIST, State.INIT,    State.INIT, State.HELP}		// UNKNOWN
	};

}
