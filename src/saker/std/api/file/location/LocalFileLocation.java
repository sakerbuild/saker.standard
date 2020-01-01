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

import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.task.TaskFactory;

/**
 * {@link FileLocation} that represents a file on the <i>local</i> file system.
 * <p>
 * The local file system is the one that the build is currently running on.
 * <p>
 * The path of the file is available using {@link #getLocalPath()}.
 * <p>
 * Note that using local file locations <i>may</i> cause tasks that are
 * {@linkplain TaskFactory#CAPABILITY_REMOTE_DISPATCHABLE remote dispatchabel} to not use build clusters, as the local
 * files may not be accessible from the clusters.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Use {@link #create(SakerPath)} to construct a new instance.
 */
public interface LocalFileLocation extends FileLocation {
	@Override
	public default void accept(FileLocationVisitor visitor) throws NullPointerException {
		visitor.visit(this);
	}

	/**
	 * Gets the absolute local path of the file.
	 * 
	 * @return The absolute local file path.
	 */
	public SakerPath getLocalPath();

	/**
	 * Creates a new local file location.
	 * <p>
	 * The argument path must be {@linkplain SakerPath#isAbsolute() absolute}. The method will not check if the path is
	 * actually a valid path on the local file system.
	 * 
	 * @param path
	 *            The local path of the file.
	 * @return The created file location.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             If the argument is not absolute.
	 */
	public static LocalFileLocation create(SakerPath path) throws NullPointerException, IllegalArgumentException {
		SakerPathFiles.requireAbsolutePath(path);
		return new LocalFileLocationImpl(path);
	}

}