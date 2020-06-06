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
package saker.std.api.util;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.std.api.dir.prepare.PrepareDirectoryWorkerTaskOutput;
import saker.std.api.file.location.FileLocation;
import saker.std.impl.dir.prepare.PrepareDirectoryWorkerTaskFactory;
import saker.std.impl.dir.prepare.PrepareDirectoryWorkerTaskIdentifier;
import saker.std.impl.file.mirror.MirroringTaskFactory;

/**
 * Utility class providing access to build tasks in the saker.standard package.
 * 
 * @since saker.standard 0.8.3
 */
public class SakerStandardTaskUtils {
	private SakerStandardTaskUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates a task that performs {@linkplain TaskContext#mirror(SakerFile) mirroring} for the argument path.
	 * <p>
	 * The task should be started with the task identifier retrieved from
	 * {@link #createMirroringTaskIdentifier(SakerPath)}.
	 * 
	 * @param executionPath
	 *            The absolute execution path of the file or directory to mirror.
	 * @return The task factory.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @throws InvalidPathFormatException
	 *             If the argument is not absolute.
	 */
	public static TaskFactory<? extends SakerPath> createMirroringTaskFactory(SakerPath executionPath)
			throws NullPointerException, InvalidPathFormatException {
		SakerPathFiles.requireAbsolutePath(executionPath);
		return new MirroringTaskFactory(executionPath);
	}

	/**
	 * Creates a task identifier for the {@linkplain #createMirroringTaskFactory(SakerPath) mirroring task}.
	 * 
	 * @param executionPath
	 *            The absolute execution path of the file or directory to mirror. This should be the same as the path
	 *            passed to {@link #createMirroringTaskFactory(SakerPath)}.
	 * @return The task factory.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @throws InvalidPathFormatException
	 *             If the argument is not absolute.
	 */
	public static TaskIdentifier createMirroringTaskIdentifier(SakerPath executionPath)
			throws NullPointerException, InvalidPathFormatException {
		SakerPathFiles.requireAbsolutePath(executionPath);
		return new MirroringTaskFactory(executionPath);
	}

	/**
	 * Creates a prepare directory worker task for the specified inputs.
	 * <p>
	 * The task should be started with the task identifier created using
	 * {@link #createPrepareDirectoryTaskIdentifier(SakerPath)}.
	 * 
	 * @param inputs
	 *            The forward relative output paths mapped to their input file locations.
	 * @return The worker task factory.
	 * @throws NullPointerException
	 *             If the argument or any of the elements are <code>null</code>.
	 * @throws InvalidPathFormatException
	 *             If an output path is not forward relative or has no file name.
	 * @since saker.standard 0.8.4
	 */
	public static TaskFactory<? extends PrepareDirectoryWorkerTaskOutput> createPrepareDirectoryTaskFactory(
			NavigableMap<SakerPath, ? extends FileLocation> inputs)
			throws NullPointerException, InvalidPathFormatException {
		Objects.requireNonNull(inputs, "inputs");
		NavigableMap<SakerPath, FileLocation> inputsmap = ImmutableUtils.makeImmutableNavigableMap(inputs);
		for (Entry<SakerPath, FileLocation> entry : inputsmap.entrySet()) {
			SakerPath opath = entry.getKey();
			Objects.requireNonNull(opath, "output path");
			Objects.requireNonNull(entry.getValue(), "input location");
			if (!opath.isForwardRelative()) {
				throw new InvalidPathFormatException("Entry output path must be forward relative: " + opath);
			}
			if (opath.getFileName() == null) {
				throw new InvalidPathFormatException("Entry output path must have a file name: " + opath);
			}
		}
		return new PrepareDirectoryWorkerTaskFactory(inputsmap);
	}

	/**
	 * Creates a task identifier for the {@linkplain #createPrepareDirectoryTaskFactory(NavigableMap) directory prepare
	 * worker task}.
	 * 
	 * @param outputpath
	 *            The forward relative output path.
	 * @return The task identifier.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @throws InvalidPathFormatException
	 *             If the output path is not forward relative or has no file name.
	 * @since saker.standard 0.8.4
	 */
	public static TaskIdentifier createPrepareDirectoryTaskIdentifier(SakerPath outputpath)
			throws NullPointerException, InvalidPathFormatException {
		Objects.requireNonNull(outputpath, "output path");
		if (!outputpath.isForwardRelative()) {
			throw new InvalidPathFormatException("Output path must be forward relative: " + outputpath);
		}
		if (outputpath.getFileName() == null) {
			throw new InvalidPathFormatException("Output path must have a file name: " + outputpath);
		}
		return new PrepareDirectoryWorkerTaskIdentifier(outputpath);
	}
}
