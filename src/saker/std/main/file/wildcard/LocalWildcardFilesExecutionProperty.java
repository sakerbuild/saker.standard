package saker.std.main.file.wildcard;

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
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;

class LocalWildcardFilesExecutionProperty
		implements ExecutionProperty<LocalWildcardFilesExecutionProperty.PropertyValue>, Externalizable {
	public static class PropertyValue implements Externalizable {
		private static final long serialVersionUID = 1L;

		private NavigableSet<SakerPath> paths;

		/**
		 * For {@link Externalizable}.
		 */
		public PropertyValue() {
		}

		public PropertyValue(NavigableSet<SakerPath> paths) {
			this.paths = paths;
		}

		public NavigableSet<SakerPath> getPaths() {
			return paths;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			SerialUtils.writeExternalCollection(out, paths);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			paths = SerialUtils.readExternalImmutableNavigableSet(in);
		}

		@Override
		public int hashCode() {
			return paths.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PropertyValue other = (PropertyValue) obj;
			if (!ObjectUtils.iterablesOrderedEquals(this.paths, other.paths)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + paths + "]";
		}

	}

	private static final long serialVersionUID = 1L;

	private NavigableSet<WildcardPath> wildcard;
	private SakerPath directory;

	/**
	 * For {@link Externalizable}.
	 */
	public LocalWildcardFilesExecutionProperty() {
	}

	public LocalWildcardFilesExecutionProperty(NavigableSet<WildcardPath> wildcard, SakerPath directory) {
		this.wildcard = wildcard;
		this.directory = directory;
	}

	@Override
	public PropertyValue getCurrentValue(ExecutionContext executioncontext) throws Exception {
		return new PropertyValue(
				WildcardPath.getItems(wildcard, ItemLister.forFileProvider(LocalFileProvider.getInstance(), directory))
						.navigableKeySet());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, wildcard);
		out.writeObject(directory);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		wildcard = SerialUtils.readExternalImmutableNavigableSet(in);
		directory = (SakerPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((directory == null) ? 0 : directory.hashCode());
		result = prime * result + ((wildcard == null) ? 0 : wildcard.hashCode());
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
		LocalWildcardFilesExecutionProperty other = (LocalWildcardFilesExecutionProperty) obj;
		if (directory == null) {
			if (other.directory != null)
				return false;
		} else if (!directory.equals(other.directory))
			return false;
		if (wildcard == null) {
			if (other.wildcard != null)
				return false;
		} else if (!wildcard.equals(other.wildcard))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (wildcard != null ? "wildcard=" + wildcard + ", " : "")
				+ (directory != null ? "directory=" + directory : "") + "]";
	}

}
