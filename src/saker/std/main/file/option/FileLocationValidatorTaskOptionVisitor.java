package saker.std.main.file.option;

import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;

final class FileLocationValidatorTaskOptionVisitor implements FileLocationVisitor {
	public static final FileLocationVisitor INSTANCE = new FileLocationValidatorTaskOptionVisitor();

	public FileLocationValidatorTaskOptionVisitor() {
	}

	@Override
	public void visit(LocalFileLocation loc) {
	}

	@Override
	public void visit(ExecutionFileLocation loc) {
	}
}