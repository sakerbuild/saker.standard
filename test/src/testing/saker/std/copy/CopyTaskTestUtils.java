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

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.ReflectUtils;

public class CopyTaskTestUtils {
	private CopyTaskTestUtils() {
		throw new UnsupportedOperationException();
	}

	public static Set<SakerPath> getExecutionPathCopiedFiles(Object taskresult) throws Exception {
		Set<SakerPath> result = new TreeSet<>();
		for (Object o : (Iterable<?>) ReflectUtils.getMethodAssert(taskresult.getClass(), "getCopiedFiles")
				.invoke(taskresult)) {
			SakerPath path = (SakerPath) getExecutionGetPathMethod(o).invoke(o);
			result.add(path);
		}
		return result;
	}

	public static SakerPath getExecutionTargetPath(Object taskresult) throws Exception {
		Object target = ReflectUtils.getMethodAssert(taskresult.getClass(), "getTarget").invoke(taskresult);
		return (SakerPath) getExecutionGetPathMethod(target).invoke(target);
	}

	public static Set<Path> getLocalPathCopiedFiles(Object taskresult) throws Exception {
		Set<Path> result = new TreeSet<>();
		for (Object o : (Iterable<?>) ReflectUtils.getMethodAssert(taskresult.getClass(), "getCopiedFiles")
				.invoke(taskresult)) {
			Path path = LocalFileProvider.toRealPath((SakerPath) getLocalGetLocalPathMethod(o).invoke(o));
			result.add(path);
		}
		return result;
	}

	public static Path getLocalTargetPath(Object taskresult) throws Exception {
		Object target = ReflectUtils.getMethodAssert(taskresult.getClass(), "getTarget").invoke(taskresult);
		return LocalFileProvider.toRealPath((SakerPath) getLocalGetLocalPathMethod(target).invoke(target));
	}

	private static Method getExecutionGetPathMethod(Object target) throws AssertionError {
		Method getpathmethod = ReflectUtils.getMethodAssert(ReflectUtils.findInterfaceWithNameInHierarchy(
				target.getClass(), "saker.std.api.file.location.ExecutionFileLocation"), "getPath");
		return getpathmethod;
	}

	private static Method getLocalGetLocalPathMethod(Object target) throws AssertionError {
		Method getpathmethod = ReflectUtils.getMethodAssert(ReflectUtils.findInterfaceWithNameInHierarchy(
				target.getClass(), "saker.std.api.file.location.LocalFileLocation"), "getLocalPath");
		return getpathmethod;
	}
}
