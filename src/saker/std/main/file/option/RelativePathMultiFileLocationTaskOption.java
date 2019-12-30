package saker.std.main.file.option;

import saker.build.file.path.SakerPath;

final class RelativePathMultiFileLocationTaskOption implements MultiFileLocationTaskOption {
	private final SakerPath path;

	public RelativePathMultiFileLocationTaskOption(SakerPath path) {
		this.path = path;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visitRelativePath(path);
	}

	@Override
	public MultiFileLocationTaskOption clone() {
		return this;
	}
}