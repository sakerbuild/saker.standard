package saker.std.api.util;

import saker.build.file.path.SakerPath;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;

/**
 * Utility class containing functions for the standard classes.
 * 
 * @since saker.standard 0.8.1
 */
public class SakerStandardUtils {
	private SakerStandardUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the file name part of the argument file location.
	 * <p>
	 * The method will return the last file name component for the path of the argument.
	 * 
	 * @param fl
	 *            The file location.
	 * @return The file name or <code>null</code> if the argument is <code>null</code>, or has no file name.
	 * @see SakerPath#getFileName()
	 * @since saker.standard 0.8.1
	 */
	public static String getFileLocationFileName(FileLocation fl) {
		if (fl == null) {
			return null;
		}
		FileLocationFileNameVisitor visitor = new FileLocationFileNameVisitor();
		fl.accept(visitor);
		return visitor.result;
	}

	private static class FileLocationFileNameVisitor implements FileLocationVisitor {
		protected String result;

		public FileLocationFileNameVisitor() {
		}

		@Override
		public void visit(LocalFileLocation loc) {
			result = loc.getLocalPath().getFileName();
		}

		@Override
		public void visit(ExecutionFileLocation loc) {
			result = loc.getPath().getFileName();
		}
	}

}
