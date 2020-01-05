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
package saker.std.main.file.copy;

import java.util.Collection;
import java.util.Collections;
import java.util.NavigableSet;

import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.exception.MissingRequiredParameterException;
import saker.build.task.exception.TaskParameterException;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.impl.file.copy.CopyFileWorkerTaskFactory;
import saker.std.main.TaskDocs;
import saker.std.main.file.option.FileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

@NestTaskInformation(returnType = @NestTypeUsage(TaskDocs.CopyFileTaskOutput.class))
@NestInformation("Copies files between two file locations.\n"
		+ "The task takes two locations, one source and one target file locations. The copying will be executed between them.\n"
		+ "The task can handle both local and file system file locations. They can be intermixed, meaning that the task can copy "
		+ "from the build file hierarchy to the local file system and vice versa.\n"
		+ "The task supports copying files, and directories as well. If the Source file is not a directory, the contents of it "
		+ "is simply copied to the target location. Any required parent directories are created automatically.\n"
		+ "If the Source is a directory, then the Wildcards parameter specifies which files in it should be copied. The "
		+ "task doesn't automatically copy the subtree of a directory if you don't specify the Wildcards parameter. "
		+ "You can use the ** wildcard to copy the complete subtree.\n"
		+ "The task may throw an exception if the copy operation would result in a non-empty directory being overwritten.")
@NestParameterInformation(value = "Source",
		aliases = { "" },
		required = true,
		type = @NestTypeUsage(FileLocationTaskOption.class),
		info = @NestInformation("The Source file location that should be copied to the Target."))
@NestParameterInformation(value = "Target",
		required = true,
		type = @NestTypeUsage(FileLocationTaskOption.class),
		info = @NestInformation("The Target file location where the Source should be copied to."))
@NestParameterInformation(value = "Wildcards",
		aliases = { "Wildcard" },
		type = @NestTypeUsage(value = Collection.class, elementTypes = WildcardPath.class),
		info = @NestInformation("Specifies the part of the directory subtree that should be copied to the Target if the Source is a directory.\n"
				+ "The parameter expects one or more wildcards that when matched against the relative path of a subfile under the "
				+ "Source directory determine if it should be copied to the Target or not.\n"
				+ "If any of the specified wildcard path matches a subtree file, then it will be copied under the Target file "
				+ "with the same relative path."))
public class CopyFileTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "std.file.copy";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new CopyFileTaskImpl();
	}

	private static final class CopyFileTaskImpl implements ParameterizableTask<Object> {
		@SakerInput(value = { "", "Source" }, required = true)
		public FileLocationTaskOption sourceOption;
		@SakerInput(value = { "Target" }, required = true)
		public FileLocationTaskOption targetOption;
		@SakerInput(value = { "Wildcard", "Wildcards" })
		public Collection<WildcardPath> wildcardOption = Collections.emptyNavigableSet();

		@Override
		public Object run(TaskContext taskcontext) throws Exception {
			if (sourceOption == null) {
				taskcontext.abortExecution(new MissingRequiredParameterException(
						"Source parameter is missing for " + TASK_NAME, taskcontext.getTaskId()));
				return null;
			}
			if (this.targetOption == null) {
				taskcontext.abortExecution(new MissingRequiredParameterException(
						"Copy target location parameter is missing: " + TASK_NAME, taskcontext.getTaskId()));
				return null;
			}
			if (this.wildcardOption == null) {
				taskcontext.abortExecution(new MissingRequiredParameterException(
						"Null Wildcard parameter: " + TASK_NAME, taskcontext.getTaskId()));
				return null;
			}
			try {
				validateCopyLocation(this.targetOption, taskcontext);
				validateCopyLocation(this.sourceOption, taskcontext);
			} catch (TaskParameterException e) {
				taskcontext.abortExecution(e);
				return null;
			}
			NavigableSet<WildcardPath> wildcards = ImmutableUtils.makeImmutableNavigableSet(wildcardOption);

			FileLocation sourcelocation = TaskOptionUtils.toFileLocation(sourceOption, taskcontext);
			FileLocation targetlocation = TaskOptionUtils.toFileLocation(targetOption, taskcontext);

			CopyFileWorkerTaskFactory workertask = new CopyFileWorkerTaskFactory(sourcelocation, targetlocation,
					wildcards);
			taskcontext.startTask(workertask, workertask, null);
			SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertask);
			taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
			return result;
		}

	}

	private static void validateCopyLocation(FileLocationTaskOption location, TaskContext taskcontext) {
		if (location == null) {
			return;
		}
		TaskOptionUtils.toFileLocation(location, taskcontext).accept(new FileLocationVisitor() {
			@Override
			public void visit(ExecutionFileLocation loc) {
				if (loc.getPath() == null) {
					throw new TaskParameterException("Copy location path is null.", taskcontext.getTaskId());
				}
			}

			@Override
			public void visit(LocalFileLocation loc) {
				SakerPath path = loc.getLocalPath();
				if (path == null) {
					throw new TaskParameterException("Copy location path is null.", taskcontext.getTaskId());
				}
				if (!path.isAbsolute()) {
					throw new TaskParameterException("Local target copy location must be absolute: " + path,
							taskcontext.getTaskId());
				}
			}

		});
	}
}
