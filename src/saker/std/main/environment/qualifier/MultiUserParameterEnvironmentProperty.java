package saker.std.main.environment.qualifier;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.io.SerialUtils;

public class MultiUserParameterEnvironmentProperty implements EnvironmentProperty<Map<String, String>>, Externalizable {
	private static final long serialVersionUID = 1L;

	private Set<String> names;

	/**
	 * For {@link Externalizable}.
	 */
	public MultiUserParameterEnvironmentProperty() {
	}

	public MultiUserParameterEnvironmentProperty(Set<String> names) {
		Objects.requireNonNull(names, "names");
		this.names = names;
	}

	@Override
	public Map<String, String> getCurrentValue(SakerEnvironment environment) throws Exception {
		Map<String, String> userparams = environment.getUserParameters();
		Map<String, String> result = new TreeMap<>();
		for (String pn : names) {
			result.put(pn, userparams.get(pn));
		}
		return result;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, names);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		names = SerialUtils.readExternalImmutableNavigableSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((names == null) ? 0 : names.hashCode());
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
		MultiUserParameterEnvironmentProperty other = (MultiUserParameterEnvironmentProperty) obj;
		if (names == null) {
			if (other.names != null)
				return false;
		} else if (!names.equals(other.names))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + names + "]";
	}

}
