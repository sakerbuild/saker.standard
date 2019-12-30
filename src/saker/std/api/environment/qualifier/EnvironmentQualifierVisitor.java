package saker.std.api.environment.qualifier;

/**
 * Visitor interface for possible types of {@link EnvironmentQualifier}.
 * <p>
 * The visitor is used with {@link EnvironmentQualifier#accept(EnvironmentQualifierVisitor)} where the subject
 * environment qualifier will call the appropriate <code>visit</code> method of this interface.
 * <p>
 * All of the declared methods in this interface are <code>default</code> and throw an
 * {@link UnsupportedOperationException} by default. Additional <code>visit</code> methods may be added to this
 * interface with similar default implementations.
 * <p>
 * Clients are recommended to implement this interface.
 */
public interface EnvironmentQualifierVisitor {
	/**
	 * Visits a {@linkplain PropertyEnvironmentQualifier property environment qualifier}.
	 * 
	 * @param qualifier
	 *            The environment qualifier.
	 */
	public default void visit(PropertyEnvironmentQualifier qualifier) {
		throw new UnsupportedOperationException("Unsupported environment qualifier: " + qualifier);
	}

	/**
	 * Visits an {@linkplain AnyEnvironmentQualifier any environment qualifier}.
	 * 
	 * @param qualifier
	 *            The environment qualifier.
	 */
	public default void visit(AnyEnvironmentQualifier qualifier) {
		throw new UnsupportedOperationException("Unsupported environment qualifier: " + qualifier);
	}
}
