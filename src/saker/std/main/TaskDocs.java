package saker.std.main;

import java.util.Collection;

import saker.build.file.path.SakerPath;
import saker.build.scripting.model.info.TypeInformationKind;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.std.main.file.copy.CopyFileTaskFactory;
import saker.std.main.file.location.LocalFileLocationTaskFactory;

public class TaskDocs {
	@NestTypeInformation(qualifiedName = "saker.std.main.file.copy.CopyFileTaskOutput")
	@NestInformation("Result of a file copy operation.\n"
			+ "Provides access to the file location of the copy source file. If the copying was done using directories, "
			+ "then contains the list of CopiedFiles that were copied during the operation. The CopiedFiles field contains "
			+ "the target file locations to where the files were copied.")
	@NestFieldInformation(value = "Target",
			type = @NestTypeUsage(DocFileLocation.class),
			info = @NestInformation("The target location of the copy operation.\n"
					+ "This is the same file location that was passed as the Target input parameter to the"
					+ CopyFileTaskFactory.TASK_NAME + "() task."))
	@NestFieldInformation(value = "CopiedFiles",
			type = @NestTypeUsage(DocFileCollection.class),
			info = @NestInformation("Collection of file locations that were copied during a directory copying.\n"
					+ "If the copy operation copied the subfiles of the source directory, then this field contains "
					+ "the file locations that were copied. The file locations point to the target location where "
					+ "the copied files ended up.\n"
					+ "If the copy operation didn't copy subfiles or subdirectories, then this field is empty."))
	public static class CopyFileTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "saker.std.api.file.location.LocalFileLocation")
	@NestInformation("Represents a local file system location.\n"
			+ "The enclosed LocalPath should be interpreted against the file system that the build execution is running on. "
			+ "Can be specified as input parameters to tasks which support it.\n" + "Can be created using the "
			+ LocalFileLocationTaskFactory.TASK_NAME + "() task.")
	@NestFieldInformation(value = "LocalPath",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("Contains the path on the local file system."))
	public static class DocLocalFileLocation {
	}

	@NestInformation("Represents a location of a file available in the build execution.\n"
			+ "The actual path to the execution file can be retrieved using the Path field. An object "
			+ "of this kind can be passed as inputs to tasks which expect files as their inputs.")
	@NestTypeInformation(qualifiedName = "saker.std.api.file.location.ExecutionFileLocation",
			relatedTypes = { @NestTypeUsage(SakerPath.class) })
	@NestFieldInformation(value = "Path",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("Contains the execution path of the file."))
	public static class DocExecutionFileLocation {
	}

	@NestTypeInformation(qualifiedName = "saker.std.api.file.location.FileLocation",
			kind = TypeInformationKind.OBJECT,
			relatedTypes = { @NestTypeUsage(DocLocalFileLocation.class),
					@NestTypeUsage(DocExecutionFileLocation.class) })
	@NestInformation("Represents a location of a given file.\n"
			+ "A file location is a reference to a file that exists at some place in the build environment. "
			+ "It may be either an execution file location, or a local file system location. The path of the "
			+ "file may be retrieved by using the field [Path] for execution and [LocalPath] for local file locations.")
	public static class DocFileLocation {
	}

	@NestTypeInformation(qualifiedName = "saker.std.api.file.location.FileCollection",
			relatedTypes = @NestTypeUsage(value = Collection.class, elementTypes = DocFileLocation.class))
	@NestInformation("Collection of file locations.\n"
			+ "The files enclosed in the file collection may represent arbitrary locations. The file collection "
			+ "can be iterated over using foreach loops. The actual paths can be retrieved by an appropriate field "
			+ "of the elements. It is usually [Path] for execution file locations and [LocalPath] for local "
			+ "file system locations.\n" + "The object can be passed as an input to tasks which support it.")
	public static class DocFileCollection {
	}

	@NestTypeInformation(qualifiedName = "saker.std.api.environment.qualifier.EnvironmentQualifier")
	@NestInformation("Represents and environment qualifier that is able to determine if a given build environment "
			+ "is suitable for an associated operation to be performed on it.")
	public static class DocEnvironmentQualifier {
	}

	@NestTypeInformation(qualifiedName = "CharsetTaskOption",
			enumValues = {

					@NestFieldInformation(value = "US_ASCII", info = @NestInformation("The US_ASCII charset.")),
					@NestFieldInformation(value = "ISO-8859-1", info = @NestInformation("The ISO-8859-1 charset.")),
					@NestFieldInformation(value = "UTF-8", info = @NestInformation("The UTF-8 charset.")),
					@NestFieldInformation(value = "UTF-16BE", info = @NestInformation("The UTF-16BE charset.")),
					@NestFieldInformation(value = "UTF-16LE", info = @NestInformation("The UTF-16LE charset.")),
					@NestFieldInformation(value = "UTF-16", info = @NestInformation("The UTF-16 charset.")),

			})
	@NestInformation("Represents a character encoding format (charset).")
	public static class DocCharsetTaskOption {
	}
}