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
public class LocalLocalDirectoryCopyTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	private Path buildDir = getTestingBaseBuildDirectory().resolve(getClass().getName().replace('.', '/'));
	private Path copySource = buildDir.resolve("dir");
	private Path copyDir2 = copySource.resolve("d2");
	private Path copySourcef1 = copySource.resolve("file1.txt");
	private Path copySourcef2 = copyDir2.resolve("file2.txt");
	private Path copyAddPath = copySource.resolve("addfile.txt");
	private Path copyDirTarget = buildDir.resolve("copydir");
	private Path copyDir2Target = copyDirTarget.resolve("d2");
	private Path copyTargetf1 = copyDirTarget.resolve("file1.txt");
	private Path copyTargetf2 = copyDir2Target.resolve("file2.txt");
	private Path copyDirAddPath = copyDirTarget.resolve("addfile.txt");

	private Path extraPath = copyDirTarget.resolve("extra.txt");

	@Override
	protected Map<String, ?> getTaskVariables() {
		Map<String, Object> result = new TreeMap<>();
		result.put("testing.location.source", copySource.toString());
		result.put("testing.location.target", copyDirTarget.toString());
		return result;
	}

	@Override
	protected void runTestImpl() throws Throwable {
		LocalFileProvider localfp = LocalFileProvider.getInstance();
		localfp.clearDirectoryRecursively(buildDir);
		localfp.createDirectories(copySourcef1.getParent());
		localfp.createDirectories(copySourcef2.getParent());

		localfp.delete(copyDirTarget);
		localfp.writeToFile(new UnsyncByteArrayInputStream("f1".getBytes()), copySourcef1);
		localfp.writeToFile(new UnsyncByteArrayInputStream("f2".getBytes()), copySourcef2);

		localfp.createDirectories(extraPath.getParent());
		localfp.writeToFile(new UnsyncByteArrayInputStream("extra".getBytes()), extraPath);

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

		localfp.writeToFile(new UnsyncByteArrayInputStream("f1mod".getBytes()), copySourcef1);
		res = runScriptTask("build");
		assertEquals(localfp.getAllBytes(copyTargetf1).toString(), "f1mod");
		assertEquals(localfp.getAllBytes(copyTargetf2).toString(), "f2");
		assertEquals(localfp.getAllBytes(extraPath).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copyDir2Target, copyTargetf1, copyTargetf2));
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyDirTarget);

		localfp.writeToFile((InputStream) new UnsyncByteArrayInputStream("xyz".getBytes()), copyTargetf1);
		res = runScriptTask("build");
		assertEquals(localfp.getAllBytes(copyTargetf1).toString(), "f1mod");
		assertEquals(localfp.getAllBytes(copyTargetf2).toString(), "f2");
		assertEquals(localfp.getAllBytes(extraPath).toString(), "extra");
		assertEquals(CopyTaskTestUtils.getLocalPathCopiedFiles(res.getTargetTaskResult("copy")),
				setOf(copyDir2Target, copyTargetf1, copyTargetf2));
		assertEquals(CopyTaskTestUtils.getLocalTargetPath(res.getTargetTaskResult("copy")), copyDirTarget);

		localfp.writeToFile(new UnsyncByteArrayInputStream("123".getBytes()), copyAddPath);
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
