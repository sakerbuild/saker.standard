package saker.std.api.util;

import java.util.Objects;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.task.EnvironmentSelectionResult;
import saker.build.task.TaskExecutionEnvironmentSelector;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.impl.property.EnvironmentSelectionTestExecutionProperty;

/**
 * Utility class containing functions for the standard classes.
 * 
 * @since saker.standard 0.8.1
 */
public class SakerStandardUtils {
	private SakerStandardUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the file name part of the argument file location.
	 * <p>
	 * The method will return the last file name component for the path of the argument.
	 * 
	 * @param fl
	 *            The file location.
	 * @return The file name or <code>null</code> if the argument is <code>null</code>, or has no file name.
	 * @see SakerPath#getFileName()
	 * @since saker.standard 0.8.1
	 */
	public static String getFileLocationFileName(FileLocation fl) {
		if (fl == null) {
			return null;
		}
		FileLocationFileNameVisitor visitor = new FileLocationFileNameVisitor();
		fl.accept(visitor);
		return visitor.result;
	}

	/**
	 * Creates an {@linkplain ExecutionProperty execution property} that tests and environment selector.
	 * <p>
	 * The execution property will use
	 * {@link ExecutionContext#testEnvironmentSelection(TaskExecutionEnvironmentSelector, java.util.Set)} to check the
	 * environment selection of the argument. It can be useful when tasks want to examine or store the results of an
	 * environment selection, and act based on that.
	 * <p>
	 * The property can be used with build clusters to determine if a given operation can be executed in the current
	 * build configuration.
	 * 
	 * @param environmentselector
	 *            The environment selector.
	 * @return The execution property.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @since saker.standard 0.8.1
	 */
	public static ExecutionProperty<? extends EnvironmentSelectionResult> createEnvironmentSelectionTestExecutionProperty(
			TaskExecutionEnvironmentSelector environmentselector) throws NullPointerException {
		Objects.requireNonNull(environmentselector, "environment selector");
		return new EnvironmentSelectionTestExecutionProperty(environmentselector);
	}

	private static class FileLocationFileNameVisitor implements FileLocationVisitor {
		protected String result;

		public FileLocationFileNameVisitor() {
		}

		@Override
		public void visit(LocalFileLocation loc) {
			result = loc.getLocalPath().getFileName();
		}

		@Override
		public void visit(ExecutionFileLocation loc) {
			result = loc.getPath().getFileName();
		}
	}

}
