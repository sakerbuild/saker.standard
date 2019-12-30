package saker.std.api.file.location;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;

class FileCollectionImpl implements FileCollection, Externalizable {
	private static final long serialVersionUID = 1L;

	private List<FileLocation> files;

	/**
	 * For {@link Externalizable}.
	 */
	public FileCollectionImpl() {
	}

	public FileCollectionImpl(Collection<? extends FileLocation> files) {
		this.files = ImmutableUtils.makeImmutableList(files);
	}

	@Override
	public Iterator<FileLocation> iterator() {
		return files.iterator();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, files);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		files = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((files == null) ? 0 : files.hashCode());
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
		FileCollectionImpl other = (FileCollectionImpl) obj;
		if (files == null) {
			if (other.files != null)
				return false;
		} else if (!files.equals(other.files))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + files + "]";
	}

}
