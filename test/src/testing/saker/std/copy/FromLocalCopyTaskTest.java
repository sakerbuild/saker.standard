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
public class FromLocalCopyTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	private Path copySource = getTestingBaseBuildDirectory().resolve(getClass().getName().replace('.', '/'))
			.resolve("copysource.txt");

	@Override
	protected Map<String, ?> getTaskVariables() {
		return Collections.singletonMap("testing.local.location", copySource.toString());
	}

	@Override
	protected void runTestImpl() throws Throwable {
		LocalFileProvider localfp = LocalFileProvider.getInstance();

		localfp.createDirectories(copySource.getParent());
		localfp.writeToFile(new UnsyncByteArrayInputStream("hello".getBytes()), copySource);
		SakerPath filetxtpath = PATH_WORKING_DIRECTORY.resolve("file.txt");

		CombinedTargetTaskResult res;
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(filetxtpath).toString(), "hello");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), filetxtpath);

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), filetxtpath);

		localfp.writeToFile(new UnsyncByteArrayInputStream("hellomod".getBytes()), copySource);
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(filetxtpath).toString(), "hellomod");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), filetxtpath);

		files.putFile(filetxtpath, "xyz");
		res = runScriptTask("build");
		assertEquals(files.getAllBytes(filetxtpath).toString(), "hellomod");
		assertEquals(CopyTaskTestUtils.getExecutionPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getExecutionTargetPath(res.getTargetTaskResult("copy")), filetxtpath);
	}
}
