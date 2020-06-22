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
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.FileLocation;
import saker.std.impl.dir.prepare.PrepareDirectoryWorkerTaskFactory;
import saker.std.impl.dir.prepare.PrepareDirectoryWorkerTaskIdentifier;
import saker.std.main.TaskDocs.DocPrepareDirectoryWorkerTaskOutput;
import saker.std.main.file.utils.TaskOptionUtils;

@NestInformation("Prepares the contents of a directory with the specified files.\n"
		+ "The task will create the configured directory hierarchy in the specified output location. "
		+ "It will delete any previous outputs in case of incremental builds and copy the specified files into it.\n"
		+ "The task can be used to prepare/create the application output hierarchy and allows testing, packaging, and other "
		+ "operations to be performed later.\n"
		+ "The ClearDirectory parameter can be used to control how existing files should be handled in case of incremental builds..")
@NestTaskInformation(returnType = @NestTypeUsage(DocPrepareDirectoryWorkerTaskOutput.class))

@NestParameterInformation(value = "Contents",
		aliases = { "" },
		required = true,
		type = @NestTypeUsage(value = Collection.class, elementTypes = RelativeContentsTaskOption.class),
		info = @NestInformation("Specifies the input contents that should be placed in the output directory."))
@NestParameterInformation(value = "Output",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("Specifies the output path of the contents.\n"
				+ "The output is a forward relative path that specifies the location in the build directory."))
@NestParameterInformation(value = "ClearDirectory",
		type = @NestTypeUsage(boolean.class),
		info = @NestInformation("Specifies whether or not existing files in the output directory should be removed or not.\n"
				+ "If this parameter is set to true, the build task will remove all existing files from the output directory.\n"
				+ "If set to false, only files which were previously created by the task will be removed in case of incremental builds.\n"
				+ "The default value is false.\n"
				+ "In general keeping this parameter as false can help testing the application that you're developing as any additionally "
				+ "created files by the app will be persisted between builds. If you're creating release builds, then set this parameter "
				+ "to true to ensure that other leftover or stale files don't interfere with the build results."))
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

			@SakerInput(value = { "ClearDirectory" })
			public boolean clearDirectoryOption = false;

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
					outputpath = SakerPath.valueOf(TASK_NAME).resolve("default");
				}
				NavigableMap<SakerPath, FileLocation> inputs = RelativeContentsTaskOption.toInputMap(taskcontext,
						contents, null);

				PrepareDirectoryWorkerTaskIdentifier workertaskid = new PrepareDirectoryWorkerTaskIdentifier(
						outputpath);
				PrepareDirectoryWorkerTaskFactory workertask = new PrepareDirectoryWorkerTaskFactory(inputs,
						clearDirectoryOption);
				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}

		};
	}

}
