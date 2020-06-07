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
package testing.saker.std.dir.prepare;

import java.io.IOException;

import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class SimpleDirectoryPrepareTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		runScriptTask("build");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f1.txt")).toString(),
				"f1");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f2.txt")).toString(),
				"f2");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f2.txt"), "outmod");
		runScriptTask("build");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f1.txt")).toString(),
				"f1");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f2.txt")).toString(),
				"f2");

		files.delete(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f2.txt"));
		runScriptTask("build");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f1.txt")).toString(),
				"f1");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f2.txt")).toString(),
				"f2");

		files.putFile(PATH_WORKING_DIRECTORY.resolve("f1.txt"), "f1mod");
		runScriptTask("build");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f1.txt")).toString(),
				"f1mod");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f2.txt")).toString(),
				"f2");

		files.putFile(PATH_WORKING_DIRECTORY.resolve("f3.txt"), "f3");
		runScriptTask("build");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f1.txt")).toString(),
				"f1mod");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f2.txt")).toString(),
				"f2");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f3.txt")).toString(),
				"f3");

		files.delete(PATH_WORKING_DIRECTORY.resolve("f3.txt"));
		runScriptTask("build");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f1.txt")).toString(),
				"f1mod");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f2.txt")).toString(),
				"f2");
		assertException(IOException.class,
				() -> files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f3.txt")));

		//The task should NOT re-run if some file was added to the output directory
		//this is so the developer can test the app and use temporary files without the build
		//system automatically deleting them in no-op builds
		//even if the task is rerun, only files which were added by the prepare task
		//should be deleted. other files which are added by other tasks or the developer should be kept
		files.putFile(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/fxxx.txt"), "xxx");
		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/fxxx.txt")).toString(),
				"xxx");

		files.putFile(PATH_WORKING_DIRECTORY.resolve("f1.txt"), "f1modagain");
		runScriptTask("build");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f1.txt")).toString(),
				"f1modagain");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f2.txt")).toString(),
				"f2");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/fxxx.txt")).toString(),
				"xxx");
	}

}
