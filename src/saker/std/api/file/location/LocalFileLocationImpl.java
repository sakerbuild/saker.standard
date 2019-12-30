package saker.std.api.file.location;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;

class LocalFileLocationImpl implements FileLocation, Externalizable, LocalFileLocation {
	private static final long serialVersionUID = 1L;

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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
