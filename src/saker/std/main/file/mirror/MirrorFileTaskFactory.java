/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package saker.std.main.file.mirror;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.exception.MissingRequiredParameterException;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.trace.BuildTrace;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.impl.file.mirror.MirroringTaskFactory;
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
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}
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
}
