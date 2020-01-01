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
