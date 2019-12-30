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
