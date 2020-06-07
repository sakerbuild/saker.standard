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

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class WildcardBaseDirectoryPrepareTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		runScriptTask("build");
		assertException(IOException.class,
				() -> files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f1.txt")));
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f2.txt")).toString(),
				"f2");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(PATH_WORKING_DIRECTORY.resolve("f1.txt"), "f1mod");
		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertException(IOException.class,
				() -> files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f1.txt")));
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f2.txt")).toString(),
				"f2");

		SakerPath f3path = PATH_WORKING_DIRECTORY.resolve("dir/subdir/f3.txt");
		files.putFile(f3path, "f3");
		runScriptTask("build");
		assertException(IOException.class,
				() -> files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f1.txt")));
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f2.txt")).toString(),
				"f2");
		assertEquals(
				files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/subdir/f3.txt")).toString(),
				"f3");

		//test prev file removal with directory
		files.delete(f3path);
		runScriptTask("build");
		assertException(IOException.class,
				() -> files.getFileAttributes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f1.txt")));
		assertEquals(files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/f2.txt")).toString(),
				"f2");
		assertException(IOException.class,
				() -> files.getFileAttributes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/subdir/f3.txt")));
		assertException(IOException.class,
				() -> files.getFileAttributes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/subdir")));

		//put back
		files.putFile(f3path, "f3");
		runScriptTask("build");

		//add my own file
		files.putFile(f3path.resolveSibling("myfile.txt"), "myf");
		runScriptTask("build");
		assertEquals(
				files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/subdir/f3.txt")).toString(),
				"f3");
		assertEquals(
				files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/subdir/myfile.txt")).toString(),
				"myf");

		//remove f3 and check that our file is still there
		//that is, the parent directory haven't been removed althought the output file in it was deleted
		files.delete(f3path);
		runScriptTask("build");
		assertException(IOException.class,
				() -> files.getFileAttributes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/subdir/f3.txt")));
		assertEquals(
				files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("std.dir.prepare/default/subdir/myfile.txt")).toString(),
				"myf");
	}

}
