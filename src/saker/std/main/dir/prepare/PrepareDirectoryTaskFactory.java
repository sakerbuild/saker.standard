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
package saker.std.main.dir.prepare;

import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.FileLocation;
import saker.std.impl.dir.prepare.PrepareDirectoryWorkerTaskFactory;
import saker.std.impl.dir.prepare.PrepareDirectoryWorkerTaskIdentifier;
import saker.std.main.file.utils.TaskOptionUtils;

@NestInformation("Prepares the contents of a directory with the specified files.\n"
		+ "The task will create the configured directory hierarchy in the specified output location. "
		+ "It will delete any pre-existing contents and copy the specified files into it.\n"
		+ "The task can be used to prepare/create the application output hierarchy and having any pre-existing "
		+ "files removed beforehand.\n"
		+ "Note that the task won't re-run if the inputs/outputs haven't changed. If you manually place additional files "
		+ "in the output directory, they will only be deleted when the task is re-run.")
@NestParameterInformation(value = "Contents",
		aliases = { "" },
		required = true,
		type = @NestTypeUsage(value = Collection.class, elementTypes = RelativeContentsTaskOption.class),
		info = @NestInformation("Specifies the input contents that should be placed in the output directory."))
@NestParameterInformation(value = "Output",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("Specifies the output path of the contents.\n"
				+ "The output is a forward relative path that specifies the location in the build directory."))
public class PrepareDirectoryTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "std.dir.prepare";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
			@SakerInput(value = { "", "Contents" }, required = true)
			public Collection<RelativeContentsTaskOption> contentsOption;

			@SakerInput(value = { "Output" })
			public SakerPath outputOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}

				List<RelativeContentsTaskOption> contents = ObjectUtils.cloneArrayList(contentsOption,
						RelativeContentsTaskOption::clone);

				SakerPath outputpath;
				if (outputOption != null) {
					TaskOptionUtils.requireForwardRelativePathWithFileName(outputOption, "Output");
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(outputOption);
				} else {
					outputpath = SakerPath.valueOf(TASK_NAME + "/default");
				}
				NavigableMap<SakerPath, FileLocation> inputs = RelativeContentsTaskOption.toInputMap(taskcontext,
						contents, null);

				PrepareDirectoryWorkerTaskIdentifier workertaskid = new PrepareDirectoryWorkerTaskIdentifier(
						outputpath);
				PrepareDirectoryWorkerTaskFactory workertask = new PrepareDirectoryWorkerTaskFactory(inputs);
				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}

		};
	}

}
