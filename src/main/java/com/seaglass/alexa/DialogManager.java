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
        INIT,       // starting state
        LAUNCH,     // User has launched the skill
        REQUEST,    // User has requested headlines without giving a section
        IN_LIST,    // User has requested headlines with a section or has asked to continue a list that has been started
        HELP,       // User has asked for help with the skill
        UNKNOWN     // An error condition
    }

    public enum Symbol {
        Launch,
        RequestList,
        Yes,
        No,
        Help
    }

    private static State[][] transitionTable = {
        // Launch      RequestList    Yes            No          Help
        {State.LAUNCH, State.REQUEST, State.INIT,    State.INIT, State.HELP},    // INIT
        {State.LAUNCH, State.REQUEST, State.IN_LIST, State.INIT, State.HELP},    // LAUNCH
        {State.LAUNCH, State.REQUEST, State.REQUEST, State.INIT, State.HELP},    // REQUEST
        {State.LAUNCH, State.REQUEST, State.IN_LIST, State.INIT, State.HELP},    // IN_LIST
        {State.INIT,   State.REQUEST, State.INIT,    State.INIT, State.INIT},    // HELP
        {State.LAUNCH, State.REQUEST, State.INIT,    State.INIT, State.HELP}     // UNKNOWN
    };

}
