package saker.std.main.file.option;

import saker.build.file.path.SakerPath;

final class RelativePathExecutionFileLocationTaskOption implements FileLocationTaskOption {
	private final SakerPath path;

	public RelativePathExecutionFileLocationTaskOption(SakerPath path) {
		this.path = path;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visitRelativePath(path);
	}

	@Override
	public FileLocationTaskOption clone() {
		return this;
	}
}