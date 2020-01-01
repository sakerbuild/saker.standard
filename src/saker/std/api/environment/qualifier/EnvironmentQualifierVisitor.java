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
 * Visitor interface for possible types of {@link EnvironmentQualifier}.
 * <p>
 * The visitor is used with {@link EnvironmentQualifier#accept(EnvironmentQualifierVisitor)} where the subject
 * environment qualifier will call the appropriate <code>visit</code> method of this interface.
 * <p>
 * All of the declared methods in this interface are <code>default</code> and throw an
 * {@link UnsupportedOperationException} by default. Additional <code>visit</code> methods may be added to this
 * interface with similar default implementations.
 * <p>
 * Clients are recommended to implement this interface.
 */
public interface EnvironmentQualifierVisitor {
	/**
	 * Visits a {@linkplain PropertyEnvironmentQualifier property environment qualifier}.
	 * 
	 * @param qualifier
	 *            The environment qualifier.
	 */
	public default void visit(PropertyEnvironmentQualifier qualifier) {
		throw new UnsupportedOperationException("Unsupported environment qualifier: " + qualifier);
	}

	/**
	 * Visits an {@linkplain AnyEnvironmentQualifier any environment qualifier}.
	 * 
	 * @param qualifier
	 *            The environment qualifier.
	 */
	public default void visit(AnyEnvironmentQualifier qualifier) {
		throw new UnsupportedOperationException("Unsupported environment qualifier: " + qualifier);
	}
}
