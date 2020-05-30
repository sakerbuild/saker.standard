package saker.std.api.util;

import java.util.Objects;
import java.util.UUID;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.ProviderHolderPathKey;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.task.EnvironmentSelectionResult;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionEnvironmentSelector;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.impl.file.property.TaggedLocalFileContentDescriptorExecutionProperty;
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

	/**
	 * Creates an {@linkplain ExecutionProperty execution property} that queries the content descriptor of a given local
	 * file.
	 * <p>
	 * The execution property will return the content descriptor of the denoted file by using
	 * {@link ExecutionContext#getContentDescriptor(ProviderHolderPathKey)}. If the file doesn't exist or not
	 * accessible, the property returns <code>null</code>.
	 * <p>
	 * The argument tag serves as an unique identifier for the execution property. It can be used to uniquely associate
	 * a given execution property with a given task. This can be important as the result of execution properties are
	 * cached during build execution. If another task modifies the denoted file, the changes in the file contents may be
	 * unnoticed.
	 * <p>
	 * Specifying an arbitrary tag object for the execution property helps avoiding the issue by directly associating it
	 * with a caller task. In general, the {@linkplain TaskContext#getTaskId() task identifier} of your task is a good
	 * tag to be passed to this function. Others, such an unique {@link UUID} is also a good candidate. <br>
	 * The tag should be serializable, and implement {@link Object#equals(Object)}.
	 * <p>
	 * <b>Note</b> that if you're using this execution property to report local output file dependencies, you may need
	 * to set additional unique values as part of the tag. This is because the build system will check the value of the
	 * execution property when it detects the deltas for the task. If you report the property with the same tag, then
	 * the old (cached) value may be reported, therefore causing unexpected results. An example that works around this
	 * for output files:
	 * 
	 * <pre>
	 * TaskContext taskcontext;
	 * 
	 * taskcontext.getTaskUtilities().getReportExecutionDependency(
	 * 		SakerStandardUtils.createLocalFileContentDescriptorExecutionProperty(outputlocalfilepath,
	 * 				ImmutableUtils.asUnmodifiableArrayList(taskcontext.getTaskId(), UUID.randomUUID())));
	 * </pre>
	 * 
	 * @param localfilepath
	 *            The local file path.
	 * @param tag
	 *            The tag object for the execution property. May be <code>null</code>.
	 * @return The execution property that returns the content descriptor for a given local file.
	 * @throws NullPointerException
	 *             If the path is <code>null</code>.
	 * @throws InvalidPathFormatException
	 *             If the path is not absolute.
	 * @since saker.standard 0.8.2
	 */
	public static ExecutionProperty<? extends ContentDescriptor> createLocalFileContentDescriptorExecutionProperty(
			SakerPath localfilepath, Object tag) throws NullPointerException, InvalidPathFormatException {
		SakerPathFiles.requireAbsolutePath(localfilepath);
		return TaggedLocalFileContentDescriptorExecutionProperty.create(localfilepath, tag);
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
