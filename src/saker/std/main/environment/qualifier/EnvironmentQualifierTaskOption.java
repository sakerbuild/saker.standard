package saker.std.main.environment.qualifier;

import saker.build.task.TaskContext;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.std.api.environment.qualifier.EnvironmentQualifier;

@NestInformation("Specifies a qualifier that determines a suitable build environment.\n"
		+ "Outputs from std.env.qualifier.*() tasks can be passed to it as an input, or values retrieved using other means.")
public interface EnvironmentQualifierTaskOption {
	public EnvironmentQualifierTaskOption clone();

	public void accept(Visitor visitor);

	public static EnvironmentQualifierTaskOption valueOf(EnvironmentQualifier qualifier) {
		return new SimpleEnvironmentQualifierTaskOption(qualifier);
	}

	public interface Visitor {
		public void visit(EnvironmentQualifier qualifier);
	}
}
