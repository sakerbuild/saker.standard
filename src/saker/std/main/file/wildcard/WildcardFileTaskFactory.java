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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Set;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.scripting.model.info.TypeInformationKind;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.dependencies.FileCollectionStrategy;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.task.utils.dependencies.WildcardFileCollectionStrategy;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileCollection;
import saker.std.api.file.location.FileLocation;
import saker.std.main.TaskDocs.DocFileCollection;

@NestTaskInformation(returnType = @NestTypeUsage(DocFileCollection.class))
@NestInformation("Resolves one or more wildcard patterns to file paths.\n"
		+ "The task will discover all files that are matched by the specified Wildcard argument. "
		+ "It returns a FileCollection that contains the paths. The result object can be passed to tasks "
		+ "which support it. Usually tasks support wildcards directly, but in some cases using this task "
		+ "can be advantageous.\n"
		+ "The returned object can be iterated over using foreach loops. The actual paths of the files can be accessed "
		+ "using the Path field on the returned elements. The task wildcards match files and directories both.")
@NestParameterInformation(value = "Wildcard",
		aliases = { "", "Wildcards" },
		required = true,
		type = @NestTypeUsage(value = Collection.class,
				kind = TypeInformationKind.COLLECTION,
				elementTypes = { WildcardPath.class }),
		info = @NestInformation("One or more wildcards to resolve.\n"
				+ "Relative wildcards will be resolved against the optional Directory argument."))
@NestParameterInformation(value = "Directory",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("The directory path to resolve relative wildcards against.\n"
				+ "If this argument is relative, it will be resolved against the working directory."))
public class WildcardFileTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "std.file.wildcard";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Wildcard", "Wildcards" }, required = true)
			public Collection<WildcardPath> wildcard;

			@SakerInput("Directory")
			public SakerPath directory;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				Collection<FileCollectionStrategy> collectionstrategies = new HashSet<>();
				for (WildcardPath wcpath : wildcard) {
					if (wcpath == null) {
						continue;
					}
					collectionstrategies.add(WildcardFileCollectionStrategy.create(directory, wcpath));
				}
				NavigableMap<SakerPath, SakerFile> files = taskcontext.getTaskUtilities()
						.collectFilesReportAdditionDependency(null, collectionstrategies);
				taskcontext.getTaskUtilities().reportInputFileDependency(null,
						ObjectUtils.singleValueMap(files.navigableKeySet(), CommonTaskContentDescriptors.PRESENT));

				Set<FileLocation> resultset = new LinkedHashSet<>();
				for (SakerPath filepath : files.keySet()) {
					resultset.add(ExecutionFileLocation.create(filepath));
				}
				Object result = FileCollection.create(resultset);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}
}
