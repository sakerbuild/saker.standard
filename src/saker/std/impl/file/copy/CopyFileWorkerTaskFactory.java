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
package saker.std.impl.file.copy;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;

import saker.build.file.DelegateSakerFile;
import saker.build.file.DirectoryVisitPredicate;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.DirectoryContentDescriptor;
import saker.build.file.path.ProviderHolderPathKey;
import saker.build.file.path.SakerPath;
import saker.build.file.path.SimpleProviderHolderPathKey;
import saker.build.file.path.WildcardPath;
import saker.build.file.path.WildcardPath.ItemLister;
import saker.build.file.provider.LocalFileProvider;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities;
import saker.build.task.TaskFactory;
import saker.build.task.dependencies.FileCollectionStrategy;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.dependencies.RecursiveFileCollectionStrategy;
import saker.build.task.utils.dependencies.WildcardFileCollectionStrategy;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.trace.BuildTrace;
import saker.build.util.file.FixedDirectoryVisitPredicate;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileCollection;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.impl.file.property.LocalDirectoryRecursiveFilePathsExecutionProperty;
import saker.std.impl.file.property.LocalDirectoryWildcardsFilePathsExecutionProperty;
import saker.std.impl.file.property.LocalFileContentDescriptorExecutionProperty;
import saker.std.main.file.copy.CopyFileTaskFactory;

public class CopyFileWorkerTaskFactory implements TaskFactory<Object>, Task<Object>, Externalizable, TaskIdentifier {
	private static final long serialVersionUID = 1L;

	private static final NavigableSet<WildcardPath> ALL_FILES_WILDCARD_SET = ImmutableUtils
			.makeImmutableNavigableSet(new WildcardPath[] { WildcardPath.valueOf("**") });

	private FileLocation sourceLocation;
	private FileLocation targetLocation;
	private NavigableSet<WildcardPath> wildcards;

	/**
	 * For {@link Externalizable}.
	 */
	public CopyFileWorkerTaskFactory() {
	}

	public CopyFileWorkerTaskFactory(FileLocation sourcelocation, FileLocation targetlocation,
			NavigableSet<WildcardPath> wildcards) {
		this.sourceLocation = sourcelocation;
		this.targetLocation = targetlocation;
		this.wildcards = wildcards;
	}

	@Override
	public Task<? extends Object> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public Object run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
		}
		taskcontext.setStandardOutDisplayIdentifier(CopyFileTaskFactory.TASK_NAME);

		Collection<FileLocation> copiedfiles = new LinkedHashSet<>();
		sourceLocation.accept(new FileLocationVisitor() {
			@Override
			public void visit(ExecutionFileLocation loc) {
				try {
					copyFrom(loc, targetLocation, taskcontext, wildcards, copiedfiles);
				} catch (Exception e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}

			@Override
			public void visit(LocalFileLocation loc) {
				try {
					copyFrom(loc, targetLocation, taskcontext, wildcards, copiedfiles);
				} catch (Exception e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}
		});
		return new CopyFileTaskOutputImpl(targetLocation, FileCollection.create(copiedfiles));
	}

	private static void copyFrom(LocalFileLocation loc, FileLocation targetlocation, TaskContext taskcontext,
			NavigableSet<WildcardPath> wildcards, Collection<FileLocation> copiedfiles) throws Exception {
		SakerPath filepath = loc.getLocalPath();
		LocalFileProvider localfp = LocalFileProvider.getInstance();
		ProviderHolderPathKey filepathkey = localfp.getPathKey(filepath);
		TaskExecutionUtilities taskutils = taskcontext.getTaskUtilities();
		targetlocation.accept(new FileLocationVisitor() {
			@Override
			public void visit(ExecutionFileLocation loc) {
				//local to execution
				SakerPath pastefilepath = loc.getPath();
				SakerPath pastedirpath = pastefilepath.getParent();
				SakerDirectory pastedir = taskutils.resolveDirectoryAtPathCreateIfAbsent(pastedirpath);
				if (pastedir == null) {
					throw new RuntimeException("Failed to create copy target directory at: " + pastedirpath);
				}
				try {
					SakerFile createdfile = taskutils.createProviderPathFile(pastefilepath.getFileName(), filepathkey);
					taskcontext.reportExecutionDependency(new LocalFileContentDescriptorExecutionProperty(filepath),
							createdfile.getContentDescriptor());
					if (createdfile instanceof SakerDirectory) {
						SakerDirectory createddir = (SakerDirectory) createdfile;
						SakerDirectory overwritedir = pastedir.addOverwriteIfNotDirectory(createdfile);

						NavigableMap<SakerPath, SakerFile> createddirchildren;
						if (ALL_FILES_WILDCARD_SET.equals(wildcards)) {
							createddirchildren = createddir.getFilesRecursiveByPath(SakerPath.EMPTY,
									DirectoryVisitPredicate.everything());
							taskcontext.reportExecutionDependency(
									new LocalDirectoryRecursiveFilePathsExecutionProperty(filepath),
									ImmutableUtils.makeImmutableNavigableSet(createddirchildren.navigableKeySet()));
						} else {
							NavigableMap<SakerPath, SakerFile> absolutechildren = createddir
									.getFilesRecursiveByPath(filepath, new WildcardsDirectoryVisitPredicate(wildcards));
							createddirchildren = SakerPathFiles.relativizeSubPath(absolutechildren, filepath);
							taskcontext.reportExecutionDependency(
									new LocalDirectoryWildcardsFilePathsExecutionProperty(filepath, wildcards),
									ImmutableUtils.makeImmutableNavigableSet(absolutechildren.navigableKeySet()));
						}
						for (SakerPath pastechildpath : createddirchildren.keySet()) {
							copiedfiles.add(ExecutionFileLocation.create(pastefilepath.append(pastechildpath)));
						}
						//remove the file location for the target path
						copiedfiles.remove(ExecutionFileLocation.create(pastefilepath));

						SakerDirectory syncdir;
						if (overwritedir == null) {
							syncdir = createddir;
							//the created file was added to the directory, possibly overwriting a file
							for (Entry<SakerPath, SakerFile> entry : createddirchildren.entrySet()) {
								ContentDescriptor createdfilecd = entry.getValue().getContentDescriptor();
								SakerPath childrelpath = entry.getKey();
								taskcontext.reportExecutionDependency(
										new LocalFileContentDescriptorExecutionProperty(filepath.resolve(childrelpath)),
										createdfilecd);
								taskcontext.reportOutputFileDependency(null, pastefilepath.resolve(childrelpath),
										createdfilecd);
							}
						} else {
							syncdir = overwritedir;
							//a directory is already present at the given location
							//merge the files
							mergeHierarchy(taskcontext, overwritedir, createddirchildren, 1, filepath, pastefilepath);
							taskutils.reportOutputFileDependency(null, overwritedir);
						}
						//only synchronize the copied children
						syncdir.synchronize(new FixedDirectoryVisitPredicate(createddirchildren.navigableKeySet()));
					} else {
						if (pastedir.addOverwriteIfNotDirectory(createdfile) != null) {
							throw new RuntimeException("Failed to overwrite copy target directory at: " + pastedirpath);
						}
						createdfile.synchronize();
						taskutils.reportOutputFileDependency(null, createdfile);
					}
				} catch (IOException e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}

			private void mergeHierarchy(TaskContext taskcontext, SakerDirectory targetdir,
					NavigableMap<SakerPath, SakerFile> createddirchildren, int depth, SakerPath sourcelocalfilepath,
					SakerPath pastefilepath) {
				//XXX could be more efficient without including delegate files

				for (Entry<SakerPath, SakerFile> entry : createddirchildren.entrySet()) {
					SakerPath entryrelpath = entry.getKey();
					if (entryrelpath.getNameCount() != depth) {
						continue;
					}
					ContentDescriptor filecd = entry.getValue().getContentDescriptor();
					SakerPath pastechildpath = pastefilepath.resolve(entryrelpath);
					SakerPath childlocalpath = sourcelocalfilepath.resolve(entryrelpath);

					SakerFile putfile = entry.getValue();
					if (putfile instanceof SakerDirectory) {
						SakerDirectory overwritedir = targetdir.getDirectoryCreate(putfile.getName());
						//merge the contents for the child file
						mergeHierarchy(taskcontext, overwritedir,
								SakerPathFiles.getPathSubMapDirectoryChildren(createddirchildren, entryrelpath, false),
								depth + 1, sourcelocalfilepath, pastefilepath);
					} else {
						SakerDirectory overwritedir = targetdir
								.addOverwriteIfNotDirectory(new DelegateSakerFile(putfile));
						if (overwritedir != null) {
							throw new RuntimeException(
									"Failed overwrite directory with copied file: " + pastechildpath);
						}
					}

					taskcontext.reportExecutionDependency(
							new LocalFileContentDescriptorExecutionProperty(childlocalpath), filecd);
					taskcontext.reportOutputFileDependency(null, pastechildpath, filecd);
				}
			}

			@Override
			public void visit(LocalFileLocation loc) {
				//local to local
				SakerPath pastefilepath = loc.getLocalPath();
				if (pastefilepath.startsWith(filepath) || filepath.startsWith(pastefilepath)) {
					throw new IllegalArgumentException(
							"Copy source and target paths contain each other: " + filepath + " - " + pastefilepath);
				}
				ProviderHolderPathKey pastepathkey = localfp.getPathKey(pastefilepath);
				try {
					ContentDescriptor filecd = taskutils.synchronize(filepathkey, pastepathkey,
							TaskExecutionUtilities.SYNCHRONIZE_FLAG_NO_OVERWRITE_DIRECTORY);
					taskcontext.reportExecutionDependency(new LocalFileContentDescriptorExecutionProperty(filepath),
							filecd);
					taskcontext.reportExecutionDependency(
							new LocalFileContentDescriptorExecutionProperty(pastefilepath), filecd);

					if (DirectoryContentDescriptor.INSTANCE.equals(filecd)) {
						//we copied a directory. copy the children as well
						NavigableMap<SakerPath, ? extends BasicFileAttributes> children;
						if (ALL_FILES_WILDCARD_SET.equals(wildcards)) {
							children = localfp.getDirectoryEntriesRecursively(filepath);
							taskcontext.reportExecutionDependency(
									new LocalDirectoryRecursiveFilePathsExecutionProperty(filepath),
									ImmutableUtils.makeImmutableNavigableSet(children.navigableKeySet()));
						} else {
							NavigableMap<SakerPath, ? extends BasicFileAttributes> absolutechildren = WildcardPath
									.getItems(wildcards,
											ItemLister.forFileProvider(LocalFileProvider.getInstance(), filepath));
							children = SakerPathFiles.relativizeSubPath(absolutechildren, filepath);
							taskcontext.reportExecutionDependency(
									new LocalDirectoryWildcardsFilePathsExecutionProperty(filepath, wildcards),
									ImmutableUtils.makeImmutableNavigableSet(absolutechildren.navigableKeySet()));
						}
						for (Entry<SakerPath, ? extends BasicFileAttributes> childentry : children.entrySet()) {
							SakerPath childrelpath = childentry.getKey();
							copiedfiles.add(LocalFileLocation.create(pastefilepath.append(childrelpath)));
							SakerPath pastechildpath = pastefilepath.resolve(childrelpath);
							SakerPath childpath = filepath.resolve(childrelpath);
							ContentDescriptor childcopycd = taskutils.synchronize(localfp.getPathKey(childpath),
									new SimpleProviderHolderPathKey(localfp, pastechildpath),
									TaskExecutionUtilities.SYNCHRONIZE_FLAG_NO_OVERWRITE_DIRECTORY);

							taskcontext.reportExecutionDependency(
									new LocalFileContentDescriptorExecutionProperty(childpath), childcopycd);
							taskcontext.reportExecutionDependency(
									new LocalFileContentDescriptorExecutionProperty(pastechildpath), childcopycd);
						}
						//remove the file location for the target path
						copiedfiles.remove(LocalFileLocation.create(pastefilepath));
					}
				} catch (NullPointerException | IOException e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}
		});
	}

	private static void copyFrom(ExecutionFileLocation loc, FileLocation targetlocation, TaskContext taskcontext,
			Collection<WildcardPath> wildcards, Collection<FileLocation> copiedfiles) throws Exception {
		SakerPath filepath = loc.getPath();
		SakerFile file = taskcontext.getTaskUtilities().resolveAtPath(filepath);
		if (file == null) {
			throw new FileNotFoundException("File to copy not found: " + filepath);
		}
		taskcontext.getTaskUtilities().reportInputFileDependency(null, file);
		targetlocation.accept(new FileLocationVisitor() {
			@Override
			public void visit(ExecutionFileLocation loc) {
				SakerPath pastefilepath = loc.getPath();
				if (pastefilepath.startsWith(filepath) || filepath.startsWith(pastefilepath)) {
					throw new IllegalArgumentException(
							"Copy source and target paths contain each other: " + filepath + " - " + pastefilepath);
				}

				copyExecutionFileToExecutionPath(taskcontext, filepath, file, pastefilepath, wildcards, copiedfiles);
			}

			@Override
			public void visit(LocalFileLocation loc) {
				SakerPath pastefilepath = loc.getLocalPath();
				try {
					LocalFileProvider localfp = LocalFileProvider.getInstance();
					//create the directories before the synchronization, so it will fail
					//if a directory cannot be created
					localfp.createDirectories(pastefilepath.getParent());
					ProviderHolderPathKey pathkey = localfp.getPathKey(pastefilepath);
					if (file instanceof SakerDirectory) {
						SakerDirectory dir = (SakerDirectory) file;
						NavigableMap<SakerPath, SakerFile> copyfiles = collectReportDirectoryCopyFiles(taskcontext,
								filepath, wildcards);
						NavigableMap<SakerPath, SakerFile> relativecopyfiles = SakerPathFiles
								.relativizeSubPath(copyfiles, filepath);
						dir.synchronize(pathkey,
								new NonDeletingFixedDirectoryVisitPredicate(relativecopyfiles.navigableKeySet()));
						if (ALL_FILES_WILDCARD_SET.equals(wildcards)) {
							taskcontext.reportInputFileAdditionDependency(null,
									RecursiveFileCollectionStrategy.create(filepath));
						} else {
							for (WildcardPath wc : wildcards) {
								taskcontext.reportInputFileAdditionDependency(null,
										WildcardFileCollectionStrategy.create(filepath, wc));
							}
						}
						for (Entry<SakerPath, SakerFile> entry : relativecopyfiles.entrySet()) {
							SakerPath pastechildpath = pastefilepath.append(entry.getKey());
							copiedfiles.add(LocalFileLocation.create(pastechildpath));
							taskcontext.reportExecutionDependency(
									new LocalFileContentDescriptorExecutionProperty(pastechildpath),
									entry.getValue().getContentDescriptor());
						}
						//remove the file location for the target path
						copiedfiles.remove(LocalFileLocation.create(pastefilepath));
						//XXX report with a path based function
						taskcontext.getTaskUtilities().reportInputFileDependency(null, copyfiles.values());
					} else {
						file.synchronize(pathkey);
						taskcontext.reportExecutionDependency(
								new LocalFileContentDescriptorExecutionProperty(pastefilepath),
								file.getContentDescriptor());
					}
				} catch (IOException e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}
		});
	}

	private static final class NonDeletingFixedDirectoryVisitPredicate extends FixedDirectoryVisitPredicate {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public NonDeletingFixedDirectoryVisitPredicate() {
		}

		NonDeletingFixedDirectoryVisitPredicate(NavigableSet<SakerPath> relativeFiles) {
			super(relativeFiles);
		}

		@Override
		public NavigableSet<String> getSynchronizeFilesToKeep() {
			// don't delete
			return null;
		}
	}

	private static final class WildcardsDirectoryVisitPredicate implements DirectoryVisitPredicate, Externalizable {
		private static final long serialVersionUID = 1L;

		private SakerPath path;
		private NavigableSet<WildcardPath> wildcards;

		/**
		 * For {@link Externalizable}.
		 */
		public WildcardsDirectoryVisitPredicate() {
		}

		public WildcardsDirectoryVisitPredicate(NavigableSet<WildcardPath> wildcards) {
			this(SakerPath.EMPTY, wildcards);
		}

		private WildcardsDirectoryVisitPredicate(SakerPath path, NavigableSet<WildcardPath> wildcards) {
			this.path = path;
			this.wildcards = wildcards;
		}

		@Override
		public DirectoryVisitPredicate directoryVisitor(String name, SakerDirectory directory) {
			SakerPath fpath = this.path.resolve(name);
			for (WildcardPath wc : wildcards) {
				if (wc.finishable(fpath)) {
					return new WildcardsDirectoryVisitPredicate(fpath, wildcards);
				}
			}
			return null;
		}

		@Override
		public boolean visitFile(String name, SakerFile file) {
			SakerPath fpath = this.path.resolve(name);
			for (WildcardPath wc : wildcards) {
				if (wc.includes(fpath)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean visitDirectory(String name, SakerDirectory directory) {
			return visitFile(name, directory);
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(path);
			SerialUtils.writeExternalCollection(out, wildcards);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			path = (SakerPath) in.readObject();
			wildcards = SerialUtils.readExternalSortedImmutableNavigableSet(in);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			result = prime * result + ((wildcards == null) ? 0 : wildcards.hashCode());
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
			WildcardsDirectoryVisitPredicate other = (WildcardsDirectoryVisitPredicate) obj;
			if (path == null) {
				if (other.path != null)
					return false;
			} else if (!path.equals(other.path))
				return false;
			if (wildcards == null) {
				if (other.wildcards != null)
					return false;
			} else if (!wildcards.equals(other.wildcards))
				return false;
			return true;
		}
	}

	private static void copyExecutionFileToExecutionPath(TaskContext taskcontext, SakerPath filepath, SakerFile file,
			SakerPath pastefilepath, Collection<WildcardPath> wildcards, Collection<FileLocation> copiedfiles) {
		SakerPath pasteparentdirpath = pastefilepath.getParent();
		SakerDirectory pastedir = taskcontext.getTaskUtilities()
				.resolveDirectoryAtPathCreateIfAbsent(pasteparentdirpath);

		if (pastedir == null) {
			throw new RuntimeException("Failed to create copy target directory at: " + pasteparentdirpath);
		}
		SakerFile addedfile;
		TaskExecutionUtilities taskutils = taskcontext.getTaskUtilities();
		if (file instanceof SakerDirectory) {
			SakerDirectory targetdir = pastedir.getDirectoryCreateIfAbsent(pastefilepath.getFileName());
			if (targetdir == null) {
				throw new RuntimeException("Failed to copy directory to target path: " + pastefilepath
						+ " (a file with the same name is already present)");
			}
			NavigableMap<SakerPath, SakerFile> copyfiles = collectReportDirectoryCopyFiles(taskcontext, filepath,
					wildcards);
			for (Entry<SakerPath, SakerFile> entry : copyfiles.entrySet()) {
				SakerPath relativecopypath = filepath.relativize(entry.getKey());
				SakerDirectory entryparentdir = taskutils.resolveDirectoryAtRelativePathCreateIfAbsent(targetdir,
						relativecopypath.getParent());
				if (entryparentdir == null) {
					throw new RuntimeException("Failed to copy file to target path: "
							+ pastefilepath.resolve(entry.getKey()) + " (parent directory cannot be created)");
				}
				copiedfiles.add(ExecutionFileLocation.create(pastefilepath.append(relativecopypath)));
				SakerFile copyfile = entry.getValue();
				if (copyfile instanceof SakerDirectory) {
					taskutils.reportOutputFileDependency(null, entryparentdir);
					continue;
				}
				DelegateSakerFile addeddelegatefile = new DelegateSakerFile(copyfile);
				if (entryparentdir.addOverwriteIfNotDirectory(addeddelegatefile) != null) {
					throw new RuntimeException("Failed to copy file to target path: "
							+ pastefilepath.resolve(entry.getKey()) + " (a directory is already present at path)");
				}
				taskutils.reportOutputFileDependency(null, addeddelegatefile);
			}
			//remove the file location for the target path
			copiedfiles.remove(ExecutionFileLocation.create(pastefilepath));
			addedfile = targetdir;
		} else {
			addedfile = new DelegateSakerFile(pastefilepath.getFileName(), file);
			if (pastedir.addOverwriteIfNotDirectory(addedfile) != null) {
				throw new RuntimeException("Failed to copy file to target path: " + pastefilepath
						+ " (a directory with the same name is already present)");
			}
		}
		try {
			addedfile.synchronize();
		} catch (IOException e) {
			throw ObjectUtils.sneakyThrow(e);
		}
		taskutils.reportOutputFileDependency(null, addedfile);
	}

	private static NavigableMap<SakerPath, SakerFile> collectReportDirectoryCopyFiles(TaskContext taskcontext,
			SakerPath filepath, Collection<WildcardPath> wildcards) {
		NavigableMap<SakerPath, SakerFile> copyfiles;
		if (ALL_FILES_WILDCARD_SET.equals(wildcards)) {
			copyfiles = taskcontext.getTaskUtilities().collectFilesReportInputFileAndAdditionDependency(null,
					RecursiveFileCollectionStrategy.create(filepath));
		} else {
			List<FileCollectionStrategy> collectionstrategies = createWildcardCollectionStrategies(filepath, wildcards);
			copyfiles = taskcontext.getTaskUtilities().collectFilesReportInputFileAndAdditionDependency(null,
					collectionstrategies);
		}
		return copyfiles;
	}

	private static List<FileCollectionStrategy> createWildcardCollectionStrategies(SakerPath filepath,
			Collection<WildcardPath> wildcards) {
		List<FileCollectionStrategy> collectionstrategies = new ArrayList<>();
		for (WildcardPath wc : wildcards) {
			collectionstrategies.add(WildcardFileCollectionStrategy.create(filepath, wc));
		}
		return collectionstrategies;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(sourceLocation);
		out.writeObject(targetLocation);
		SerialUtils.writeExternalCollection(out, wildcards);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		sourceLocation = (FileLocation) in.readObject();
		targetLocation = (FileLocation) in.readObject();
		wildcards = SerialUtils.readExternalImmutableNavigableSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceLocation == null) ? 0 : sourceLocation.hashCode());
		result = prime * result + ((targetLocation == null) ? 0 : targetLocation.hashCode());
		result = prime * result + ((wildcards == null) ? 0 : wildcards.hashCode());
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
		CopyFileWorkerTaskFactory other = (CopyFileWorkerTaskFactory) obj;
		if (sourceLocation == null) {
			if (other.sourceLocation != null)
				return false;
		} else if (!sourceLocation.equals(other.sourceLocation))
			return false;
		if (targetLocation == null) {
			if (other.targetLocation != null)
				return false;
		} else if (!targetLocation.equals(other.targetLocation))
			return false;
		if (wildcards == null) {
			if (other.wildcards != null)
				return false;
		} else if (!wildcards.equals(other.wildcards))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[sourceLocation=" + sourceLocation + ", targetLocation=" + targetLocation
				+ ", wildcards=" + wildcards + "]";
	}

}
