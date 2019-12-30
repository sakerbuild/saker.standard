package saker.std.main.file.option;

import saker.std.api.file.location.FileLocation;

final class SimpleFileLocationTaskOption implements FileLocationTaskOption {
	private final FileLocation location;

	public SimpleFileLocationTaskOption(FileLocation location) {
		this.location = location;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visitFileLocation(location);
	}

	@Override
	public FileLocationTaskOption clone() {
		return this;
	}
}