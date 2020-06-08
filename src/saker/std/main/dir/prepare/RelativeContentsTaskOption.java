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
package saker.std.main.dir.prepare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities;
import saker.build.task.dependencies.FileCollectionStrategy;
import saker.build.task.utils.dependencies.WildcardFileCollectionStrategy;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileCollection;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.util.SakerStandardUtils;
import saker.std.main.TaskDocs.DocPattern;
import saker.std.main.TaskDocs.DocPatternReplacement;
import saker.std.main.file.option.MultiFileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

@NestInformation("A file configuration that is used to include files in a given package.\n"
		+ "The configuration is used to specify which files and how they should be added to the "
		+ "result package or directory.\n"
		+ "The configuration accepts simple paths and wildcards of theirs or its fields can be used "
		+ "to configure in a more complex way.")
@NestFieldInformation(value = "Directory",
		type = @NestTypeUsage(value = Collection.class, elementTypes = MultiFileLocationTaskOption.class),
		info = @NestInformation("Specifies one or more directory paths which are used as a base to find files specified in Wildcard.\n"
				+ "Each wildcard path specified in the Wildcard field will be matched relative to the Directory paths specified.\n"
				+ "The result path will be the concatenation of the TargetDirectory field, and the relative path from the "
				+ "Directory to each found file.\n"
				+ "If no directory is specified, the current working path of the task will be used.\n"
				+ "This option cannot be used together with Files."))
@NestFieldInformation(value = "Wildcard",
		type = @NestTypeUsage(value = Collection.class, elementTypes = WildcardPath.class),
		info = @NestInformation("Wildcard patterns used to find files relative to the specified Directory.\n"
				+ "The specified wildcard paths in this field will be used to match the input files under the directories specified "
				+ "in Directory. Each matched file will be included in the result with the path that is the concatenation of TargetDirectory "
				+ "and the relative path from the associated Directory.\n"
				+ "The wildcards can be used to match both files and directories to add to the archive.\n"
				+ "This option cannot be used together with Files."))

@NestFieldInformation(value = "Files",
		type = @NestTypeUsage(value = Collection.class, elementTypes = MultiFileLocationTaskOption.class),
		info = @NestInformation("Specifies one or more files that should be added to the result.\n"
				+ "The files can be specified using simple paths, wildcards, file locations or file collections.\n"
				+ "The final archive path of each file will be the concatenation of the TargetDirectory and the file name of a "
				+ "given file.\n" + "This option cannot be used together with Directory and Resources."))

@NestFieldInformation(value = "TargetDirectory",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("Specifies the target directory under which the entries specified in the configuration should be placed.\n"
				+ "Any input file that is matched by this configuration will have their output paths prepended by the value of this field.\n"
				+ "The specified path must be forward relative. By default, no target directory is used.\n"
				+ "The TargetDirectory is prepended to the output path AFTER it has been remapped defined by the Remap field."))
@NestFieldInformation(value = "Remap",
		type = @NestTypeUsage(value = Map.class, elementTypes = { DocPattern.class, DocPatternReplacement.class }),
		info = @NestInformation("Contains path remapping specifications with regular expressions.\n"
				+ "The keys of the Remap field are regular expressions that are matched against the output relative path of a file. "
				+ "The paths which are matched don't have the TargetDirectory value prepended to them.\n"
				+ "The values of the Remap field are replacement expressions that are applied to the matched paths.\n"
				+ "The pattern matching and replacement works the same way as the Java Pattern class.\n"
				+ "If the remapped path replacement results in empty string then the corresponding file will be omitted."))
//since 0.8.4
public interface RelativeContentsTaskOption {
	public default RelativeContentsTaskOption clone() {
		List<MultiFileLocationTaskOption> directory = ObjectUtils.cloneArrayList(getDirectory(),
				MultiFileLocationTaskOption::clone);
		List<MultiFileLocationTaskOption> files = ObjectUtils.cloneArrayList(getFiles(),
				MultiFileLocationTaskOption::clone);
		List<WildcardPath> wildcard = ImmutableUtils.makeImmutableList(getWildcard());
		SakerPath targetdirectory = getTargetDirectory();
		LinkedHashMap<String, String> remap = ObjectUtils.cloneLinkedHashMap(getRemap());
		return new RelativeContentsTaskOption() {
			@Override
			public RelativeContentsTaskOption clone() {
				return this;
			}

			@Override
			public void accept(RelativeContentsTaskOption.Visitor visitor) {
				visitor.visit(this);
			}

			@Override
			public Collection<MultiFileLocationTaskOption> getDirectory() {
				return directory;
			}

			@Override
			public Collection<WildcardPath> getWildcard() {
				return wildcard;
			}

			@Override
			public Collection<MultiFileLocationTaskOption> getFiles() {
				return files;
			}

			@Override
			public SakerPath getTargetDirectory() {
				return targetdirectory;
			}

			@Override
			public Map<String, String> getRemap() {
				return remap;
			}
		};
	}

	public void accept(RelativeContentsTaskOption.Visitor visitor);

	public default Collection<MultiFileLocationTaskOption> getDirectory() {
		return null;
	}

	public default Collection<WildcardPath> getWildcard() {
		return null;
	}

	public default Collection<MultiFileLocationTaskOption> getFiles() {
		return null;
	}

	public default SakerPath getTargetDirectory() {
		return null;
	}

	//maps regexes to replacements
	public default Map<String, String> getRemap() {
		return null;
	}

	public static RelativeContentsTaskOption valueOf(String input) {
		return valueOf(SakerPath.valueOf(input));
	}

	public static RelativeContentsTaskOption valueOf(WildcardPath input) {
		return new RelativeContentsTaskOption() {
			@Override
			public RelativeContentsTaskOption clone() {
				return this;
			}

			@Override
			public void accept(RelativeContentsTaskOption.Visitor visitor) {
				visitor.visit(input);
			}
		};
	}

	public static RelativeContentsTaskOption valueOf(SakerPath input) {
		return valueOf(WildcardPath.valueOf(input));
	}

	public static RelativeContentsTaskOption valueOf(FileLocation input) {
		return valueOf(FileCollection.create(ImmutableUtils.singletonSet(input)));
	}

	public static RelativeContentsTaskOption valueOf(FileCollection input) {
		return new RelativeContentsTaskOption() {
			@Override
			public RelativeContentsTaskOption clone() {
				return this;
			}

			@Override
			public void accept(RelativeContentsTaskOption.Visitor visitor) {
				visitor.visit(input);
			}
		};
	}

	public interface Visitor {
		public void visit(RelativeContentsTaskOption input);

		public void visit(SakerPath path);

		public void visit(WildcardPath wildcard);

		public void visit(FileCollection files);
	}

	public static NavigableMap<SakerPath, FileLocation> toInputMap(TaskContext taskcontext,
			Iterable<? extends RelativeContentsTaskOption> contents, Object wildcarddependencytag) {
		if (contents == null) {
			return null;
		}
		NavigableMap<SakerPath, FileLocation> inputs = new TreeMap<>();
		RelativeContentsTaskOption.Visitor tovisitor = new RelativeContentsTaskOption.Visitor() {
			@Override
			public void visit(FileCollection files) {
				for (FileLocation f : files) {
					visitFileLocation(f);
				}
			}

			@Override
			public void visit(WildcardPath wildcard) {
				for (FileLocation fl : TaskOptionUtils.toFileLocations(MultiFileLocationTaskOption.valueOf(wildcard),
						taskcontext, wildcarddependencytag)) {
					visitFileLocation(fl);
				}
			}

			@Override
			public void visit(SakerPath path) {
				visitExecutionFile(taskcontext.getTaskWorkingDirectoryPath().tryResolve(path));
			}

			@Override
			public void visit(RelativeContentsTaskOption input) {
				SakerPath targetdir = ObjectUtils.nullDefault(input.getTargetDirectory(), SakerPath.EMPTY);
				if (targetdir != null && !targetdir.isForwardRelative()) {
					throw new IllegalArgumentException("TargetDirectory must be a forward relative path: " + targetdir);
				}
				Map<String, String> remap = input.getRemap();
				List<Entry<Pattern, String>> remapentries;
				if (!ObjectUtils.isNullOrEmpty(remap)) {
					remapentries = new ArrayList<>();
					for (Entry<String, String> entry : remap.entrySet()) {
						remapentries.add(ImmutableUtils.makeImmutableMapEntry(Pattern.compile(entry.getKey()),
								entry.getValue()));
					}
				} else {
					remapentries = null;
				}
				Collection<FileLocation> files = TaskOptionUtils.toFileLocations(input.getFiles(), taskcontext,
						wildcarddependencytag);
				Collection<FileLocation> directory = TaskOptionUtils.toFileLocations(input.getDirectory(), taskcontext,
						wildcarddependencytag);
				Collection<WildcardPath> wildcard = input.getWildcard();
				if (files != null) {
					if (directory != null || wildcard != null) {
						throw new IllegalArgumentException("Files cannot be used together with Directory or Wildcard.");
					}
					for (FileLocation f : files) {
						visitComplex(f, SakerPath.valueOf(SakerStandardUtils.getFileLocationFileName(f)), remapentries,
								targetdir);
					}
				} else {
					if (wildcard == null) {
						throw new IllegalArgumentException("No inputs specified. Wildcard or Files property missing.");
					}
					if (directory == null) {
						directory = ImmutableUtils
								.singletonSet(ExecutionFileLocation.create(taskcontext.getTaskWorkingDirectoryPath()));
					}
					FileLocationVisitor dirflvisitor = new FileLocationVisitor() {
						//TODO support local file locations

						@Override
						public void visit(ExecutionFileLocation loc) {
							SakerPath dirpath = loc.getPath();
							Collection<FileCollectionStrategy> collectionstrats = new HashSet<>();
							for (WildcardPath wc : wildcard) {
								collectionstrats.add(WildcardFileCollectionStrategy.create(dirpath, wc));
							}
							TaskExecutionUtilities taskutils = taskcontext.getTaskUtilities();

							NavigableMap<SakerPath, SakerFile> files = taskutils
									.collectFilesReportAdditionDependency(wildcarddependencytag, collectionstrats);
							taskutils.reportInputFileDependency(wildcarddependencytag, ObjectUtils
									.singleValueMap(files.navigableKeySet(), CommonTaskContentDescriptors.PRESENT));

							//only include children that are UNDER the relative directory
							//don't include the directory itself as that has no file name
							//and would only cause an exception down the line
							for (SakerPath filepath : SakerPathFiles
									.getPathSubSetDirectoryChildren(files.navigableKeySet(), dirpath, false)) {
								SakerPath relative = dirpath.relativize(filepath);
								visitComplex(ExecutionFileLocation.create(filepath), relative, remapentries, targetdir);

							}
						}
					};
					for (FileLocation dir : directory) {
						dir.accept(dirflvisitor);
					}
				}
			}

			private void visitComplex(FileLocation f, SakerPath basepath, List<Entry<Pattern, String>> remapentries,
					SakerPath targetdir) {
				if (remapentries != null) {
					String basepathstr = basepath.toString();
					for (Entry<Pattern, String> remapentry : remapentries) {
						Matcher matcher = remapentry.getKey().matcher(basepathstr);
						if (matcher.matches()) {
							String replaced = null;
							try {
								replaced = matcher.replaceFirst(remapentry.getValue());
								if (ObjectUtils.isNullOrEmpty(replaced)) {
									//remapped to empty. this signals to exclude
									return;
								}
								basepath = SakerPath.valueOf(replaced);
							} catch (Exception e) {
								throw new IllegalArgumentException("Failed to perform Remapping of entry: " + basepath
										+ " with pattern: " + remapentry.getKey().pattern() + " and replacement: "
										+ remapentry.getValue() + (replaced == null ? "" : " and result: " + replaced),
										e);
							}
							if (!basepath.isForwardRelative()) {
								throw new IllegalArgumentException(
										"Remapped path is not forward relative: " + basepath);
							}
							if (basepath.getFileName() == null) {
								throw new IllegalArgumentException("Remapped path has no file name: " + basepath);
							}
							break;
						}
					}
				}
				SakerPath path;
				if (targetdir != null) {
					path = targetdir.resolve(basepath);
				} else {
					path = basepath;
				}
				putInput(path, f);
			}

			private void visitFileLocation(FileLocation f) {
				SakerPath path = SakerPath.valueOf(SakerStandardUtils.getFileLocationFileName(f));
				FileLocation prev = inputs.putIfAbsent(path, f);
				if (prev != null) {
					throw new IllegalArgumentException(
							"Duplicate entries for path: " + path + " with " + prev + " and " + f);
				}
			}

			private void visitExecutionFile(SakerPath inpath) {
				SakerPath path = SakerPath.valueOf(inpath.getFileName());
				ExecutionFileLocation f = ExecutionFileLocation.create(inpath);
				putInput(path, f);
			}

			private void putInput(SakerPath path, FileLocation f) {
				FileLocation prev = inputs.putIfAbsent(path, f);
				if (prev != null) {
					throw new IllegalArgumentException(
							"Duplicate entries for path: " + path + " with " + prev + " and " + f);
				}
			}
		};
		for (RelativeContentsTaskOption contentoption : contents) {
			if (contentoption == null) {
				continue;
			}
			contentoption.accept(tovisitor);
		}
		return inputs;
	}
}