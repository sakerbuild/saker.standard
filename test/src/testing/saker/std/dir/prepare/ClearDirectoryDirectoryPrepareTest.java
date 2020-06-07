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
public class ClearDirectoryDirectoryPrepareTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		runScriptTask("build");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f1.txt")).toString(),
				"f1");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f2.txt")).toString(),
				"f2");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f3.txt"), "added");
		runScriptTask("build");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f1.txt")).toString(),
				"f1");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f2.txt")).toString(),
				"f2");
		assertException(IOException.class,
				() -> files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f3.txt")));

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
	}

}
