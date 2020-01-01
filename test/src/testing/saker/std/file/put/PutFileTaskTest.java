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
package testing.saker.std.file.put;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.ObjectUtils;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class PutFileTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	private String localPath;
	private String contents;

	@Override
	protected Map<String, ?> getTaskVariables() {
		TreeMap<String, Object> result = ObjectUtils.newTreeMap(super.getTaskVariables());
		result.put("test.out.contents", contents);
		result.put("test.local.path", localPath);
		return result;
	}

	@Override
	protected void runTestImpl() throws Throwable {
		testBuildDirOut();

		testLocalOut();
	}

	private void testLocalOut() throws Throwable {
		LocalFileProvider fp = LocalFileProvider.getInstance();

		contents = "abc";
		Path localpath = getBuildDirectory().resolve("localfile.txt");
		localPath = localpath.toString();

		runScriptTask("localout");
		assertEquals(fp.getAllBytes(localpath).toString(), contents);

		runScriptTask("localout");
		assertEmpty(getMetric().getRunTaskIdFactories());

		contents = "123";
		runScriptTask("localout");
		assertEquals(fp.getAllBytes(localpath).toString(), contents);

		runScriptTask("localout");
		assertEmpty(getMetric().getRunTaskIdFactories());

		fp.delete(localpath);
		runScriptTask("localout");
		assertEquals(fp.getAllBytes(localpath).toString(), contents);
	}

	private void testBuildDirOut() throws Throwable, AssertionError, IOException, DirectoryNotEmptyException {
		contents = "abc";

		SakerPath bdoutpath = PATH_BUILD_DIRECTORY.resolve("outfile.txt");

		runScriptTask("builddirout");
		assertEquals(files.getAllBytes(bdoutpath).toString(), contents);

		runScriptTask("builddirout");
		assertEmpty(getMetric().getRunTaskIdFactories());

		contents = "123";
		runScriptTask("builddirout");
		assertEquals(files.getAllBytes(bdoutpath).toString(), contents);

		runScriptTask("builddirout");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.delete(bdoutpath);
		runScriptTask("builddirout");
		assertEquals(files.getAllBytes(bdoutpath).toString(), contents);
	}

}
