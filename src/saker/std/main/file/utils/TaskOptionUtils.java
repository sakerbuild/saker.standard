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
package saker.std.main.file.utils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Objects;

import saker.build.exception.InvalidPathFormatException;
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
import saker.std.api.file.location.FileLocationVisitor;
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

	public static void visitFileLocation(FileLocationTaskOption taskoption, TaskContext taskcontext,
			FileLocationVisitor visitor) throws NullPointerException {
		taskoption.accept(new FileLocationTaskOption.Visitor() {
			@Override
			public void visitRelativePath(SakerPath path) {
				ExecutionFileLocation.create(taskcontext.getTaskWorkingDirectoryPath().resolve(path)).accept(visitor);
			}

			@Override
			public void visitFileLocation(FileLocation location) {
				location.accept(visitor);
			}
		});
	}

	public static Collection<FileLocation> toFileLocations(MultiFileLocationTaskOption taskoption,
			TaskContext taskcontext, Object wildcarddependencytag) {
		if (taskoption == null) {
			return null;
		}
		LinkedHashSet<FileLocation> result = new LinkedHashSet<>();
		toFileLocationsImpl(taskoption, taskcontext, wildcarddependencytag, result);
		return result;
	}

	//since 0.8.4
	public static Collection<FileLocation> toFileLocations(Iterable<? extends MultiFileLocationTaskOption> taskoption,
			TaskContext taskcontext, Object wildcarddependencytag) {
		if (taskoption == null) {
			return null;
		}
		LinkedHashSet<FileLocation> result = new LinkedHashSet<>();
		for (MultiFileLocationTaskOption to : taskoption) {
			toFileLocationsImpl(to, taskcontext, wildcarddependencytag, result);
		}
		return result;
	}

	private static void toFileLocationsImpl(MultiFileLocationTaskOption taskoption, TaskContext taskcontext,
			Object wildcarddependencytag, LinkedHashSet<FileLocation> result) {
		if (taskoption == null) {
			return;
		}
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
	}

	public static void requireForwardRelativePathWithFileName(SakerPath path, String message) {
		if (message == null) {
			message = "path";
		}
		Objects.requireNonNull(path, message);
		if (!path.isForwardRelative()) {
			throw new InvalidPathFormatException(message + " must be forward relative: " + path);
		}
		if (path.getFileName() == null) {
			throw new InvalidPathFormatException(message + " must have a file name: " + path);
		}
	}
}
