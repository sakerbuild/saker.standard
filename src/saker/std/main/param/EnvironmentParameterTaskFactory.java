package saker.std.main.param;

import java.util.NoSuchElementException;
import java.util.Optional;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.scripting.model.info.TypeInformationKind;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.util.property.UserParameterEnvironmentProperty;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;

@NestInformation("Gets the value of a build environment user parameter.\n"
		+ "The task queries the value of a build environment user parameter that was specified when creating the build environment.\n"
		+ "If the environment parameter doesn't exists, or specified without a value, the Default argument will be returned.\n"
		+ "If the environment parameter value is failed to be determined, an exception is thrown. "
		+ "The name is always resolved from the build environment that is used to execute the build.\n"
		+ "Corresponds to the -EU command line flag.")
@NestTaskInformation(returnType = @NestTypeUsage(String.class))

@NestParameterInformation(value = "Name",
		aliases = { "" },
		required = true,
		type = @NestTypeUsage(value = String.class, kind = TypeInformationKind.ENVIRONMENT_USER_PARAMETER),
		info = @NestInformation("Specifies the name of the build environment user parameter."))

@NestParameterInformation(value = "Default",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("Specifies the value to be returned if the build environment user parameter is not found for the specified name, "
				+ "or is mapped to null."))

public class EnvironmentParameterTaskFactory extends FrontendTaskFactory<String> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "std.param.env";
	
	@Override
	public ParameterizableTask<? extends String> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<String>() {
			@SakerInput(value = { "", "Name" }, required = true)
			public String parameterName;

			@SakerInput("Default")
			public Optional<String> defaultValue;

			@Override
			public String run(TaskContext taskcontext) throws Exception {
				if (parameterName == null) {
					taskcontext.abortExecution(new NullPointerException("Environment user parameter name is null."));
					return null;
				}

				String val = taskcontext.getTaskUtilities()
						.getReportEnvironmentDependency(new UserParameterEnvironmentProperty(parameterName));
				if (val == null) {
					if (defaultValue == null) {
						taskcontext.abortExecution(new NoSuchElementException(
								"Environment user parameter is null, and no default value specified: "
										+ parameterName));
						return null;
					}
					return ObjectUtils.getOptional(defaultValue);
				}
				return val;
			}
		};
	}
}
