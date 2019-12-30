package saker.std.api.environment.qualifier;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.runtime.environment.EnvironmentProperty;

class SimplePropertyEnvironmentQualifier implements PropertyEnvironmentQualifier, Externalizable {
	private static final long serialVersionUID = 1L;

	private EnvironmentProperty<?> property;
	private Object expectedValue;

	/**
	 * For {@link Externalizable}.
	 */
	public SimplePropertyEnvironmentQualifier() {
	}

	public SimplePropertyEnvironmentQualifier(EnvironmentProperty<?> property, Object expectedValue) {
		this.property = property;
		this.expectedValue = expectedValue;
	}

	@Override
	public void accept(EnvironmentQualifierVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public EnvironmentProperty<?> getEnvironmentProperty() {
		return property;
	}

	@Override
	public Object getExpectedValue() {
		return expectedValue;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(property);
		out.writeObject(expectedValue);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		property = (EnvironmentProperty<?>) in.readObject();
		expectedValue = in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expectedValue == null) ? 0 : expectedValue.hashCode());
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimplePropertyEnvironmentQualifier other = (SimplePropertyEnvironmentQualifier) obj;
		if (expectedValue == null) {
			if (other.expectedValue != null)
				return false;
		} else if (!expectedValue.equals(other.expectedValue))
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + property + " : " + expectedValue + "]";
	}

}
