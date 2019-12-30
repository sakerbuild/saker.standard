package testing.saker.std.copy;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class ExecDirCopyTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	@Override
	protected void runTestImpl() throws Throwable {
		SakerPath dirpath = PATH_WORKING_DIRECTORY.resolve("dir");
		SakerPath dirtxt1path = dirpath.resolve("file1.txt");
		SakerPath dirtxt2path = dirpath.resolve("d2/file2.txt");
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
		assertEquals(files.getAllBytes(copydirtxt2path).toString(), "f2");
		assertEquals(files.getAllBytes(copydirextra).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copydirtxt1path, copydirtxt2path, copydir2path));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copydirtxt1path, copydirtxt2path, copydir2path));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);

		files.putFile(dirtxt1path, "f1mod");
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(copydirtxt1path).toString(), "f1mod");
		assertEquals(files.getAllBytes(copydirextra).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copydirtxt1path, copydirtxt2path, copydir2path));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);

		files.putFile(copydirtxt1path, "xyz");
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(copydirtxt1path).toString(), "f1mod");
		assertEquals(files.getAllBytes(copydirextra).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copydirtxt1path, copydirtxt2path, copydir2path));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);

		files.putFile(diraddpath, "123");
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(copydiraddpath).toString(), "123");
		assertEquals(files.getAllBytes(copydirextra).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copydirtxt1path, copydirtxt2path, copydir2path, copydiraddpath));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copydirtxt1path, copydirtxt2path, copydir2path, copydiraddpath));
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), copydirpath);
	}
}
