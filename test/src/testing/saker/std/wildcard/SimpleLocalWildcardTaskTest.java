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
package testing.saker.std.wildcard;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.TaskName;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import testing.saker.SakerTest;
import testing.saker.build.tests.CollectingTestMetric;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class SimpleLocalWildcardTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	public static class PathizingTaskFactory implements TaskFactory<Object>, Externalizable {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public PathizingTaskFactory() {
		}

		@Override
		public Task<? extends Object> createTask(ExecutionContext executioncontext) {
			return new ParameterizableTask<Object>() {

				@SakerInput(value = "", required = true)
				public Collection<Object> objects;

				@Override
				public Object run(TaskContext taskcontext) throws Exception {
					Set<SakerPath> result = new TreeSet<>();
					for (Object o : objects) {
						result.add(
								(SakerPath) ReflectUtils
										.getMethodAssert(
												ReflectUtils.findInterfaceWithNameInHierarchy(o.getClass(),
														"saker.std.api.file.location.LocalFileLocation"),
												"getLocalPath")
										.invoke(o));
					}
					return result;
				}
			};
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
			return "PathizingTaskFactory[]";
		}
	}

	@Override
	protected CollectingTestMetric createMetricImpl() {
		CollectingTestMetric result = super.createMetricImpl();
		TreeMap<TaskName, TaskFactory<?>> injecteds = ObjectUtils.newTreeMap(result.getInjectedTaskFactories());
		injecteds.put(TaskName.valueOf("test.pathize"), new PathizingTaskFactory());
		result.setInjectedTaskFactories(injecteds);
		return result;
	}

	@Override
	protected Map<String, ?> getTaskVariables() {
		TreeMap<String, Object> result = ObjectUtils.newTreeMap(super.getTaskVariables());
		result.put("test.local.working.dir", getWorkingDirectory().toString());
		result.put("test.local.build.dir", getBuildDirectory().toString());
		return result;
	}

	@Override
	protected void runTestImpl() throws Throwable {
		LocalFileProvider.getInstance().clearDirectoryRecursively(getBuildDirectory());
		LocalFileProvider.getInstance().createDirectories(getBuildDirectory());
		LocalFileProvider.getInstance().writeToFile(new UnsyncByteArrayInputStream("".getBytes()),
				getBuildDirectory().resolve("bdfile.txt"));

		Set<SakerPath> rootpaths = new TreeSet<>();
		for (String r : LocalFileProvider.getInstance().getRoots()) {
			rootpaths.add(SakerPath.valueOf(r));
		}

		CombinedTargetTaskResult res;

		res = runScriptTask("build");
		assertEquals(res.getTargetTaskResult("roots"), rootpaths);
		assertEquals(res.getTargetTaskResult("workdircontents"),
				setOf(SakerPath.valueOf(getWorkingDirectory().resolve("file.txt"))));
		assertEquals(res.getTargetTaskResult("builddircontents"),
				setOf(SakerPath.valueOf(getBuildDirectory().resolve("bdfile.txt"))));

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		LocalFileProvider.getInstance().writeToFile(new UnsyncByteArrayInputStream("".getBytes()),
				getBuildDirectory().resolve("bdfile2.txt"));
		res = runScriptTask("build");
		assertEquals(res.getTargetTaskResult("builddircontents"),
				setOf(SakerPath.valueOf(getBuildDirectory().resolve("bdfile.txt")),
						SakerPath.valueOf(getBuildDirectory().resolve("bdfile2.txt"))));

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		LocalFileProvider.getInstance().writeToFile(new UnsyncByteArrayInputStream("modified".getBytes()),
				getBuildDirectory().resolve("bdfile2.txt"));
		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(res.getTargetTaskResult("builddircontents"),
				setOf(SakerPath.valueOf(getBuildDirectory().resolve("bdfile.txt")),
						SakerPath.valueOf(getBuildDirectory().resolve("bdfile2.txt"))));
	}

}
