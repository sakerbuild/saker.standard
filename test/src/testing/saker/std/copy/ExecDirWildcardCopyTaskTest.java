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

import java.io.IOException;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class ExecDirWildcardCopyTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	@Override
	protected void runTestImpl() throws Throwable {
		SakerPath dirpath = PATH_WORKING_DIRECTORY.resolve("dir");
		SakerPath dir2path = dirpath.resolve("d2");
		SakerPath dirtxt1path = dirpath.resolve("file1.txt");
		SakerPath dirtxt2path = dir2path.resolve("file2.txt");
		SakerPath diraddpath = dirpath.resolve("addfile.txt");
		SakerPath copydirpath = PATH_WORKING_DIRECTORY.resolve("copydir");
		SakerPath copydir2path = copydirpath.resolve("d2");
		SakerPath copydirtxt1path = copydirpath.resolve("file1.txt");
		SakerPath copydirtxt2path = copydir2path.resolve("file2.txt");
		SakerPath copydiraddpath = copydirpath.resolve("addfile.txt");

		SakerPath copydirextra = copydirpath.resolve("extra.txt");

		files.putFile(dirtxt1path, "f1");
		files.putFile(dirtxt2path, "f2");
		files.putFile(copydirextra, "extra");

		CombinedTargetTaskResult res;
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(copydirtxt1path).toString(), "f1");
		assertEquals(files.getAllBytes(copydirextra).toString(), "extra");
		assertException(IOException.class, () -> files.getFileAttributes(copydir2path));
		assertException(IOException.class, () -> files.getFileAttributes(copydirtxt2path));
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copydirtxt1path));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copydirtxt1path));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);

		files.putFile(dirtxt1path, "f1mod");
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(copydirtxt1path).toString(), "f1mod");
		assertEquals(files.getAllBytes(copydirextra).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copydirtxt1path));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);

		files.putFile(copydirtxt1path, "xyz");
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(copydirtxt1path).toString(), "f1mod");
		assertEquals(files.getAllBytes(copydirextra).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copydirtxt1path));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);

		files.putFile(diraddpath, "123");
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(copydirtxt1path).toString(), "f1mod");
		assertEquals(files.getAllBytes(copydiraddpath).toString(), "123");
		assertException(IOException.class, () -> files.getFileAttributes(copydir2path));
		assertException(IOException.class, () -> files.getFileAttributes(copydirtxt2path));
		assertEquals(files.getAllBytes(copydirextra).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copydirtxt1path, copydiraddpath));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copydirtxt1path, copydiraddpath));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);
	}
}
