package com.ltm.insurance.documentservice.service;

import com.ltm.insurance.documentservice.exception.DocumentGenerationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class LocalStorageService implements StorageService {

    private final Path root;

    public LocalStorageService(@Value("${document.storage.location:./generated-documents}") String location) {
        root = Path.of(location).toAbsolutePath().normalize();
    }

    @Override
    public String save(String fileName, byte[] contents) {
        String safeName = fileName == null ? "" : fileName.replaceAll("[^A-Za-z0-9._-]", "_");
        Path target = resolve(safeName);
        try {
            Files.createDirectories(root);
            Files.write(target, contents);
            return target.toString();
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to save generated document", exception);
        }
    }

    @Override
    public byte[] read(String filePath) {
        try {
            return Files.readAllBytes(resolveStoredPath(filePath));
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to read generated document", exception);
        }
    }

    @Override
    public void delete(String filePath) {
        try {
            Files.deleteIfExists(resolveStoredPath(filePath));
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to remove generated document", exception);
        }
    }

    private Path resolve(String fileName) {
        if (fileName.isBlank() || fileName.equals(".") || fileName.equals("..")) {
            throw new DocumentGenerationException("Invalid document file name");
        }
        Path target = root.resolve(fileName).normalize();
        if (!target.startsWith(root)) {
            throw new DocumentGenerationException("Invalid document file name");
        }
        return target;
    }

    private Path resolveStoredPath(String filePath) {
        Path target = Path.of(filePath).toAbsolutePath().normalize();
        if (!target.startsWith(root)) {
            throw new DocumentGenerationException("Invalid document storage path");
        }
        return target;
    }
}