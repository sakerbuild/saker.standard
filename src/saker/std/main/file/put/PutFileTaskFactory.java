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
package saker.std.main.file.put;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.trace.BuildTrace;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.FileLocation;
import saker.std.impl.file.put.PutFileWorkerTaskFactory;
import saker.std.impl.file.put.PutFileWorkerTaskIdentifier;
import saker.std.main.TaskDocs.DocCharsetTaskOption;
import saker.std.main.TaskDocs.DocFileLocation;
import saker.std.main.file.option.FileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

@NestTaskInformation(returnType = @NestTypeUsage(DocFileLocation.class))
@NestInformation("Puts a file to the given file location.\n"
		+ "The task takes the string Contents and writes them to the specified file location. It can be used "
		+ "to write a file directly from the build script.\n"
		+ "Developers should take great care when using this task, as using the written file by other tasks can "
		+ "lead to concurrency errors and inconsistent builds. Generally, this task should be used to write a file as "
		+ "an output of the build and shouldn't be consumed by other tasks without additional task synchronization.")
@NestParameterInformation(value = "Target",
		aliases = "",
		required = true,
		type = @NestTypeUsage(FileLocationTaskOption.class),
		info = @NestInformation("Specifies the location where the file contents should be written to.\n"
				+ "The localtion may be an execution path or local file location."))
@NestParameterInformation(value = "Content",
		aliases = "Contents",
		required = true,
		type = @NestTypeUsage(String.class),
		info = @NestInformation("Specifies the string contents of the file.\n"
				+ "The specified string contents will be written to the target file locaiton. The characters "
				+ "are encoded by the charset specified in the Charset parameter."))
@NestParameterInformation(value = "Charset",
		type = @NestTypeUsage(DocCharsetTaskOption.class),
		info = @NestInformation("Specifies the charset that should be used to encode the characters into bytes.\n"
				+ "The default value is UTF-8."))
public class PutFileTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "std.file.put";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Target" }, required = true)
			public FileLocationTaskOption targetOption;

			@SakerInput(value = { "Content", "Contents" }, required = true)
			public String contentsOption;

			@SakerInput(value = "Charset")
			public String charsetOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}
				if (contentsOption == null) {
					taskcontext.abortExecution(new IllegalArgumentException("Null file contents."));
					return null;
				}
				if (targetOption == null) {
					taskcontext.abortExecution(new IllegalArgumentException("Null target."));
					return null;
				}

				FileLocation filelocation = TaskOptionUtils.toFileLocation(targetOption.clone(), taskcontext);
				PutFileWorkerTaskIdentifier workertaskid = new PutFileWorkerTaskIdentifier(filelocation);
				taskcontext.startTask(workertaskid,
						new PutFileWorkerTaskFactory(filelocation, contentsOption, charsetOption), null);
				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
