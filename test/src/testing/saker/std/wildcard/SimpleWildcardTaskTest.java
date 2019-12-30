package testing.saker.std.wildcard;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.TaskName;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import testing.saker.SakerTest;
import testing.saker.build.tests.CollectingTestMetric;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class SimpleWildcardTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	//also tests the convertability of FileCollection to Collection<SakerPath>
	//   in the pathizing task factory

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
				public Collection<SakerPath> objects;

				@Override
				public Object run(TaskContext taskcontext) throws Exception {
					return ImmutableUtils.makeImmutableNavigableSet(objects);
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
	protected void runTestImpl() throws Throwable {
		CombinedTargetTaskResult res;

		res = runScriptTask("build");
		assertEquals(res.getTargetTaskResult("files"), setOf(PATH_WORKING_DIRECTORY.resolve("file.txt")));
		assertEquals(res.getTargetTaskResult("dirfiles"), setOf(PATH_WORKING_DIRECTORY.resolve("dir/file.txt")));

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(PATH_WORKING_DIRECTORY.resolve("file2.txt"), "");
		res = runScriptTask("build");
		assertEquals(res.getTargetTaskResult("files"),
				setOf(PATH_WORKING_DIRECTORY.resolve("file.txt"), PATH_WORKING_DIRECTORY.resolve("file2.txt")));
		assertEquals(res.getTargetTaskResult("dirfiles"), setOf(PATH_WORKING_DIRECTORY.resolve("dir/file.txt")));

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(PATH_WORKING_DIRECTORY.resolve("file2.txt"), "modified");
		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
	}

}
