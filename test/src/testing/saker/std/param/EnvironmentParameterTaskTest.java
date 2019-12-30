package testing.saker.std.param;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import saker.build.task.exception.MissingRequiredParameterException;
import testing.saker.SakerTest;
import testing.saker.build.tests.EnvironmentTestCaseConfiguration;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class EnvironmentParameterTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	@Override
	protected Set<EnvironmentTestCaseConfiguration> getTestConfigurations() {
		Map<String, String> userparams = new TreeMap<>();
		userparams.put("test.param1", "t1");
		return EnvironmentTestCaseConfiguration.builder(super.getTestConfigurations())
				.setEnvironmentUserParameters(userparams).build();
	}

	@Override
	protected void runTestImpl() throws Throwable {
		assertTaskException(MissingRequiredParameterException.class, () -> runScriptTask("noparam"));

		CombinedTargetTaskResult res;

		res = runScriptTask("param1");
		assertEquals(res.getTargetTaskResult("out"), "t1");

		res = runScriptTask("param1");
		assertEquals(res.getTargetTaskResult("out"), "t1");
		assertEmpty(getMetric().getRunTaskIdFactories());

		assertTaskException(NoSuchElementException.class, () -> runScriptTask("nonexist"));

		assertTaskException(NoSuchElementException.class, () -> runScriptTask("nonexist"));
		assertEmpty(getMetric().getRunTaskIdFactories());

		res = runScriptTask("def");
		assertEquals(res.getTargetTaskResult("out"), "defval");

		res = runScriptTask("def");
		assertEquals(res.getTargetTaskResult("out"), "defval");
		assertEmpty(getMetric().getRunTaskIdFactories());
	}
}
