package saker.std.impl.file.property;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionProperty;

public class TaggedLocalFileTypeExecutionProperty extends LocalFileTypeExecutionProperty {
	private static final long serialVersionUID = 1L;

	private Object tag;

	/**
	 * For {@link Externalizable}.
	 */
	public TaggedLocalFileTypeExecutionProperty() {
	}

	protected TaggedLocalFileTypeExecutionProperty(SakerPath path, Object tag) {
		super(path);
		this.tag = tag;
	}

	public static ExecutionProperty<Integer> create(SakerPath path, Object tag) {
		if (tag == null) {
			return new LocalFileTypeExecutionProperty(path);
		}
		return new TaggedLocalFileTypeExecutionProperty(path, tag);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(tag);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		tag = in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaggedLocalFileTypeExecutionProperty other = (TaggedLocalFileTypeExecutionProperty) obj;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		return true;
	}
}
