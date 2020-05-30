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
package saker.std.main.param;

import java.util.NoSuchElementException;
import java.util.Optional;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.scripting.model.info.TypeInformationKind;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.build.util.property.UserParameterExecutionProperty;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;

@NestInformation("Gets the value of a build execution user parameter.\n"
		+ "The task queries the value of a build execution user parameter that was specified in the build configuration.\n"
		+ "If the execution parameter doesn't exists, or specified without a value, the Default argument will be returned.\n"
		+ "If the execution parameter value is failed to be determined, an exception is thrown. "
		+ "Corresponds to the -U command line flag.")
@NestTaskInformation(returnType = @NestTypeUsage(String.class))

@NestParameterInformation(value = "Name",
		aliases = { "" },
		required = true,
		type = @NestTypeUsage(value = String.class, kind = TypeInformationKind.EXECUTION_USER_PARAMETER),
		info = @NestInformation("Specifies the name of the build execution user parameter."))

@NestParameterInformation(value = "Default",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("Specifies the value to be returned if the build execution user parameter is not found for the specified name, "
				+ "or is mapped to null."))

public class ExecutionParameterTaskFactory extends FrontendTaskFactory<String> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "std.param.exec";

	@Override
	public ParameterizableTask<? extends String> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<String>() {
			@SakerInput(value = { "", "Name" }, required = true)
			public String parameterName;

			@SakerInput("Default")
			public Optional<String> defaultValue;

			@Override
			public String run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_CONFIGURATION);
				}
				if (parameterName == null) {
					taskcontext.abortExecution(new NullPointerException("Execution user parameter name is null."));
					return null;
				}

				String val = taskcontext.getTaskUtilities()
						.getReportExecutionDependency(new UserParameterExecutionProperty(parameterName));
				if (val == null) {
					if (defaultValue == null) {
						taskcontext.abortExecution(new NoSuchElementException(
								"Execution user parameter is null, and no default value specified: " + parameterName));
						return null;
					}
					return ObjectUtils.getOptional(defaultValue);
				}
				return val;
			}
		};
	}

}
