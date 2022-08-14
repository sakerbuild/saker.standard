package saker.std.main.file.type;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.provider.FileEntry;
import saker.std.api.file.location.FileLocation;

public class FileTypeTaskOutput implements Externalizable {
	private static final long serialVersionUID = 1L;

	private FileLocation fileLocation;
	/**
	 * Type constant from {@link FileEntry}.
	 * <p>
	 * {@link FileEntry#TYPE_NULL} if not exists.
	 */
	private int type;

	/**
	 * For {@link Externalizable}.
	 */
	public FileTypeTaskOutput() {
	}

	public FileTypeTaskOutput(FileLocation fileLocation, int type) {
		this.fileLocation = fileLocation;
		this.type = type;
	}

	//getX format is used instead of isX so the $output[Exists] format can be used in the build scripts
	//like:
	//    if (std.file.type(myfile)[Exists]) { ... }

	public boolean getExists() {
		return type != FileEntry.TYPE_NULL;
	}

	public boolean getDirectory() {
		return type == FileEntry.TYPE_DIRECTORY;
	}

	public boolean getRegularFile() {
		return type == FileEntry.TYPE_FILE;
	}

	public FileLocation getFileLocation() {
		return fileLocation;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(type);
		out.writeObject(fileLocation);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		type = in.readInt();
		fileLocation = (FileLocation) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileLocation == null) ? 0 : fileLocation.hashCode());
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
		FileTypeTaskOutput other = (FileTypeTaskOutput) obj;
		if (fileLocation == null) {
			if (other.fileLocation != null)
				return false;
		} else if (!fileLocation.equals(other.fileLocation))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[type=");
		switch (type) {
			case FileEntry.TYPE_NULL: {
				builder.append("not exists");
				break;
			}
			case FileEntry.TYPE_FILE: {
				builder.append("file");
				break;
			}
			case FileEntry.TYPE_DIRECTORY: {
				builder.append("directory");
				break;
			}
			default: {
				builder.append("unknown (");
				builder.append(type);
				builder.append(')');
				break;
			}
		}
		builder.append(", fileLocation=");
		builder.append(fileLocation);
		builder.append("]");
		return builder.toString();
	}

}
