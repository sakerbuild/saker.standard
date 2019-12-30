package testing.saker.std.param;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import saker.build.task.exception.MissingRequiredParameterException;
import saker.build.thirdparty.saker.util.ObjectUtils;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class ExecutionParameterTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		assertTaskException(MissingRequiredParameterException.class, () -> runScriptTask("noparam"));

		CombinedTargetTaskResult res;

		parameters.setUserParameters(mapWithAdded(parameters.getUserParameters(), "test.param1", "v1"));
		res = runScriptTask("param1");
		assertEquals(res.getTargetTaskResult("out"), "v1");

		res = runScriptTask("param1");
		assertEquals(res.getTargetTaskResult("out"), "v1");
		assertEmpty(getMetric().getRunTaskIdFactories());

		parameters.setUserParameters(mapWithAdded(parameters.getUserParameters(), "test.param1", "v2"));
		res = runScriptTask("param1");
		assertEquals(res.getTargetTaskResult("out"), "v2");

		assertTaskException(NoSuchElementException.class, () -> runScriptTask("nonexist"));
		
		assertTaskException(NoSuchElementException.class, () -> runScriptTask("nonexist"));
		assertEmpty(getMetric().getRunTaskIdFactories());
		
		parameters.setUserParameters(mapWithAdded(parameters.getUserParameters(), "non.existent1", "ne1"));
		res = runScriptTask("nonexist");
		assertEquals(res.getTargetTaskResult("out"), "ne1");
		
		res = runScriptTask("def");
		assertEquals(res.getTargetTaskResult("out"), "defval");
		
		res = runScriptTask("def");
		assertEquals(res.getTargetTaskResult("out"), "defval");
		assertEmpty(getMetric().getRunTaskIdFactories());
		
		parameters.setUserParameters(mapWithAdded(parameters.getUserParameters(), "non.existent2", "ne2"));
		res = runScriptTask("def");
		assertEquals(res.getTargetTaskResult("out"), "ne2");
	}

	private static Map<String, String> mapWithAdded(Map<String, String> map, String key, String value) {
		TreeMap<String, String> result = ObjectUtils.newTreeMap(map);
		result.put(key, value);
		return result;
	}
}
