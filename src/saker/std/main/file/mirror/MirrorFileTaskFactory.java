package saker.std.main.file.mirror;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Path;

import saker.build.exception.FileMirroringUnavailableException;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.ParameterizableTask;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities;
import saker.build.task.TaskFactory;
import saker.build.task.exception.MissingRequiredParameterException;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.main.file.option.FileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

@NestTaskInformation(returnType = @NestTypeUsage(SakerPath.class))
@NestInformation(value = "Mirrors a file at the given path to the local file system.\n"
		+ "The task may throw an exception if the mirror directory is not available.\n"
		+ "The task may throw an exception if a file doesn't exist at the given path.\n"
		+ "The Path argument may denote a file or directory.\n"
		+ "The task returns the local file system path to the mirrored file.")
@NestParameterInformation(value = "Path",
		aliases = "",
		info = @NestInformation("The file location to mirror.\n"
				+ "It should be a valid path for the current build execution or be a local file location."),
		type = @NestTypeUsage(FileLocationTaskOption.class),
		required = true)
public class MirrorFileTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "std.file.mirror";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
			@SakerInput(value = { "", "Path" }, required = true)
			public FileLocationTaskOption location;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (location == null) {
					taskcontext.abortExecution(new MissingRequiredParameterException(
							"Path parameter is null for " + TASK_NAME, taskcontext.getTaskId()));
					return null;
				}

				Object[] result = { null };
				TaskOptionUtils.toFileLocation(location, taskcontext).accept(new FileLocationVisitor() {
					@Override
					public void visit(ExecutionFileLocation loc) {
						MirroringTaskFactory backendtask = new MirroringTaskFactory(loc.getPath());
						taskcontext.startTask(backendtask, backendtask, null);

						result[0] = new SimpleStructuredObjectTaskResult(backendtask);
						taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result[0]));
					}

					@Override
					public void visit(LocalFileLocation loc) {
						//no need to mirror, already a local path
						result[0] = loc.getLocalPath();
						taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result[0]));
					}
				});
				return result[0];
			}
		};
	}

	private static class MirroringTaskFactory
			implements TaskFactory<SakerPath>, Task<SakerPath>, Externalizable, TaskIdentifier {
		private static final long serialVersionUID = 1L;

		private SakerPath path;

		/**
		 * For {@link Externalizable}.
		 */
		public MirroringTaskFactory() {
		}

		public MirroringTaskFactory(SakerPath path) {
			this.path = path;
		}

		@Override
		public SakerPath run(TaskContext taskcontext) throws Exception {
			TaskExecutionUtilities taskutils = taskcontext.getTaskUtilities();
			SakerFile file = taskutils.resolveAtPath(path);
			if (file == null) {
				taskcontext.reportInputFileDependency(null, path, CommonTaskContentDescriptors.NOT_PRESENT);
				taskcontext.abortExecution(new FileNotFoundException("File not found at path: " + path));
				return null;
			}
			SakerPath abspath = path.isRelative() ? taskcontext.getTaskWorkingDirectoryPath().resolve(path) : path;

			taskutils.reportInputFileDependency(null, file);
			SakerPath result = SakerPath.valueOf(taskcontext.mirror(file));

			ContentDescriptor filecontents = file.getContentDescriptor();
			MirrorPathContentExecutionProperty mirrorexecproperty = new MirrorPathContentExecutionProperty(
					taskcontext.getTaskId(), abspath);
			taskcontext.reportExecutionDependency(mirrorexecproperty, filecontents);
			return result;
		}

		@Override
		public Task<? extends SakerPath> createTask(ExecutionContext executioncontext) {
			return this;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(path);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			path = (SakerPath) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((path == null) ? 0 : path.hashCode());
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
			MirroringTaskFactory other = (MirroringTaskFactory) obj;
			if (path == null) {
				if (other.path != null)
					return false;
			} else if (!path.equals(other.path))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[path=" + path + "]";
		}
	}

	private static class MirrorPathContentExecutionProperty
			implements ExecutionProperty<ContentDescriptor>, Externalizable {
		private static final long serialVersionUID = 1L;

		private TaskIdentifier associatedTask;
		private SakerPath execPath;

		/**
		 * For {@link Externalizable}.
		 */
		public MirrorPathContentExecutionProperty() {
		}

		public MirrorPathContentExecutionProperty(TaskIdentifier associatedTask, SakerPath execPath) {
			this.associatedTask = associatedTask;
			this.execPath = execPath;
		}

		@Override
		public ContentDescriptor getCurrentValue(ExecutionContext executioncontext) {
			try {
				Path mirrorpath = executioncontext.toMirrorPath(execPath);
				return executioncontext.getContentDescriptor(LocalFileProvider.getInstance().getPathKey(mirrorpath));
			} catch (FileMirroringUnavailableException e) {
				return null;
			}
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(associatedTask);
			out.writeObject(execPath);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			associatedTask = (TaskIdentifier) in.readObject();
			execPath = (SakerPath) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((associatedTask == null) ? 0 : associatedTask.hashCode());
			result = prime * result + ((execPath == null) ? 0 : execPath.hashCode());
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
			MirrorPathContentExecutionProperty other = (MirrorPathContentExecutionProperty) obj;
			if (associatedTask == null) {
				if (other.associatedTask != null)
					return false;
			} else if (!associatedTask.equals(other.associatedTask))
				return false;
			if (execPath == null) {
				if (other.execPath != null)
					return false;
			} else if (!execPath.equals(other.execPath))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + execPath + "]";
		}

	}
}
