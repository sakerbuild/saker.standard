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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.util.data.annotation.ValueType;

@ValueType
final class LocalFileLocationImpl implements LocalFileLocation, Externalizable, Comparable<LocalFileLocationImpl> {
	private static final long serialVersionUID = 1L;

	private static final int TYPE_HASH = LocalFileLocationImpl.class.hashCode();

	private SakerPath path;

	/**
	 * For {@link Externalizable}.
	 */
	public LocalFileLocationImpl() {
	}

	public LocalFileLocationImpl(SakerPath path) {
		//absolute path checked by caller
		this.path = path;
	}

	@Override
	public SakerPath getLocalPath() {
		return path;
	}

	@Override
	public void accept(FileLocationVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(path);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		path = (SakerPath) in.readObject();
	}

	@Override
	public int compareTo(LocalFileLocationImpl o) {
		//paths shouldn't be null, but just in case
		return ObjectUtils.compareNullsFirst(this.path, o.path);
	}

	@Override
	public int hashCode() {
		return 31 * TYPE_HASH + Objects.hashCode(path);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocalFileLocationImpl other = (LocalFileLocationImpl) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[path=" + path + "]";
	}

}
