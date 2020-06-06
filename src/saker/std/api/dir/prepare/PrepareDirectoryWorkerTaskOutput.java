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
package saker.std.api.dir.prepare;

import java.util.NavigableSet;

import saker.build.file.path.SakerPath;

/**
 * Output of the directory preparation task.
 * <p>
 * The output provides access to information about the output contents.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @since saker.standard 0.8.4
 */
public interface PrepareDirectoryWorkerTaskOutput {

	/**
	 * Gets the path to the output directory.
	 * 
	 * @return The absolute execution path.
	 */
	public SakerPath getPath();

	/**
	 * Gets the set of file paths that are placed in the output directory.
	 * 
	 * @return A set of absolute execution paths of the files.
	 */
	public NavigableSet<SakerPath> getFiles();
}
