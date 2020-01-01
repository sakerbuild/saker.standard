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

import java.util.Objects;

import saker.build.runtime.environment.EnvironmentProperty;

/**
 * {@link EnvironmentQualifier} that determines the suitability of an environment based on an
 * {@linkplain EnvironmentProperty environment property} and an {@linkplain #getExpectedValue() expected value}.
 * <p>
 * The environment qualifier consists of an environment property and an expected value. The environment property should
 * be tested in the candidate environments, and checked if the associated value equals to the
 * {@linkplain #getExpectedValue() expected value}.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Use {@link #create(EnvironmentProperty, Object)} to construct a new instance.
 */
public interface PropertyEnvironmentQualifier extends EnvironmentQualifier {
	@Override
	public default void accept(EnvironmentQualifierVisitor visitor) throws NullPointerException {
		visitor.visit(this);
	}

	/**
	 * Gets the environment property that is checked in the candidate environments.
	 * 
	 * @return The environment property.
	 */
	public EnvironmentProperty<?> getEnvironmentProperty();

	/**
	 * Gets the expected value of the associated {@linkplain #getEnvironmentProperty() environment property}.
	 * 
	 * @return The expected value. May be <code>null</code>.
	 */
	public Object getExpectedValue();

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

	/**
	 * Creates a new instance with the specified properties.
	 * 
	 * @param envproperty
	 *            The environment property to check on the candidate environments.
	 * @param expectedvalue
	 *            The expected value of the environment property.
	 * @return The created environment qualifier.
	 * @throws NullPointerException
	 *             If the environment property is <code>null</code>.
	 */
	public static PropertyEnvironmentQualifier create(EnvironmentProperty<?> envproperty, Object expectedvalue)
			throws NullPointerException {
		Objects.requireNonNull(envproperty, "environment property");
		return new SimplePropertyEnvironmentQualifier(envproperty, expectedvalue);
	}
}
