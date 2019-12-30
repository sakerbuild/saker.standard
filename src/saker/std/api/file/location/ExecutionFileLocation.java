package saker.std.api.file.location;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.util.data.DataConverterUtils;

/**
 * {@link FileLocation} that represents a file that is accessible in the build file hierarchy.
 * <p>
 * The path of the file is available using {@link #getPath()}.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Use {@link #create(SakerPath)} to construct a new instance.
 */
public interface ExecutionFileLocation extends FileLocation {
	@Override
	public default void accept(FileLocationVisitor visitor) throws NullPointerException {
		visitor.visit(this);
	}

	/**
	 * Gets the absolute execution path of the file.
	 * 
	 * @return The absolute path of the file.
	 */
	public SakerPath getPath();

	//for supporting automatic conversion to SakerPath
	/**
	 * Same as {@link #getPath()}.
	 * <p>
	 * This method is present to support automatic conversion to {@link SakerPath} using the {@link DataConverterUtils}
	 * class. This makes the execution file location assignable to task input parameters with the {@link SakerPath}
	 * type.
	 * 
	 * @return The path.
	 */
	public default SakerPath toSakerPath() {
		return getPath();
	}

	/**
	 * Creates a new execution file location.
	 * <p>
	 * The argument path must be {@linkplain SakerPath#isAbsolute() absolute}.
	 * 
	 * @param path
	 *            The execution path of the file.
	 * @return The created file location.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             If the argument is not absolute.
	 */
	public static ExecutionFileLocation create(SakerPath path) throws NullPointerException, IllegalArgumentException {
		SakerPathFiles.requireAbsolutePath(path);
		return new ExecutionFileLocationImpl(path);
	}
}