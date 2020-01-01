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
package saker.std.main.file.wildcard;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.NavigableSet;
import java.util.Set;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.scripting.model.info.TypeInformationKind;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.FileCollection;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.main.TaskDocs.DocFileCollection;

@NestTaskInformation(returnType = @NestTypeUsage(DocFileCollection.class))
@NestInformation("Resolves one or more wildcard patterns to file paths on the local file system.\n"
		+ "The task will discover all files that are matched by the specified Wildcard argument. "
		+ "It returns a FileCollection thta contains the local paths. The result can be passed to tasks "
		+ "which support it. As tasks generally support wildcard inputs with execution files, this can be "
		+ "useful to pass local input files to tasks using wildcards.\n"
		+ "The returned object can be iterated over using foreach loops. The actual paths of files can be accessed "
		+ "using the LocalPath field on the returned elements. The task wildcards match files and directories both.\n"
		+ "This task is similar to " + WildcardFileTaskFactory.TASK_NAME + "() but operates on local files.")
@NestParameterInformation(value = "Wildcard",
		aliases = { "", "Wildcards" },
		required = true,
		type = @NestTypeUsage(value = Collection.class,
				kind = TypeInformationKind.COLLECTION,
				elementTypes = { WildcardPath.class }),
		info = @NestInformation("One or more wildcards to resolve.\n"
				+ "Relative wildcards will be resolved against the optional Directory argument. "
				+ "If a wildcard is relative, and no Directory argument is specified, it will most likely "
				+ "discover no files. The current working directory of the build process is not considered."))
@NestParameterInformation(value = "Directory",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("The absolute directory local path to resolve relative wildcards against.\n"
				+ "If this argument is relative, an exception is thrown."))
public class LocalWildcardFileTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "std.file.wildcard.local";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Wildcard", "Wildcards" }, required = true)
			public Collection<WildcardPath> wildcard;

			@SakerInput("Directory")
			public SakerPath directory;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (directory != null && !directory.isAbsolute()) {
					taskcontext.abortExecution(new InvalidPathFormatException(
							"The Directory local path argument must be absolute: " + directory));
					return null;
				}
				NavigableSet<SakerPath> files = taskcontext.getTaskUtilities()
						.getReportExecutionDependency(new LocalWildcardFilesExecutionProperty(
								ImmutableUtils.makeImmutableNavigableSet(wildcard), directory))
						.getPaths();
				Set<FileLocation> resultset = new LinkedHashSet<>();
				for (SakerPath filepath : files) {
					resultset.add(LocalFileLocation.create(filepath));
				}
				Object result = FileCollection.create(resultset);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}
}
