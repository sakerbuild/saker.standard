package testing.saker.std.copy;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class ToLocalCopyTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	private Path copyTarget = getTestingBaseBuildDirectory().resolve(getClass().getName().replace('.', '/'))
			.resolve("copytarget.txt");

	@Override
	protected Map<String, ?> getTaskVariables() {
		return Collections.singletonMap("testing.local.location", copyTarget.toString());
	}

	@Override
	protected void runTestImpl() throws Throwable {
		LocalFileProvider localfp = LocalFileProvider.getInstance();
		localfp.delete(copyTarget);
		SakerPath filetxtpath = PATH_WORKING_DIRECTORY.resolve("file.txt");
		files.putFile(filetxtpath, "hello");

		CombinedTargetTaskResult res;
		res = runScriptTask("build");
		assertEquals(localfp.getAllBytes(copyTarget).toString(), "hello");
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyTarget);

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyTarget);

		files.putFile(filetxtpath, "hellomod");
		res = runScriptTask("build");
		assertEquals(localfp.getAllBytes(copyTarget).toString(), "hellomod");
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyTarget);

		localfp.writeToFile((InputStream) new UnsyncByteArrayInputStream("xyz".getBytes()), copyTarget);
		res = runScriptTask("build");
		assertEquals(localfp.getAllBytes(copyTarget).toString(), "hellomod");
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyTarget);
	}
}
