package saker.std.main.file.option;

import saker.build.file.path.WildcardPath;

final class WildcardMultiFileLocationTaskOption implements MultiFileLocationTaskOption {
	private final WildcardPath path;

	public WildcardMultiFileLocationTaskOption(WildcardPath path) {
		this.path = path;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visitWildcard(path);
	}

	@Override
	public MultiFileLocationTaskOption clone() {
		return this;
	}
}