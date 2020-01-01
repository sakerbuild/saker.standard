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

import java.util.Collection;
import java.util.Objects;

/**
 * Container for multiple {@linkplain FileLocation file locations}.
 * <p>
 * The interface represents a container that holds zero, one, or more file locations. The interface extends
 * {@link Iterable Iterable&lt;FileLocation&gt;}.
 * <p>
 * It can be used to represent a collection of files, and can be used to accept multiple file locations as an input to
 * tasks.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Use {@link #create(Collection)} to create a new instance.
 */
public interface FileCollection extends Iterable<FileLocation> {
	/**
	 * Creates a new {@link FileCollection}.
	 * <p>
	 * The argument file locations will be the enclosed in the created collection.
	 * 
	 * @param files
	 *            The file locations.
	 * @return The created file collection.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public static FileCollection create(Collection<? extends FileLocation> files) throws NullPointerException {
		Objects.requireNonNull(files, "files");
		return new FileCollectionImpl(files);
	}
}
