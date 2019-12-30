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
		+ "The file location may be either reference a file in the build execution file hierarchy, or the local file system.")
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
