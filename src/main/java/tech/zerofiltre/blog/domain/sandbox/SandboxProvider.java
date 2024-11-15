package tech.zerofiltre.blog.domain.sandbox;

import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.sandbox.model.Sandbox;

public interface SandboxProvider {
    Sandbox initialize(String fullName, String email) throws ZerofiltreException;

    void destroy(String fullName, String email) throws ZerofiltreException;

}
