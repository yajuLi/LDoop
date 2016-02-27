package deepdoop.datalog;

import java.util.Arrays;

public class RefModeElement extends PredicateElement {

	public RefModeElement(String name, VariableExpr entity, IExpr primitive) {
		super(name, Arrays.asList(entity, primitive));
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(" : ");
		for (IExpr e : _exprs) joiner.add(e.toString());
		return _name + "(" + joiner + ")";
	}
}
