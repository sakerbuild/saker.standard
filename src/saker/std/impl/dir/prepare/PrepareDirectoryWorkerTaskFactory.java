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
package saker.std.impl.dir.prepare;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import saker.build.file.DelegateSakerFile;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.DirectoryContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities;
import saker.build.task.TaskFactory;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.task.utils.dependencies.RecursiveFileCollectionStrategy;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.trace.BuildTrace;
import saker.std.api.dir.prepare.PrepareDirectoryWorkerTaskOutput;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.api.util.SakerStandardUtils;

public class PrepareDirectoryWorkerTaskFactory implements TaskFactory<PrepareDirectoryWorkerTaskOutput>,
		Task<PrepareDirectoryWorkerTaskOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	private NavigableMap<SakerPath, FileLocation> inputs;
	private boolean clearDirectory;

	/**
	 * For {@link Externalizable}.
	 */
	public PrepareDirectoryWorkerTaskFactory() {
	}

	public PrepareDirectoryWorkerTaskFactory(NavigableMap<SakerPath, FileLocation> inputs, boolean clearDirectory) {
		this.inputs = inputs;
		this.clearDirectory = clearDirectory;
	}

	@Override
	public PrepareDirectoryWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		PrepareDirectoryWorkerTaskIdentifier taskid = (PrepareDirectoryWorkerTaskIdentifier) taskcontext.getTaskId();
		SakerPath outpath = taskid.getOutputPath();
		String stdoutid;
		if (outpath.getNameCount() == 1) {
			stdoutid = outpath.getFileName();
		} else {
			stdoutid = outpath.getName(0) + ":" + outpath.getFileName();
		}
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
			BuildTrace.setDisplayInformation("prepare:" + outpath.getFileName(), stdoutid);
		}
		taskcontext.setStandardOutDisplayIdentifier(stdoutid);

		TaskExecutionUtilities taskutils = taskcontext.getTaskUtilities();
		SakerDirectory outputdir = taskutils
				.resolveDirectoryAtPathCreate(SakerPathFiles.requireBuildDirectory(taskcontext), outpath);

		SakerPath outputdirpath = outputdir.getSakerPath();

		if (clearDirectory) {
			outputdir.clear();
		} else {
			NavigableMap<SakerPath, ? extends SakerFile> prevoutputdeps = SakerPathFiles.getPathSubMapDirectoryChildren(
					taskcontext.getPreviousOutputDependencies(null), outputdirpath, false);
			if (!ObjectUtils.isNullOrEmpty(prevoutputdeps)) {
				//remove previous outputs that are under the output directory
				List<SakerDirectory> dirs = new ArrayList<>();
				for (Entry<SakerPath, ? extends SakerFile> entry : prevoutputdeps.entrySet()) {
					SakerFile f = entry.getValue();
					if (f == null) {
						//already removed
						continue;
					}
					if (f instanceof SakerDirectory) {
						//we may need to keep the directory if there's any files placed in it.
						dirs.add((SakerDirectory) f);
					} else {
						f.remove();
					}
				}
				for (SakerDirectory d : dirs) {
					if (d.isEmpty()) {
						d.remove();
					} // else keep it as some other agent placed a file in it
				}
			}
		}

		NavigableMap<SakerPath, ContentDescriptor> inputfilecontents = new TreeMap<>();
		NavigableMap<SakerPath, ContentDescriptor> outputfilecontents = new TreeMap<>();

		outputfilecontents.put(outputdirpath, DirectoryContentDescriptor.INSTANCE);

		NavigableSet<SakerPath> outputfilepaths = new TreeSet<>();

		UUID taskuuid = UUID.randomUUID();

		//TODO perform iteration more efficiently, without too many path resolutions
		for (Entry<SakerPath, FileLocation> entry : inputs.entrySet()) {
			SakerPath entrypath = entry.getKey();
			String entryfilename = entrypath.getFileName();
			SakerDirectory parentdir = outputdir;
			{
				SakerPath currentpath = outputdirpath;
				ListIterator<String> it = entrypath.nameIterator();
				while (true) {
					String n = it.next();
					if (!it.hasNext()) {
						break;
					}
					parentdir = parentdir.getDirectoryCreate(n);
					currentpath = currentpath.resolve(n);
					ContentDescriptor prev = outputfilecontents.putIfAbsent(currentpath,
							DirectoryContentDescriptor.INSTANCE);
					if (prev != null && !DirectoryContentDescriptor.INSTANCE.equals(prev)) {
						throw new IllegalArgumentException("Conflicting output files at: " + currentpath
								+ " with contents: " + DirectoryContentDescriptor.INSTANCE + " and " + prev);
					}
				}
			}
			SakerDirectory fparentdir = parentdir;
			SakerPath entryoutpath = outputdirpath.resolve(entrypath);
			outputfilepaths.add(entryoutpath);
			entry.getValue().accept(new FileLocationVisitor() {
				@Override
				public void visit(LocalFileLocation loc) {
					SakerPath localpath = loc.getLocalPath();
					ContentDescriptor cd = taskutils.getReportExecutionDependency(
							SakerStandardUtils.createLocalFileContentDescriptorExecutionProperty(localpath, taskuuid));
					if (cd == null) {
						throw ObjectUtils.sneakyThrow(new NoSuchFileException(localpath.toString()));
					}
					if (cd instanceof DirectoryContentDescriptor) {
						fparentdir.getDirectoryCreate(entryfilename);
					} else {
						try {
							SakerFile newfile = taskutils.createProviderPathFile(entryfilename,
									LocalFileProvider.getInstance().getPathKey(localpath));
							if (newfile instanceof SakerDirectory) {
								//should not happen, but better check this
								throw new ConcurrentModificationException(
										"File hierarchy was concurrently modified at " + loc);
							}
							fparentdir.add(newfile);
						} catch (Exception e) {
							throw ObjectUtils.sneakyThrow(e);
						}
					}
					outputfilecontents.put(entryoutpath, cd);
				}

				@Override
				public void visit(ExecutionFileLocation loc) {
					SakerPath path = loc.getPath();
					SakerFile f = taskutils.resolveAtPath(path);
					if (f == null) {
						throw ObjectUtils.sneakyThrow(new NoSuchFileException(path.toString()));
					}
					ContentDescriptor cd = f.getContentDescriptor();
					inputfilecontents.put(path, cd);
					if (f instanceof SakerDirectory) {
						fparentdir.getDirectoryCreate(entryfilename);
					} else {
						fparentdir.add(new DelegateSakerFile(entryfilename, f));
					}
					outputfilecontents.put(entryoutpath, cd);
				}
			});
		}
		taskutils.reportInputFileDependency(null, inputfilecontents);
		if (clearDirectory) {
			//use the input dependency reporting to ensure that we re-run if any files are added to the output directory 
			taskcontext.reportInputFileAdditionDependency(null, RecursiveFileCollectionStrategy.create(outputdirpath));
			taskutils.reportInputFileDependency(null, outputfilecontents);
		} else {
			taskutils.reportOutputFileDependency(null, outputfilecontents);
		}
		outputdir.synchronize();

		PrepareDirectoryWorkerTaskOutputImpl result = new PrepareDirectoryWorkerTaskOutputImpl(outputdirpath,
				outputfilepaths);
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
		return result;
	}

	@Override
	public Task<? extends PrepareDirectoryWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalMap(out, inputs);
		out.writeBoolean(clearDirectory);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		inputs = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		clearDirectory = in.readBoolean();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
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
		PrepareDirectoryWorkerTaskFactory other = (PrepareDirectoryWorkerTaskFactory) obj;
		if (clearDirectory != other.clearDirectory)
			return false;
		if (inputs == null) {
			if (other.inputs != null)
				return false;
		} else if (!inputs.equals(other.inputs))
			return false;
		return true;
	}

}
