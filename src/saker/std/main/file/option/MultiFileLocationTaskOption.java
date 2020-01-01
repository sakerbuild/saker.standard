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
package saker.std.main.file.option;

import java.util.Collection;
import java.util.Collections;

import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.file.path.WildcardPath.ReducedWildcardPath;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileCollection;
import saker.std.api.file.location.FileLocation;
import saker.std.main.TaskDocs.DocFileCollection;
import saker.std.main.TaskDocs.DocFileLocation;

@NestTypeInformation(relatedTypes = { @NestTypeUsage(DocFileLocation.class), @NestTypeUsage(WildcardPath.class),
		@NestTypeUsage(SakerPath.class), @NestTypeUsage(DocFileCollection.class), })
@NestInformation("Option accepting files as its input.\n"
		+ "Accepts simple paths, wildcards, file locations and file collections as its input.")
public interface MultiFileLocationTaskOption {
	public void accept(Visitor visitor);

	public MultiFileLocationTaskOption clone();

	public static MultiFileLocationTaskOption valueOf(FileLocation filelocation) {
		FileLocationTaskOption.validateFileLocation(filelocation);
		return new SimpleMultiFileLocationTaskOption(Collections.singleton(filelocation));
	}

	public static MultiFileLocationTaskOption valueOf(FileCollection files) {
		return new SimpleMultiFileLocationTaskOption(ImmutableUtils.makeImmutableLinkedHashSet(files));
	}

	public static MultiFileLocationTaskOption valueOf(WildcardPath path) {
		ReducedWildcardPath reduced = path.reduce();
		if (reduced.getWildcard() == null) {
			return valueOf(reduced.getFile());
		}
		return new WildcardMultiFileLocationTaskOption(path);
	}

	public static MultiFileLocationTaskOption valueOf(SakerPath path) {
		if (!path.isAbsolute()) {
			return new RelativePathMultiFileLocationTaskOption(path);
		}
		return new SimpleMultiFileLocationTaskOption(Collections.singleton(ExecutionFileLocation.create(path)));
	}

	public static MultiFileLocationTaskOption valueOf(String path) {
		return valueOf(WildcardPath.valueOf(path));
	}

	public interface Visitor {
		public void visitRelativePath(SakerPath path);

		public void visitWildcard(WildcardPath path);

		public void visitFileLocations(Collection<? extends FileLocation> locations);
	}
}
