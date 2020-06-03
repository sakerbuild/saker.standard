package saker.std.api.util;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.std.impl.file.mirror.MirroringTaskFactory;

/**
 * Utility class providing access to build tasks in the saker.standard package.
 * 
 * @since saker.standard 0.8.3
 */
public class SakerStandardTaskUtils {
	private SakerStandardTaskUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates a task that performs {@linkplain TaskContext#mirror(SakerFile) mirroring} for the argument path.
	 * <p>
	 * The task should be started with the task identifier retrieved from
	 * {@link #createMirroringTaskIdentifier(SakerPath)}.
	 * 
	 * @param executionPath
	 *            The absolute execution path of the file or directory to mirror.
	 * @return The task factory.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @throws InvalidPathFormatException
	 *             If the argument is not absolute.
	 */
	public static TaskFactory<? extends SakerPath> createMirroringTaskFactory(SakerPath executionPath)
			throws NullPointerException, InvalidPathFormatException {
		SakerPathFiles.requireAbsolutePath(executionPath);
		return new MirroringTaskFactory(executionPath);
	}

	/**
	 * Creates a task identifier for the {@linkplain #createMirroringTaskFactory(SakerPath) mirroring task}.
	 * 
	 * @param executionPath
	 *            The absolute execution path of the file or directory to mirror. This should be the same as the path
	 *            passed to {@link #createMirroringTaskFactory(SakerPath)}.
	 * @return The task factory.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @throws InvalidPathFormatException
	 *             If the argument is not absolute.
	 */
	public static TaskIdentifier createMirroringTaskIdentifier(SakerPath executionPath)
			throws NullPointerException, InvalidPathFormatException {
		SakerPathFiles.requireAbsolutePath(executionPath);
		return new MirroringTaskFactory(executionPath);
	}
}
