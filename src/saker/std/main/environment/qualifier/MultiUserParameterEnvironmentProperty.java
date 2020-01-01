/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
