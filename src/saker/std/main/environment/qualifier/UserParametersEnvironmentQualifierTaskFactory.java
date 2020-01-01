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
package saker.std.main.environment.qualifier;

import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.util.property.UserParameterEnvironmentProperty;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.environment.qualifier.AnyEnvironmentQualifier;
import saker.std.api.environment.qualifier.EnvironmentQualifier;
import saker.std.api.environment.qualifier.PropertyEnvironmentQualifier;
import saker.std.main.TaskDocs.DocEnvironmentQualifier;

@NestTaskInformation(returnType = @NestTypeUsage(DocEnvironmentQualifier.class))
@NestInformation("Creates an environment qualifier that expects the specified environment user parameters to have the "
		+ "associated values.\n"
		+ "The returned environment qualifier can be used with other tasks which expect them as their inputs.")
@NestParameterInformation(value = "Parameters",
		aliases = { "" },
		required = true,
		type = @NestTypeUsage(Map.class),
		info = @NestInformation("Specifies the environment user parameters and their expected values.\n"
				+ "A build environment will be considered to be suitable by the result if the specified environment "
				+ "user parameters have the associated values defined for them."))
public class UserParametersEnvironmentQualifierTaskFactory extends FrontendTaskFactory<EnvironmentQualifier> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "std.env.qualifier.params";

	@Override
	public ParameterizableTask<? extends EnvironmentQualifier> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<EnvironmentQualifier>() {
			@SakerInput(value = { "Parameters", "" }, required = true)
			public Map<String, String> parametersOption;

			@Override
			public EnvironmentQualifier run(TaskContext taskcontext) throws Exception {
				NavigableMap<String, String> expected = ImmutableUtils.makeImmutableNavigableMap(parametersOption);
				if (expected == null) {
					taskcontext.abortExecution(new IllegalArgumentException("Expected parameter values map is null."));
					return null;
				}
				if (expected.isEmpty()) {
					return AnyEnvironmentQualifier.create();
				}
				PropertyEnvironmentQualifier result;
				if (expected.size() == 1) {
					Entry<String, String> entry = expected.firstEntry();
					result = PropertyEnvironmentQualifier.create(new UserParameterEnvironmentProperty(entry.getKey()),
							entry.getValue());
				} else {
					result = PropertyEnvironmentQualifier
							.create(new MultiUserParameterEnvironmentProperty(expected.keySet()), expected);
				}
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
