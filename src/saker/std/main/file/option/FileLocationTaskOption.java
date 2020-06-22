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

import saker.build.file.path.SakerPath;
import saker.build.scripting.model.info.TypeInformationKind;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.main.TaskDocs.DocExecutionFileLocation;
import saker.std.main.TaskDocs.DocLocalFileLocation;

@NestInformation("Represents a location of a file.\n"
		+ "The file location may either be a reference to a file in the build execution file hierarchy, "
		+ "or the local file system.")
@NestTypeInformation(relatedTypes = { @NestTypeUsage(kind = TypeInformationKind.PATH, value = SakerPath.class),
		@NestTypeUsage(DocLocalFileLocation.class), @NestTypeUsage(DocExecutionFileLocation.class), })
public interface FileLocationTaskOption {
	public void accept(Visitor visitor);

	public FileLocationTaskOption clone();

	public static FileLocationTaskOption valueOf(FileLocation filelocation) {
		validateFileLocation(filelocation);
		return new SimpleFileLocationTaskOption(filelocation);
	}

	public static FileLocationTaskOption valueOf(SakerPath path) {
		if (!path.isAbsolute()) {
			return new RelativePathExecutionFileLocationTaskOption(path);
		}
		return new SimpleFileLocationTaskOption(ExecutionFileLocation.create(path));
	}

	public static FileLocationTaskOption valueOf(String path) {
		return valueOf(SakerPath.valueOf(path));
	}

	public static void validateFileLocation(FileLocation filelocation) {
		filelocation.accept(FileLocationValidatorTaskOptionVisitor.INSTANCE);
	}

	public interface Visitor {
		public void visitRelativePath(SakerPath path);

		public void visitFileLocation(FileLocation location);
	}
}
