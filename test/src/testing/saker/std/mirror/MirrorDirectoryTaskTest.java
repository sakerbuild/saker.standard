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
package testing.saker.std.mirror;

import java.io.IOException;
import java.nio.file.Path;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class MirrorDirectoryTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	@Override
	protected void runTestImpl() throws Throwable {
		CombinedTargetTaskResult res;
		LocalFileProvider localfp = LocalFileProvider.getInstance();
		SakerPath mirrorexecpath = PATH_WORKING_DIRECTORY.resolve("dir");
		Path mirrorlocalpath = getBuildDirectory().resolve("mirror/wd_/dir");

		//delete at start of test to have a clean state
		localfp.deleteRecursively(mirrorlocalpath);

		SakerPath atxtexec = mirrorexecpath.resolve("a.txt");
		SakerPath btxtexec = mirrorexecpath.resolve("b.txt");

		files.putFile(atxtexec, "a");
		res = runScriptTask("build");
		assertEquals(res.getTargetTaskResult("mirror"), SakerPath.valueOf(mirrorlocalpath));
		assertEquals(localfp.getAllBytes(mirrorlocalpath.resolve("a.txt")).toString(), "a");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(atxtexec, "mod");
		res = runScriptTask("build");
		assertEquals(res.getTargetTaskResult("mirror"), SakerPath.valueOf(mirrorlocalpath));
		assertEquals(localfp.getAllBytes(mirrorlocalpath.resolve("a.txt")).toString(), "mod");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(btxtexec, "b");
		res = runScriptTask("build");
		assertEquals(res.getTargetTaskResult("mirror"), SakerPath.valueOf(mirrorlocalpath));
		assertEquals(localfp.getAllBytes(mirrorlocalpath.resolve("a.txt")).toString(), "mod");
		assertEquals(localfp.getAllBytes(mirrorlocalpath.resolve("b.txt")).toString(), "b");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		localfp.setFileBytes(SakerPath.valueOf(mirrorlocalpath.resolve("b.txt")),
				ByteArrayRegion.wrap("xx".getBytes()));
		assertEquals(localfp.getAllBytes(mirrorlocalpath.resolve("b.txt")).toString(), "xx");

		res = runScriptTask("build");
		assertEquals(res.getTargetTaskResult("mirror"), SakerPath.valueOf(mirrorlocalpath));
		assertEquals(localfp.getAllBytes(mirrorlocalpath.resolve("a.txt")).toString(), "mod");
		assertEquals(localfp.getAllBytes(mirrorlocalpath.resolve("b.txt")).toString(), "b");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.delete(atxtexec);
		res = runScriptTask("build");
		assertEquals(res.getTargetTaskResult("mirror"), SakerPath.valueOf(mirrorlocalpath));
		assertException(IOException.class, () -> localfp.getAllBytes(mirrorlocalpath.resolve("a.txt")).toString());
		assertEquals(localfp.getAllBytes(mirrorlocalpath.resolve("b.txt")).toString(), "b");

		files.deleteRecursively(mirrorexecpath);
		files.putFile(mirrorexecpath, "dirreplacement");
		res = runScriptTask("build");
		assertEquals(localfp.getAllBytes(mirrorlocalpath).toString(), "dirreplacement");
	}
}
