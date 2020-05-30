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
package saker.std.impl.file.mirror;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.NavigableMap;

import saker.build.exception.FileMirroringUnavailableException;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.task.utils.dependencies.RecursiveFileCollectionStrategy;
import saker.build.trace.BuildTrace;
import saker.build.util.file.FixedDirectoryVisitPredicate;
import saker.std.main.file.mirror.MirrorFileTaskFactory;

public class MirroringTaskFactory implements TaskFactory<SakerPath>, Task<SakerPath>, Externalizable, TaskIdentifier {
	private static final long serialVersionUID = 1L;

	private SakerPath path;

	/**
	 * For {@link Externalizable}.
	 */
	public MirroringTaskFactory() {
	}

	public MirroringTaskFactory(SakerPath path) {
		this.path = path;
	}

	@Override
	public SakerPath run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
			StringBuilder btlabel = new StringBuilder();
			btlabel.append(MirrorFileTaskFactory.TASK_NAME);
			btlabel.append(": ");
			String fn = path.getFileName();
			if (fn != null) {
				btlabel.append(fn);
			} else {
				String root = path.getRoot();
				if (root != null) {
					btlabel.append(root);
				} else {
					btlabel.append(path);
				}
			}
			BuildTrace.setDisplayInformation(MirrorFileTaskFactory.TASK_NAME, btlabel.toString());
		}

		TaskExecutionUtilities taskutils = taskcontext.getTaskUtilities();
		NavigableMap<SakerPath, SakerFile> dirinputfiles = taskutils
				.collectFilesReportInputFileAndAdditionDependency(null, RecursiveFileCollectionStrategy.create(path));

		final SakerPath result;
		TaskIdentifier taskid = taskcontext.getTaskId();
		if (!dirinputfiles.isEmpty()) {
			//the specified file to mirror is a directory
			//perform the mirroring
			result = SakerPath.valueOf(taskcontext.mirror(dirinputfiles.get(path), new FixedDirectoryVisitPredicate(
					SakerPathFiles.relativizeSubPath(dirinputfiles.navigableKeySet(), path))));
			//TODO this should be done in bulk
			for (Entry<SakerPath, SakerFile> entry : dirinputfiles.entrySet()) {
				MirrorPathContentExecutionProperty mirrorexecproperty = new MirrorPathContentExecutionProperty(taskid,
						entry.getKey());
				taskcontext.reportExecutionDependency(mirrorexecproperty, entry.getValue().getContentDescriptor());
			}
		} else {
			SakerFile file = taskutils.resolveAtPath(path);
			if (file == null) {
				taskcontext.reportInputFileDependency(null, path, CommonTaskContentDescriptors.NOT_PRESENT);
				taskcontext.abortExecution(new FileNotFoundException("File not found at path: " + path));
				return null;
			}
			ContentDescriptor filecontents = file.getContentDescriptor();
			taskcontext.reportInputFileDependency(null, path, filecontents);
			result = SakerPath.valueOf(taskcontext.mirror(file));

			MirrorPathContentExecutionProperty mirrorexecproperty = new MirrorPathContentExecutionProperty(taskid,
					path);
			taskcontext.reportExecutionDependency(mirrorexecproperty, filecontents);
		}
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
		return result;
	}

	@Override
	public Task<? extends SakerPath> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(path);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		path = (SakerPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MirroringTaskFactory other = (MirroringTaskFactory) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[path=" + path + "]";
	}

	private static class MirrorPathContentExecutionProperty
			implements ExecutionProperty<ContentDescriptor>, Externalizable {
		private static final long serialVersionUID = 1L;

		private TaskIdentifier associatedTask;
		private SakerPath execPath;

		/**
		 * For {@link Externalizable}.
		 */
		public MirrorPathContentExecutionProperty() {
		}

		public MirrorPathContentExecutionProperty(TaskIdentifier associatedTask, SakerPath execPath) {
			this.associatedTask = associatedTask;
			this.execPath = execPath;
		}

		@Override
		public ContentDescriptor getCurrentValue(ExecutionContext executioncontext) {
			try {
				Path mirrorpath = executioncontext.toMirrorPath(execPath);
				return executioncontext.getContentDescriptor(LocalFileProvider.getInstance().getPathKey(mirrorpath));
			} catch (FileMirroringUnavailableException e) {
				return null;
			}
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(associatedTask);
			out.writeObject(execPath);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			associatedTask = (TaskIdentifier) in.readObject();
			execPath = (SakerPath) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((associatedTask == null) ? 0 : associatedTask.hashCode());
			result = prime * result + ((execPath == null) ? 0 : execPath.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MirroringTaskFactory.MirrorPathContentExecutionProperty other = (MirroringTaskFactory.MirrorPathContentExecutionProperty) obj;
			if (associatedTask == null) {
				if (other.associatedTask != null)
					return false;
			} else if (!associatedTask.equals(other.associatedTask))
				return false;
			if (execPath == null) {
				if (other.execPath != null)
					return false;
			} else if (!execPath.equals(other.execPath))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + execPath + "]";
		}
	}
}