package saker.std.api.environment.qualifier;

/**
 * {@link EnvironmentQualifier} that specifies that any build environment can be used for a given operation.
 * <p>
 * The interface contains no properties.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Use {@link #create()} to get an instance.
 */
public interface AnyEnvironmentQualifier extends EnvironmentQualifier {
	@Override
	public default void accept(EnvironmentQualifierVisitor visitor) throws NullPointerException {
		visitor.visit(this);
	}

	/**
	 * Gets the hash code for the environment qualifier.
	 * <p>
	 * Defined as:
	 * 
	 * <pre>
	 * AnyEnvironmentQualifier.class.getName().hashCode()
	 * </pre>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode();

	/**
	 * Checks if the argument object is an instance of {@link AnyEnvironmentQualifier}.
	 * <p>
	 * The equality check should return <code>true</code> if the argument is an instance of
	 * {@link AnyEnvironmentQualifier}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj);

	/**
	 * Gets an {@link AnyEnvironmentQualifier} instance.
	 * 
	 * @return An instance.
	 */
	public static AnyEnvironmentQualifier create() {
		return SimpleAnyEnvironmentQualifier.INSTANCE;
	}
}
