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
