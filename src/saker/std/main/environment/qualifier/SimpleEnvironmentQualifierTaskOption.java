package saker.std.main.environment.qualifier;

import saker.std.api.environment.qualifier.EnvironmentQualifier;

final class SimpleEnvironmentQualifierTaskOption implements EnvironmentQualifierTaskOption {
	private final EnvironmentQualifier qualifier;

	public SimpleEnvironmentQualifierTaskOption(EnvironmentQualifier qualifier) {
		this.qualifier = qualifier;
	}

	@Override
	public EnvironmentQualifierTaskOption clone() {
		return this;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(qualifier);
	}
}