package testing.saker.std.mirror;

import java.io.FileNotFoundException;
import java.nio.file.Path;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class MirrorTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	@Override
	protected void runTestImpl() throws Throwable {
		LocalFileProvider localfp = LocalFileProvider.getInstance();
		SakerPath mirrorexecpath = PATH_WORKING_DIRECTORY.resolve("a.txt");
		Path mirrorlocalpath = getBuildDirectory().resolve("mirror/wd_/a.txt");

		//delete at start of test to have a clean state
		localfp.delete(mirrorlocalpath);

		files.putFile(mirrorexecpath, "a");

		CombinedTargetTaskResult res = runScriptTask("build");
		assertEquals(res.getTargetTaskResult("mirror"), SakerPath.valueOf(mirrorlocalpath));
		assertEquals(localfp.getAllBytes(mirrorlocalpath).toString(), "a");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(mirrorexecpath, "amod");
		runScriptTask("build");
		assertEquals(localfp.getAllBytes(mirrorlocalpath).toString(), "amod");

		localfp.delete(mirrorlocalpath);
		runScriptTask("build");
		assertEquals(localfp.getAllBytes(mirrorlocalpath).toString(), "amod");

		files.delete(mirrorexecpath);
		assertTaskException(FileNotFoundException.class, () -> runScriptTask("build"));

		//test clean aborting
		assertTaskException(FileNotFoundException.class, () -> runScriptTask("build"));
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(mirrorexecpath, "a2");
		runScriptTask("build");
		assertEquals(localfp.getAllBytes(mirrorlocalpath).toString(), "a2");
	}
}
