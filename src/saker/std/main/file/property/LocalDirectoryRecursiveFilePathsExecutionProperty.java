package saker.std.main.file.property;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableSet;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.thirdparty.saker.util.ImmutableUtils;

public class LocalDirectoryRecursiveFilePathsExecutionProperty
		implements ExecutionProperty<NavigableSet<SakerPath>>, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath path;

	/**
	 * For {@link Externalizable}.
	 */
	public LocalDirectoryRecursiveFilePathsExecutionProperty() {
	}

	public LocalDirectoryRecursiveFilePathsExecutionProperty(SakerPath path) {
		this.path = path;
	}

	@Override
	public NavigableSet<SakerPath> getCurrentValue(ExecutionContext executioncontext) throws Exception {
		return ImmutableUtils.makeImmutableNavigableSet(
				LocalFileProvider.getInstance().getDirectoryEntriesRecursively(path).navigableKeySet());
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
		LocalDirectoryRecursiveFilePathsExecutionProperty other = (LocalDirectoryRecursiveFilePathsExecutionProperty) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (path != null ? "path=" + path : "") + "]";
	}

}
