package saker.std.main.file.place;

import saker.build.exception.InvalidPathFormatException;
import saker.build.exception.MissingConfigurationException;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(SakerPath.class))
@NestInformation("Gets a file place in the build directory where you can work and freely modify files.\n"
		+ "The result of the task is a path where you can freely place files to during the build execution. "
		+ "It is mainly useful when you need a location where you can set up a file hierarchy suitable for your "
		+ "use-cases, without polluting the working directory or interfering with other tasks.\n"
		+ "The result path of the task will be {build-dir}/" + FilePlaceTaskFactory.TASK_NAME + "/{Path}.")
@NestParameterInformation(value = "Path",
		aliases = "",
		required = true,
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("Specifies a forward relative path where the requested place should point to.\n"
				+ "The parameter will be resolved against the build directory, and the resulting absolute path "
				+ "is returned."))
public class FilePlaceTaskFactory extends FrontendTaskFactory<SakerPath> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "std.file.place";

	@Override
	public ParameterizableTask<? extends SakerPath> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<SakerPath>() {

			@SakerInput(value = { "", "Path" }, required = true)
			public SakerPath pathOption;

			@Override
			public SakerPath run(TaskContext taskcontext) throws Exception {
				if (pathOption == null) {
					taskcontext.abortExecution(new NullPointerException("Null path specified."));
					return null;
				}
				SakerPath bdpath = taskcontext.getTaskBuildDirectoryPath();
				if (bdpath == null) {
					taskcontext.abortExecution(
							new MissingConfigurationException("No build directory configured for build execution."));
					return null;
				}
				if (!pathOption.isForwardRelative()) {
					taskcontext.abortExecution(
							new InvalidPathFormatException("Path must be forward relative: " + pathOption));
					return null;
				}
				return bdpath.resolve(TASK_NAME).resolve(pathOption);
			}
		};
	}
}
