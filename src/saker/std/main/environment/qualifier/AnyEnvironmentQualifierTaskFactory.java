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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.Set;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.dependencies.CommonTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.std.api.environment.qualifier.AnyEnvironmentQualifier;
import saker.std.api.environment.qualifier.EnvironmentQualifier;
import saker.std.main.TaskDocs.DocEnvironmentQualifier;

@NestTaskInformation(returnType = @NestTypeUsage(DocEnvironmentQualifier.class))
@NestInformation("Gets an environment qualifier that doesn't restrict the suitable build environments.\n"
		+ "The qualifier will allow any build environments to be used with the associated operation.\n"
		+ "The returned environment qualifier can be used with other tasks which expect them as their inputs.\n"
		+ "The task takes no parameters.")
public class AnyEnvironmentQualifierTaskFactory
		implements TaskFactory<EnvironmentQualifier>, Task<EnvironmentQualifier>, Externalizable {
	private static final long serialVersionUID = 1L;

	public AnyEnvironmentQualifierTaskFactory() {
	}

	@Override
	public Task<? extends EnvironmentQualifier> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public EnvironmentQualifier run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_CONFIGURATION);
		}
		//never change, we always return the same object
		taskcontext.reportSelfTaskOutputChangeDetector(CommonTaskOutputChangeDetector.NEVER);
		return AnyEnvironmentQualifier.create();
	}

	@Override
	public Set<String> getCapabilities() {
		return Collections.singleton(CAPABILITY_SHORT_TASK);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	}

	@Override
	public int hashCode() {
		return getClass().getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return ObjectUtils.isSameClass(this, obj);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[]";
	}
}
