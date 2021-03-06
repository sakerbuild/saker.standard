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
package saker.std.api.file.location;

/**
 * Represents a location of a file.
 * <p>
 * {@link FileLocation} is a superinterface for all file location types that can be represented under this interface.
 * <p>
 * A file location specifies the location of a file. The manners of how the file should be accessed depends on the type
 * of the file location and is the responsibility of the consumer.
 * <p>
 * When using file locations, you shouldn't downcast them or use <code>instanceof</code>, but use the
 * {@link #accept(FileLocationVisitor)} method with a custom {@link FileLocationVisitor} implementation.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * You can create a new {@link FileLocation} by using the <code>create</code> methods of the actual subinterfaces.
 * 
 * @see ExecutionFileLocation
 * @see LocalFileLocation
 * @see FileCollection
 */
public interface FileLocation {
	/**
	 * Accepts a visitor and calls an appropriate <code>visit</code> method on it.
	 * 
	 * @param visitor
	 *            The visitor.
	 * @throws NullPointerException
	 *             If the visitor is <code>null</code>.
	 */
	public void accept(FileLocationVisitor visitor) throws NullPointerException;

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
