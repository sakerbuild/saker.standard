package saker.std.api.environment.qualifier;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

final class SimpleAnyEnvironmentQualifier implements AnyEnvironmentQualifier, Externalizable {
	private static final long serialVersionUID = 1L;

	private static final int HASH_CODE = AnyEnvironmentQualifier.class.getName().hashCode();

	public static final SimpleAnyEnvironmentQualifier INSTANCE = new SimpleAnyEnvironmentQualifier();

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleAnyEnvironmentQualifier() {
	}

	@Override
	public void accept(EnvironmentQualifierVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	}

	@Override
	public int hashCode() {
		return HASH_CODE;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof AnyEnvironmentQualifier;
	}
}
