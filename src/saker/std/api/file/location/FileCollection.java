package saker.std.api.file.location;

import java.util.Collection;
import java.util.Objects;

/**
 * Container for multiple {@linkplain FileLocation file locations}.
 * <p>
 * The interface represents a container that holds zero, one, or more file locations. The interface extends
 * {@link Iterable Iterable&lt;FileLocation&gt;}.
 * <p>
 * It can be used to represent a collection of files, and can be used to accept multiple file locations as an input to
 * tasks.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Use {@link #create(Collection)} to create a new instance.
 */
public interface FileCollection extends Iterable<FileLocation> {
	/**
	 * Creates a new {@link FileCollection}.
	 * <p>
	 * The argument file locations will be the enclosed in the created collection.
	 * 
	 * @param files
	 *            The file locations.
	 * @return The created file collection.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public static FileCollection create(Collection<? extends FileLocation> files) throws NullPointerException {
		Objects.requireNonNull(files, "files");
		return new FileCollectionImpl(files);
	}
}
