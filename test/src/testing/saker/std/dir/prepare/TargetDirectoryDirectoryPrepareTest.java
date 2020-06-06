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

import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class TargetDirectoryDirectoryPrepareTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		runScriptTask("build");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/tdir/f1.txt")).toString(),
				"f1");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/tdir/f2.txt")).toString(),
				"f2");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(PATH_WORKING_DIRECTORY.resolve("f1.txt"), "f1mod");
		runScriptTask("build");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/tdir/f1.txt")).toString(),
				"f1mod");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/tdir/f2.txt")).toString(),
				"f2");

		files.putFile(PATH_WORKING_DIRECTORY.resolve("f3.txt"), "f3");
		runScriptTask("build");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/tdir/f1.txt")).toString(),
				"f1mod");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/tdir/f2.txt")).toString(),
				"f2");
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/tdir/f3.txt")).toString(),
				"f3");
	}

}
