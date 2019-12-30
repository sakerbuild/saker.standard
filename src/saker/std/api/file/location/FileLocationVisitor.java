package saker.std.api.file.location;

/**
 * Visitor interface for the possible types of {@link FileLocation}.
 * <p>
 * The visitor is used with {@link FileLocation#accept(FileLocationVisitor)} where the subject file location will call
 * the appropriate <code>visit</code> method of this interface.
 * <p>
 * All of the declared methods in this interface are <code>default</code> and throw an
 * {@link UnsupportedOperationException} by default. Additional <code>visit</code> methods may be added to this
 * interface with similar default implementations.
 * <p>
 * Clients are recommended to implement this interface.
 */
public interface FileLocationVisitor {
	/**
	 * Visits a {@linkplain LocalFileLocation local file location}.
	 * 
	 * @param loc
	 *            The file location.
	 */
	public default void visit(LocalFileLocation loc) {
		throw new UnsupportedOperationException("Unsupported file location: " + loc);
	}

	/**
	 * Visits a {@linkplain ExecutionFileLocation execution file location}.
	 * 
	 * @param loc
	 *            The file location.
	 */
	public default void visit(ExecutionFileLocation loc) {
		throw new UnsupportedOperationException("Unsupported file location: " + loc);
	}
}
