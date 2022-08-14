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
package testing.saker.std.file.type;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.ReflectUtils;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class FileTypeTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	private String localPath;
	private String execPath;

	@Override
	protected Map<String, ?> getTaskVariables() {
		TreeMap<String, Object> result = ObjectUtils.newTreeMap(super.getTaskVariables());
		result.put("test.exec.path", execPath);
		result.put("test.local.path", localPath);
		return result;
	}

	@Override
	protected void runTestImpl() throws Throwable {
		runExecTest();

		runLocalTest();
	}

	private void runLocalTest() throws Throwable {
		final String TARGET_NAME = "localtest";

		Path path = getBuildDirectory().resolve("file.txt");
		localPath = path.toString();

		LocalFileProvider fp = LocalFileProvider.getInstance();
		fp.createDirectories(path.getParent());
		fp.delete(path);

		runCheck(TARGET_NAME, false, false, false);

		runCheck(TARGET_NAME, false, false, false);
		assertMap(getMetric().getRunTaskIdFactories());

		createDummyFile(fp, path);

		runCheck(TARGET_NAME, true, true, false);
		assertMap(getMetric().getRunTaskIdFactories());

		fp.delete(path);
		runCheck(TARGET_NAME, false, false, false);

		fp.createDirectories(path);
		runCheck(TARGET_NAME, true, false, true);

		fp.delete(path);
		runCheck(TARGET_NAME, false, false, false);
	}

	private void runExecTest() throws Throwable {
		final String TARGET_NAME = "exectest";

		SakerPath execpath = PATH_WORKING_DIRECTORY.resolve("file.txt");
		execPath = execpath.toString();

		runCheck(TARGET_NAME, false, false, false);

		runCheck(TARGET_NAME, false, false, false);
		assertMap(getMetric().getRunTaskIdFactories());

		files.putFile(execpath, "abc");
		runCheck(TARGET_NAME, true, true, false);

		runCheck(TARGET_NAME, true, true, false);
		assertMap(getMetric().getRunTaskIdFactories());

		files.delete(execpath);
		runCheck(TARGET_NAME, false, false, false);

		files.createDirectories(execpath);
		runCheck(TARGET_NAME, true, false, true);

		runCheck(TARGET_NAME, true, false, true);
		assertMap(getMetric().getRunTaskIdFactories());

		files.delete(execpath);
		files.putFile(execpath, "abc");
		runCheck(TARGET_NAME, true, true, false);

		files.delete(execpath);
		files.createDirectories(execpath);
		runCheck(TARGET_NAME, true, false, true);

		files.delete(execpath);
		runCheck(TARGET_NAME, false, false, false);
	}

	private void runCheck(String targetname, boolean exists, boolean regularfile, boolean directory) throws Throwable {
		CombinedTargetTaskResult buildoutput = runScriptTask(targetname, SakerPath.valueOf(DEFAULT_BUILD_FILE_NAME));
		Object res = buildoutput.getTargetTaskResult("res");
		assertEquals(ReflectUtils.getMethodAssert(res.getClass(), "getExists").invoke(res), exists, "exists check");
		assertEquals(ReflectUtils.getMethodAssert(res.getClass(), "getRegularFile").invoke(res), regularfile,
				"regular file check");
		assertEquals(ReflectUtils.getMethodAssert(res.getClass(), "getDirectory").invoke(res), directory,
				"directory check");

		assertEquals(buildoutput.getTargetTaskResult("ex"), exists, "exists task output");
		assertEquals(buildoutput.getTargetTaskResult("rf"), regularfile, " regular file task output");
		assertEquals(buildoutput.getTargetTaskResult("dir"), directory, "directory task output");
	}

	private static void createDummyFile(LocalFileProvider fp, Path path) throws IOException {
		try (OutputStream os = fp.openOutputStream(path)) {
			os.write("abc".getBytes());
		}
	}
}
