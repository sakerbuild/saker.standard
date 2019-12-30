package saker.std.main.file.option;

import java.util.Collection;

import saker.std.api.file.location.FileLocation;

final class SimpleMultiFileLocationTaskOption implements MultiFileLocationTaskOption {
	private final Collection<FileLocation> fileLocations;

	public SimpleMultiFileLocationTaskOption(Collection<FileLocation> fileLocations) {
		this.fileLocations = fileLocations;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visitFileLocations(fileLocations);
	}

	@Override
	public MultiFileLocationTaskOption clone() {
		return this;
	}
}