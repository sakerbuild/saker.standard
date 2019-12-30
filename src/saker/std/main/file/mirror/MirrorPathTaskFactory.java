package saker.std.main.file.mirror;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.exception.MissingRequiredParameterException;
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
@NestInformation(value = "Gets the mirror path for a given file location.\n" + "This task returns the path that the "
		+ MirrorFileTaskFactory.TASK_NAME + "() task would return, but doesn't actually execute the mirroring. "
		+ "It only converts the parameter to the local file system path where the mirroring result would be.")
@NestParameterInformation(value = "Path",
		aliases = "",
		required = true,
		type = @NestTypeUsage(FileLocationTaskOption.class),
		info = @NestInformation("The file location to get the mirror path of."))
public class MirrorPathTaskFactory extends FrontendTaskFactory<SakerPath> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "std.file.mirror.path";

	@Override
	public ParameterizableTask<? extends SakerPath> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<SakerPath>() {
			@SakerInput(value = { "", "Path" }, required = true)
			public FileLocationTaskOption location;

			@Override
			public SakerPath run(TaskContext taskcontext) throws Exception {
				if (location == null) {
					taskcontext.abortExecution(new MissingRequiredParameterException(
							"Path parameter is null for " + TASK_NAME, taskcontext.getTaskId()));
					return null;
				}

				SakerPath[] result = { null };
				TaskOptionUtils.toFileLocation(location, taskcontext).accept(new FileLocationVisitor() {
					@Override
					public void visit(ExecutionFileLocation loc) {
						result[0] = SakerPath.valueOf(executioncontext.toMirrorPath(loc.getPath()));
					}

					@Override
					public void visit(LocalFileLocation loc) {
						//no need to mirror, already a local path
						result[0] = loc.getLocalPath();
					}
				});
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result[0]));
				return result[0];
			}
		};
	}
}