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
package saker.std.main.file.type;

import java.util.UUID;

import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.FileEntry;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.exception.MissingRequiredParameterException;
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
import saker.std.impl.file.property.TaggedLocalFileTypeExecutionProperty;
import saker.std.main.TaskDocs.DocFileTypeTaskOutput;
import saker.std.main.file.option.FileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

@NestTaskInformation(returnType = @NestTypeUsage(DocFileTypeTaskOutput.class))
@NestInformation("Gets information about the type of a file.\n"
		+ "The task will query the file system for the file at the given file location "
		+ "and returns an object containing information about its type.\n"
		+ "In case information about a local file is queried, symbolic links will be followed.")
@NestParameterInformation(value = "Path",
		aliases = "",
		required = true,
		type = @NestTypeUsage(FileLocationTaskOption.class),
		info = @NestInformation("The path or file location of the file that should be queried."))
public class FileTypeTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "std.file.type";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
			@SakerInput(value = { "", "Path" }, required = true)
			public FileLocationTaskOption location;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_META);
				}
				if (location == null) {
					taskcontext.abortExecution(new MissingRequiredParameterException(
							"Path parameter is null for " + TASK_NAME, taskcontext.getTaskId()));
					return null;
				}
				Object[] result = { null };
				TaskOptionUtils.visitFileLocation(location, taskcontext, new FileLocationVisitor() {
					@Override
					public void visit(ExecutionFileLocation loc) {
						SakerPath path = loc.getPath();
						SakerFile foundfile = taskcontext.getTaskUtilities().resolveAtPath(path);
						if (foundfile == null) {
							taskcontext.reportInputFileDependency(null, path, CommonTaskContentDescriptors.NOT_PRESENT);
							result[0] = new FileTypeTaskOutput(loc, FileEntry.TYPE_NULL);
						} else if (foundfile instanceof SakerDirectory) {
							taskcontext.reportInputFileDependency(null, path,
									CommonTaskContentDescriptors.IS_DIRECTORY);
							result[0] = new FileTypeTaskOutput(loc, FileEntry.TYPE_DIRECTORY);
						} else {
							taskcontext.reportInputFileDependency(null, path, CommonTaskContentDescriptors.IS_FILE);
							result[0] = new FileTypeTaskOutput(loc, FileEntry.TYPE_FILE);
						}
					}

					@Override
					public void visit(LocalFileLocation loc) {
						UUID taskuuid = UUID.randomUUID();
						Integer filetype = taskcontext.getTaskUtilities().getReportExecutionDependency(
								TaggedLocalFileTypeExecutionProperty.create(loc.getLocalPath(), taskuuid));
						if (filetype == null) {
							result[0] = new FileTypeTaskOutput(loc, FileEntry.TYPE_NULL);
						} else {
							result[0] = new FileTypeTaskOutput(loc, filetype);
						}
					}
				});
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result[0]));
				return result[0];
			}
		};
	}

}
