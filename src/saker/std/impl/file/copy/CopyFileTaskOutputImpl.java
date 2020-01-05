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
package saker.std.impl.file.copy;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.std.api.file.location.FileCollection;
import saker.std.api.file.location.FileLocation;

public class CopyFileTaskOutputImpl implements Externalizable {
	private static final long serialVersionUID = 1L;

	private FileLocation target;
	private FileCollection copiedFiles;

	/**
	 * For {@link Externalizable}.
	 */
	public CopyFileTaskOutputImpl() {
	}

	public CopyFileTaskOutputImpl(FileLocation target, FileCollection copiedFiles) {
		this.target = target;
		this.copiedFiles = copiedFiles;
	}

	public FileLocation getTarget() {
		return target;
	}

	public FileCollection getCopiedFiles() {
		return copiedFiles;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(target);
		out.writeObject(copiedFiles);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		target = (FileLocation) in.readObject();
		copiedFiles = (FileCollection) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((copiedFiles == null) ? 0 : copiedFiles.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CopyFileTaskOutputImpl other = (CopyFileTaskOutputImpl) obj;
		if (copiedFiles == null) {
			if (other.copiedFiles != null)
				return false;
		} else if (!copiedFiles.equals(other.copiedFiles))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (target != null ? "target=" + target + ", " : "")
				+ (copiedFiles != null ? "copiedFiles=" + copiedFiles : "") + "]";
	}

}
