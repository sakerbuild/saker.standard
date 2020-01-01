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

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class FromLocalDirCopyTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	private Path buildDir = getTestingBaseBuildDirectory().resolve(getClass().getName().replace('.', '/'));
	private Path copySource = buildDir.resolve("dir");
	private Path copyDir2 = copySource.resolve("d2");
	private Path copySourcef1 = copySource.resolve("file1.txt");
	private Path copySourcef2 = copyDir2.resolve("file2.txt");
	private Path copyAddPath = copySource.resolve("addfile.txt");

	@Override
	protected Map<String, ?> getTaskVariables() {
		return Collections.singletonMap("testing.local.location", copySource.toString());
	}

	@Override
	protected void runTestImpl() throws Throwable {
		LocalFileProvider localfp = LocalFileProvider.getInstance();
		localfp.clearDirectoryRecursively(buildDir);
		localfp.createDirectories(copySourcef1.getParent());
		localfp.createDirectories(copySourcef2.getParent());

		localfp.writeToFile(new UnsyncByteArrayInputStream("f1".getBytes()), copySourcef1);
		localfp.writeToFile(new UnsyncByteArrayInputStream("f2".getBytes()), copySourcef2);

		SakerPath copydirpath = PATH_WORKING_DIRECTORY.resolve("copydir");
		SakerPath copydir2path = copydirpath.resolve("d2");
		SakerPath copytargetf1 = copydirpath.resolve("file1.txt");
		SakerPath copytargetf2 = copydir2path.resolve("file2.txt");
		SakerPath copydiraddpath = copydirpath.resolve("addfile.txt");

		SakerPath copydirextra = copydirpath.resolve("extra.txt");
		files.putFile(copydirextra, "extra");

		CombinedTargetTaskResult res;
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(copytargetf1).toString(), "f1");
		assertEquals(files.getAllBytes(copytargetf2).toString(), "f2");
		assertEquals(files.getAllBytes(copydirextra).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copytargetf1, copytargetf2, copydir2path));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copytargetf1, copytargetf2, copydir2path));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);

		localfp.writeToFile(new UnsyncByteArrayInputStream("f1mod".getBytes()), copySourcef1);
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(copytargetf1).toString(), "f1mod");
		assertEquals(files.getAllBytes(copytargetf2).toString(), "f2");
		assertEquals(files.getAllBytes(copydirextra).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copytargetf1, copytargetf2, copydir2path));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);

		files.putFile(copytargetf1, "xyz");
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(copytargetf1).toString(), "f1mod");
		assertEquals(files.getAllBytes(copytargetf2).toString(), "f2");
		assertEquals(files.getAllBytes(copydirextra).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copytargetf1, copytargetf2, copydir2path));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);

		localfp.writeToFile(new UnsyncByteArrayInputStream("123".getBytes()), copyAddPath);
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(copydiraddpath).toString(), "123");
		assertEquals(files.getAllBytes(copydirextra).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copytargetf1, copytargetf2, copydir2path, copydiraddpath));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copytargetf1, copytargetf2, copydir2path, copydiraddpath));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);
	}
}
