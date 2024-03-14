package dev.codex.io;

import dev.codex.java.wrapper.runtime.AccessMode;
import dev.codex.system.LibCWrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileStream implements AutoCloseable {
    private final String path;
    private final AccessMode mode;
    private int fd;

    public FileStream(String name) throws FileNotFoundException, NullPointerException {
        this(name != null ? new File(name) : null, AccessMode.READ_ONLY);
    }

    public FileStream(String name, AccessMode mode) throws FileNotFoundException, NullPointerException {
        this(name != null ? new File(name) : null, mode);
    }

    public FileStream(File file) throws FileNotFoundException, NullPointerException {
        this(file, AccessMode.READ_ONLY);
    }

    public FileStream(File file, AccessMode mode) throws FileNotFoundException, NullPointerException {
        String path = file != null ? file.getPath() : null;
        if (path == null) {
            throw new NullPointerException();
        }
        if (!file.exists()) {
            throw new FileNotFoundException("Invalid file path " + path);
        }

        this.path = path;
        this.mode = mode;
        this.fd = -1;
    }

    public FileStream open() throws IOException {
        this.fd = LibCWrapper.open(this.path, this.mode.value());
        if (fd < 0) {
            throw new IOException("Exception occurred while opening the file: " + LibCWrapper.strerror());
        }
        return this;
    }

    public boolean isOpen() {
        return this.fd != -1;
    }

    @Override
    public void close() throws IOException {
        if (this.isOpen()) {
            if (LibCWrapper.close(this.fd) < 0) {
                throw new IOException("Exception occurred while closing the file: " + LibCWrapper.strerror());
            }
            this.fd = -1;
        }
    }

    public int write(byte[] bytes) throws IOException {
        if (!this.isOpen())
            throw new IOException("File " + this.path + " is not open");

        if (this.mode == AccessMode.READ_ONLY)
            throw new IOException("File " + this.path + " is open in READ_ONLY mode");

        int write = LibCWrapper.write(this.fd, bytes, bytes.length);
        if (write < 0) {
            throw new IOException("Exception occurred while writing to the file: " + LibCWrapper.strerror());
        }
        return write;
    }

    public int read(byte[] bytes) throws IOException {
        if (!this.isOpen())
            throw new IOException("File " + this.path + " is not open");

        if (this.mode == AccessMode.WRITE_ONLY)
            throw new IOException("File " + this.path + " is open in WRITE_ONLY mode");

        int read = LibCWrapper.read(this.fd, bytes, bytes.length);
        if (read < 0) {
            throw new IOException("Exception occurred while reading from the file: " + LibCWrapper.strerror());
        }
        return read;
    }

    public String path() {
        return this.path;
    }

    public AccessMode mode() {
        return this.mode;
    }

    public int fd() {
        return this.fd;
    }
}