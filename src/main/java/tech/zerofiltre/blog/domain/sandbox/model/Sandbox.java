package tech.zerofiltre.blog.domain.sandbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
public class Sandbox {

    private String username;
    private String password;
    private Type type;

    @AllArgsConstructor
    public enum Type {
        K8S("k8s"),
        NONE("none");

        @Getter
        private final String value;

    }
}
