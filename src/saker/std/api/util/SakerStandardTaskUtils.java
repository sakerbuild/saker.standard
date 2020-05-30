package saker.std.api.util;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
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

	public static TaskFactory<? extends SakerPath> createMirroringTaskFactory(SakerPath executionPath)
			throws NullPointerException, InvalidPathFormatException {
		SakerPathFiles.requireAbsolutePath(executionPath);
		return new MirroringTaskFactory(executionPath);
	}

	public static TaskIdentifier createMirroringTaskIdentifier(SakerPath executionPath)
			throws NullPointerException, InvalidPathFormatException {
		SakerPathFiles.requireAbsolutePath(executionPath);
		return new MirroringTaskFactory(executionPath);
	}
}
