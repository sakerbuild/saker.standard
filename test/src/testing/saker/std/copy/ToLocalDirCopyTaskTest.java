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
public class ToLocalDirCopyTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	private Path buildDir = getTestingBaseBuildDirectory().resolve(getClass().getName().replace('.', '/'));
	private Path copyDirTarget = buildDir.resolve("copydir");
	private Path copyDir2Target = copyDirTarget.resolve("d2");
	private Path copyTargetf1 = copyDirTarget.resolve("file1.txt");
	private Path copyTargetf2 = copyDir2Target.resolve("file2.txt");
	private Path copyDirAddPath = copyDirTarget.resolve("addfile.txt");

	private Path extraPath = copyDirTarget.resolve("extra.txt");

	@Override
	protected Map<String, ?> getTaskVariables() {
		return Collections.singletonMap("testing.local.location", copyDirTarget.toString());
	}

	@Override
	protected void runTestImpl() throws Throwable {
		SakerPath dirpath = PATH_WORKING_DIRECTORY.resolve("dir");
		SakerPath dir2path = dirpath.resolve("d2");
		SakerPath dirtxt1path = dirpath.resolve("file1.txt");
		SakerPath dirtxt2path = dir2path.resolve("file2.txt");
		SakerPath diraddpath = dirpath.resolve("addfile.txt");

		LocalFileProvider localfp = LocalFileProvider.getInstance();
		localfp.clearDirectoryRecursively(buildDir);
		localfp.createDirectories(buildDir);

		localfp.createDirectories(extraPath.getParent());
		localfp.writeToFile(new UnsyncByteArrayInputStream("extra".getBytes()), extraPath);

		files.putFile(dirtxt1path, "f1");
		files.putFile(dirtxt2path, "f2");

		CombinedTargetTaskResult res;
		res = runScriptTask("build");
		assertEquals(localfp.getAllBytes(copyTargetf1).toString(), "f1");
		assertEquals(localfp.getAllBytes(copyTargetf2).toString(), "f2");
		assertEquals(localfp.getAllBytes(extraPath).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copyDir2Target, copyTargetf1, copyTargetf2));
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyDirTarget);

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copyDir2Target, copyTargetf1, copyTargetf2));
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyDirTarget);

		files.putFile(dirtxt1path, "f1mod");
		res = runScriptTask("build");
		assertEquals(localfp.getAllBytes(copyTargetf1).toString(), "f1mod");
		assertEquals(localfp.getAllBytes(copyTargetf2).toString(), "f2");
		assertEquals(localfp.getAllBytes(extraPath).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copyDir2Target, copyTargetf1, copyTargetf2));
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyDirTarget);

		localfp.writeToFile(new UnsyncByteArrayInputStream("xyz".getBytes()), copyTargetf1);
		res = runScriptTask("build");
		assertEquals(localfp.getAllBytes(copyTargetf1).toString(), "f1mod");
		assertEquals(localfp.getAllBytes(copyTargetf2).toString(), "f2");
		assertEquals(localfp.getAllBytes(extraPath).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copyDir2Target, copyTargetf1, copyTargetf2));
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyDirTarget);

		files.putFile(diraddpath, "123");
		res = runScriptTask("build");
		assertEquals(localfp.getAllBytes(copyDirAddPath).toString(), "123");
		assertEquals(localfp.getAllBytes(extraPath).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copyDir2Target, copyTargetf1, copyTargetf2, copyDirAddPath));
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyDirTarget);

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copyDir2Target, copyTargetf1, copyTargetf2, copyDirAddPath));
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyDirTarget);
	}
}
