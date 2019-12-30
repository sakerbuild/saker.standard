package saker.std.main.file.utils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.NavigableMap;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities;
import saker.build.task.dependencies.FileCollectionStrategy;
import saker.build.task.utils.dependencies.WildcardFileCollectionStrategy;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.main.file.option.FileLocationTaskOption;
import saker.std.main.file.option.MultiFileLocationTaskOption;

public class TaskOptionUtils {
	private TaskOptionUtils() {
		throw new UnsupportedOperationException();
	}

	public static FileLocation toFileLocation(FileLocationTaskOption taskoption, TaskContext taskcontext) {
		if (taskoption == null) {
			return null;
		}
		FileLocation[] result = { null };
		taskoption.accept(new FileLocationTaskOption.Visitor() {
			@Override
			public void visitRelativePath(SakerPath path) {
				result[0] = ExecutionFileLocation.create(taskcontext.getTaskWorkingDirectoryPath().resolve(path));
			}

			@Override
			public void visitFileLocation(FileLocation location) {
				result[0] = location;
			}
		});
		return result[0];
	}

	public static Collection<FileLocation> toFileLocations(MultiFileLocationTaskOption taskoption,
			TaskContext taskcontext, Object wildcarddependencytag) {
		LinkedHashSet<FileLocation> result = new LinkedHashSet<>();
		taskoption.accept(new MultiFileLocationTaskOption.Visitor() {

			@Override
			public void visitWildcard(WildcardPath path) {
				FileCollectionStrategy collectionstrategy = WildcardFileCollectionStrategy.create(path);
				TaskExecutionUtilities taskutils = taskcontext.getTaskUtilities();

				NavigableMap<SakerPath, SakerFile> files = taskutils
						.collectFilesReportAdditionDependency(wildcarddependencytag, collectionstrategy);
				taskutils.reportInputFileDependency(wildcarddependencytag,
						ObjectUtils.singleValueMap(files.navigableKeySet(), CommonTaskContentDescriptors.PRESENT));
				for (SakerPath filepath : files.navigableKeySet()) {
					result.add(ExecutionFileLocation.create(filepath));
				}
			}

			@Override
			public void visitRelativePath(SakerPath path) {
				result.add(ExecutionFileLocation.create(taskcontext.getTaskWorkingDirectoryPath().resolve(path)));
			}

			@Override
			public void visitFileLocations(Collection<? extends FileLocation> locations) {
				result.addAll(locations);
			}
		});
		return result;
	}
}