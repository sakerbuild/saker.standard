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

/**
 * {@link EnvironmentQualifier} that specifies that any build environment can be used for a given operation.
 * <p>
 * The interface contains no properties.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Use {@link #create()} to get an instance.
 */
public interface AnyEnvironmentQualifier extends EnvironmentQualifier {
	@Override
	public default void accept(EnvironmentQualifierVisitor visitor) throws NullPointerException {
		visitor.visit(this);
	}

	/**
	 * Gets the hash code for the environment qualifier.
	 * <p>
	 * Defined as:
	 * 
	 * <pre>
	 * AnyEnvironmentQualifier.class.getName().hashCode()
	 * </pre>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode();

	/**
	 * Checks if the argument object is an instance of {@link AnyEnvironmentQualifier}.
	 * <p>
	 * The equality check should return <code>true</code> if the argument is an instance of
	 * {@link AnyEnvironmentQualifier}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj);

	/**
	 * Gets an {@link AnyEnvironmentQualifier} instance.
	 * 
	 * @return An instance.
	 */
	public static AnyEnvironmentQualifier create() {
		return SimpleAnyEnvironmentQualifier.INSTANCE;
	}
}
