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
 * Visitor interface for the possible types of {@link FileLocation}.
 * <p>
 * The visitor is used with {@link FileLocation#accept(FileLocationVisitor)} where the subject file location will call
 * the appropriate <code>visit</code> method of this interface.
 * <p>
 * All of the declared methods in this interface are <code>default</code> and throw an
 * {@link UnsupportedOperationException} by default. Additional <code>visit</code> methods may be added to this
 * interface with similar default implementations.
 * <p>
 * Clients are recommended to implement this interface.
 */
public interface FileLocationVisitor {
	/**
	 * Visits a {@linkplain LocalFileLocation local file location}.
	 * 
	 * @param loc
	 *            The file location.
	 */
	public default void visit(LocalFileLocation loc) {
		throw new UnsupportedOperationException("Unsupported file location: " + loc);
	}

	/**
	 * Visits a {@linkplain ExecutionFileLocation execution file location}.
	 * 
	 * @param loc
	 *            The file location.
	 */
	public default void visit(ExecutionFileLocation loc) {
		throw new UnsupportedOperationException("Unsupported file location: " + loc);
	}
}
