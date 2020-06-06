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
public class WildcardBaseRemapTargetDirDirectoryPrepareTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		runScriptTask("build");
		assertException(IOException.class,
				() -> files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/tdir/1/f1.txt")));
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/tdir/2/f.txt")).toString(),
				"f2");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(PATH_WORKING_DIRECTORY.resolve("f1.txt"), "f1mod");
		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertException(IOException.class,
				() -> files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/tdir/1/f.txt")));
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/tdir/2/f.txt")).toString(),
				"f2");

		files.putFile(PATH_WORKING_DIRECTORY.resolve("dir/subdir/f3.txt"), "f3");
		runScriptTask("build");
		assertException(IOException.class,
				() -> files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/tdir/1/f.txt")));
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/tdir/2/f.txt")).toString(),
				"f2");
		assertEquals(
				files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/tdir/3/subdir/f.txt")).toString(),
				"f3");
	}

}
