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
