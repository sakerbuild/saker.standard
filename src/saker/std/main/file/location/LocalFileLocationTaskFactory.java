package saker.std.main.file.location;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.exception.TaskParameterException;
import saker.build.task.utils.annot.SakerInput;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.main.TaskDocs.DocLocalFileLocation;

@NestTaskInformation(returnType = @NestTypeUsage(DocLocalFileLocation.class))
@NestInformation("Specifies a file location on the local file system.\n"
		+ "This task can be used to create a file location object that references a file on the file system "
		+ "that the build is executed on.\n"
		+ "As most tasks use execution related paths as their input and output specifications, this task "
		+ "can be used to specify a local file. Using the result of this task as an input to other tasks "
		+ "may not be supported in all cases. It depends on the accepting task implementation.")

@NestParameterInformation(value = "Path",
		aliases = { "" },
		type = @NestTypeUsage(SakerPath.class),
		required = true,
		info = @NestInformation("Specifies  the absolute local file system path for which the file location should be created.\n"
				+ "The given path must be absolute. A file is not required to exist at the given location. "
				+ "The specified file may be of any kind, its representation is based on the accepting task. "
				+ "(I.e. it may be a regular file, directory, link, or non-existent.)"))
public class LocalFileLocationTaskFactory extends FrontendTaskFactory<FileLocation> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "std.file.local";

	@Override
	public ParameterizableTask<? extends FileLocation> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<FileLocation>() {
			@SakerInput(value = { "", "Path" }, required = true)
			public SakerPath path;

			@Override
			public FileLocation run(TaskContext taskcontext) throws Exception {
				if (path == null || !path.isAbsolute()) {
					taskcontext.abortExecution(
							new TaskParameterException("Path must be absolute: " + path, taskcontext.getTaskId()));
					return null;
				}
				return LocalFileLocation.create(path);
			}
		};
	}

}
