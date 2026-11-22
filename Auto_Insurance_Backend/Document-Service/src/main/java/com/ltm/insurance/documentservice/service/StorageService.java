package com.ltm.insurance.documentservice.service;

public interface StorageService {
    String save(String fileName, byte[] contents);
    byte[] read(String filePath);
    void delete(String filePath);
}