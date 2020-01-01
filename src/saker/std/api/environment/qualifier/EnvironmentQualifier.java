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
 * Interface specifying the suitability of a build environment for a given operation.
 * <p>
 * {@link EnvironmentQualifier} is a superinterface for all environment qualifier types that can be represented under
 * this interface.
 * <p>
 * An environment qualifier specifies if a build environment should be used for a given operation in the context it is
 * being used in.
 * <p>
 * When using environment qualifiers, you shouldn't downcast them or use <code>instanceof</code>, but use the
 * {@link #accept(EnvironmentQualifierVisitor)} method with a custom {@link EnvironmentQualifierVisitor} implementation.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * you can create a new {@link EnvironmentQualifier} by using the <code>create</code> methods of the actual
 * subinterfaces.
 * 
 * @see PropertyEnvironmentQualifier
 * @see AnyEnvironmentQualifier
 */
public interface EnvironmentQualifier {
	/**
	 * Accepts a visitor and calls an appropriate <code>visit</code> method on it.
	 * 
	 * @param visitor
	 *            The visitor.
	 * @throws NullPointerException
	 *             If the visitor is <code>null</code>.
	 */
	public void accept(EnvironmentQualifierVisitor visitor) throws NullPointerException;

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
