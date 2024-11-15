package tech.zerofiltre.blog.domain.storage;

import tech.zerofiltre.blog.domain.error.ZerofiltreException;

import java.util.Optional;

public interface StorageProvider {

    void store(byte[] data, String path) throws ZerofiltreException;

    Optional<byte[]> get(String key);

    void delete(String path) throws ZerofiltreException;

}
