package testing.saker.std.copy;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class LocalLocalCopyTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	private Path buildDir = getTestingBaseBuildDirectory().resolve(getClass().getName().replace('.', '/'));
	private Path copySource = buildDir.resolve("copysource.txt");
	private Path copyTarget = buildDir.resolve("copytarget.txt");

	@Override
	protected Map<String, ?> getTaskVariables() {
		Map<String, Object> result = new TreeMap<>();
		result.put("testing.location.source", copySource.toString());
		result.put("testing.location.target", copyTarget.toString());
		return result;
	}

	@Override
	protected void runTestImpl() throws Throwable {
		LocalFileProvider localfp = LocalFileProvider.getInstance();
		localfp.createDirectories(buildDir);

		localfp.delete(copyTarget);
		localfp.writeToFile(new UnsyncByteArrayInputStream("hello".getBytes()), copySource);

		CombinedTargetTaskResult res;
		res = runScriptTask("build");
		assertEquals(localfp.getAllBytes(copyTarget).toString(), "hello");
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyTarget);

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyTarget);

		localfp.writeToFile(new UnsyncByteArrayInputStream("hellomod".getBytes()), copySource);
		res = runScriptTask("build");
		assertEquals(localfp.getAllBytes(copyTarget).toString(), "hellomod");
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyTarget);

		localfp.writeToFile((InputStream) new UnsyncByteArrayInputStream("xyz".getBytes()), copyTarget);
		assertEquals(localfp.getAllBytes(copyTarget).toString(), "xyz");
		res = runScriptTask("build");
		assertEquals(localfp.getAllBytes(copyTarget).toString(), "hellomod");
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")), setOf());
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyTarget);
	}
}
