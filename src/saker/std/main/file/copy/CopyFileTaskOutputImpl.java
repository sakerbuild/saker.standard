package saker.std.main.file.copy;

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
