package saker.std.main.file.property;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableSet;

import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.file.path.WildcardPath.ItemLister;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;

public class LocalDirectoryWildcardsFilePathsExecutionProperty
		implements ExecutionProperty<NavigableSet<SakerPath>>, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath path;
	private NavigableSet<WildcardPath> wildcards;

	/**
	 * For {@link Externalizable}.
	 */
	public LocalDirectoryWildcardsFilePathsExecutionProperty() {
	}

	public LocalDirectoryWildcardsFilePathsExecutionProperty(SakerPath path, NavigableSet<WildcardPath> wildcards) {
		this.path = path;
		this.wildcards = wildcards;
	}

	@Override
	public NavigableSet<SakerPath> getCurrentValue(ExecutionContext executioncontext) throws Exception {
		return ImmutableUtils.makeImmutableNavigableSet(
				WildcardPath.getItems(wildcards, ItemLister.forFileProvider(LocalFileProvider.getInstance(), path))
						.navigableKeySet());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(path);
		SerialUtils.writeExternalCollection(out, wildcards);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		path = (SakerPath) in.readObject();
		wildcards = SerialUtils.readExternalSortedImmutableNavigableSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((wildcards == null) ? 0 : wildcards.hashCode());
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
		LocalDirectoryWildcardsFilePathsExecutionProperty other = (LocalDirectoryWildcardsFilePathsExecutionProperty) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (wildcards == null) {
			if (other.wildcards != null)
				return false;
		} else if (!wildcards.equals(other.wildcards))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (path != null ? "path=" + path + ", " : "")
				+ (wildcards != null ? "wildcards=" + wildcards : "") + "]";
	}

}
