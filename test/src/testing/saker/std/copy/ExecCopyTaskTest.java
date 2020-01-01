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
package testing.saker.std.copy;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class ExecCopyTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	@Override
	protected void runTestImpl() throws Throwable {
		SakerPath filetxtpath = PATH_WORKING_DIRECTORY.resolve("file.txt");
		SakerPath copiedtxtpath = PATH_WORKING_DIRECTORY.resolve("copied.txt");
		files.putFile(filetxtpath, "hello");

		CombinedTargetTaskResult res;
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(copiedtxtpath).toString(), "hello");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copiedtxtpath);

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copiedtxtpath);

		files.putFile(filetxtpath, "hellomod");
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(copiedtxtpath).toString(), "hellomod");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copiedtxtpath);

		files.putFile(copiedtxtpath, "xyz");
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(copiedtxtpath).toString(), "hellomod");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copiedtxtpath);
	}
}
