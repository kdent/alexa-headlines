package com.seaglass.alexa;

public class LogicalForm {

	private PredicateType predicate;

	public LogicalForm(PredicateType predicate) {
		this.predicate = predicate;
	}
	public PredicateType getPredicate() {
		return predicate;
	}

	public void setPredicate(PredicateType predicate) {
		this.predicate = predicate;
	}

	public enum PredicateType {
		GREET,
	};

}
