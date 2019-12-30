package saker.std.main.file.put;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;

import saker.build.file.ByteArraySakerFile;
import saker.build.file.SakerDirectory;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.FileEntry;
import saker.build.file.provider.LocalFileProvider;
import saker.build.file.provider.SakerFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.main.file.property.LocalFileContentDescriptorExecutionProperty;

class PutFileWorkerTaskFactory
		implements TaskFactory<FileLocation>, Task<FileLocation>, Externalizable {
	private static final long serialVersionUID = 1L;

	private FileLocation fileLocation;
	private String contents;
	private String charset;

	/**
	 * For {@link Externalizable}.
	 */
	public PutFileWorkerTaskFactory() {
	}

	public PutFileWorkerTaskFactory(FileLocation fileLocation, String contents, String charset) {
		this.fileLocation = fileLocation;
		this.contents = contents;
		this.charset = charset;
	}

	@Override
	public Task<? extends FileLocation> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public FileLocation run(TaskContext taskcontext) throws Exception {
		fileLocation.accept(new FileLocationVisitor() {
			@Override
			public void visit(ExecutionFileLocation loc) {
				SakerPath path = loc.getPath();
				try {
					byte[] bytecontents = charset == null ? contents.getBytes(StandardCharsets.UTF_8)
							: contents.getBytes(charset);
					ByteArraySakerFile outfile = new ByteArraySakerFile(path.getFileName(), bytecontents);
					SakerDirectory parentdir = taskcontext.getTaskUtilities()
							.resolveDirectoryAtPathCreate(path.getParent());
					parentdir.add(outfile);
					outfile.synchronize();
					taskcontext.getTaskUtilities().reportOutputFileDependency(null, outfile);
				} catch (Exception e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}

			@Override
			public void visit(LocalFileLocation loc) {
				SakerPath path = loc.getLocalPath();
				try {
					byte[] bytecontents = charset == null ? contents.getBytes(StandardCharsets.UTF_8)
							: contents.getBytes(charset);

					LocalFileProvider fp = LocalFileProvider.getInstance();
					fp.ensureWriteRequest(path, FileEntry.TYPE_FILE,
							SakerFileProvider.OPERATION_FLAG_DELETE_INTERMEDIATE_FILES);
					fp.writeToFile(new UnsyncByteArrayInputStream(bytecontents), path);
					ContentDescriptor contentdescriptor = taskcontext
							.invalidateGetContentDescriptor(fp.getPathKey(path));

					taskcontext.reportExecutionDependency(new LocalFileContentDescriptorExecutionProperty(path),
							contentdescriptor);
				} catch (Exception e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}
		});

		return fileLocation;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(fileLocation);
		out.writeObject(contents);
		out.writeObject(charset);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		fileLocation = (FileLocation) in.readObject();
		contents = (String) in.readObject();
		charset = (String) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((charset == null) ? 0 : charset.hashCode());
		result = prime * result + ((contents == null) ? 0 : contents.hashCode());
		result = prime * result + ((fileLocation == null) ? 0 : fileLocation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PutFileWorkerTaskFactory other = (PutFileWorkerTaskFactory) obj;
		if (charset == null) {
			if (other.charset != null)
				return false;
		} else if (!charset.equals(other.charset))
			return false;
		if (contents == null) {
			if (other.contents != null)
				return false;
		} else if (!contents.equals(other.contents))
			return false;
		if (fileLocation == null) {
			if (other.fileLocation != null)
				return false;
		} else if (!fileLocation.equals(other.fileLocation))
			return false;
		return true;
	}

}