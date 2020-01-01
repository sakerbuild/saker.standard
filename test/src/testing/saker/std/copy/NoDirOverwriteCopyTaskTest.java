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
package testing.saker.std.copy;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class NoDirOverwriteCopyTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	private Path copySource = getTestingBaseBuildDirectory()
			.resolve(getClass().getName().replace('.', '/')).resolve("copysource.txt");
	private Path copyTarget = getTestingBaseBuildDirectory()
			.resolve(getClass().getName().replace('.', '/')).resolve("dir");

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

		localfp.createDirectories(copySource.getParent());
		localfp.createDirectories(copyTarget);
		localfp.writeToFile(new UnsyncByteArrayInputStream("hello".getBytes()), copySource);

		SakerPath filetxtpath = PATH_WORKING_DIRECTORY.resolve("file.txt");
		SakerPath dirpath = PATH_WORKING_DIRECTORY.resolve("dir");
		files.putFile(filetxtpath, "hello");

		assertTaskException(RuntimeException.class, () -> runScriptTask("betweenexec"));
		assertTrue(files.getFileAttributes(dirpath).isDirectory());

		assertTaskException(RuntimeException.class, () -> runScriptTask("fromlocal"));
		assertTrue(files.getFileAttributes(dirpath).isDirectory());

		assertTaskException(RuntimeException.class, () -> runScriptTask("tolocal"));
		assertTrue(localfp.getFileAttributes(copyTarget).isDirectory());
		
		assertTaskException(RuntimeException.class, () -> runScriptTask("betweenlocal"));
		assertTrue(localfp.getFileAttributes(copyTarget).isDirectory());
	}
}
