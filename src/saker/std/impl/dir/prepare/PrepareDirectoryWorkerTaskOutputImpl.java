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
package saker.std.impl.dir.prepare;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableSet;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.std.api.dir.prepare.PrepareDirectoryWorkerTaskOutput;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;

final class PrepareDirectoryWorkerTaskOutputImpl implements PrepareDirectoryWorkerTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath outputDirPath;
	private NavigableSet<SakerPath> outputFilePaths;

	/**
	 * For {@link Externalizable}.
	 */
	public PrepareDirectoryWorkerTaskOutputImpl() {
	}

	public PrepareDirectoryWorkerTaskOutputImpl(SakerPath outputDirPath, NavigableSet<SakerPath> outputFilePaths) {
		this.outputDirPath = outputDirPath;
		this.outputFilePaths = outputFilePaths;
	}

	@Override
	public SakerPath getPath() {
		return outputDirPath;
	}

	@Override
	public NavigableSet<SakerPath> getFiles() {
		return outputFilePaths;
	}

	//for conversion compatibility
	public SakerPath toSakerPath() {
		return getPath();
	}

	//for conversion compatibility
	public FileLocation toFileLocation() {
		return ExecutionFileLocation.create(toSakerPath());
	}

	//for conversion compatibility
	public ExecutionFileLocation toExecutionFileLocation() {
		return ExecutionFileLocation.create(toSakerPath());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(outputDirPath);
		SerialUtils.writeExternalCollection(out, outputFilePaths);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		outputDirPath = SerialUtils.readExternalObject(in);
		outputFilePaths = SerialUtils.readExternalSortedImmutableNavigableSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((outputDirPath == null) ? 0 : outputDirPath.hashCode());
		result = prime * result + ((outputFilePaths == null) ? 0 : outputFilePaths.hashCode());
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
		PrepareDirectoryWorkerTaskOutputImpl other = (PrepareDirectoryWorkerTaskOutputImpl) obj;
		if (outputDirPath == null) {
			if (other.outputDirPath != null)
				return false;
		} else if (!outputDirPath.equals(other.outputDirPath))
			return false;
		if (outputFilePaths == null) {
			if (other.outputFilePaths != null)
				return false;
		} else if (!outputFilePaths.equals(other.outputFilePaths))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (outputDirPath != null ? "outputDirPath=" + outputDirPath : "") + "]";
	}

}