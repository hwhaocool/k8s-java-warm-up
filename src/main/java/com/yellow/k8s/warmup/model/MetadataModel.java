package com.yellow.k8s.warmup.model;

public class MetadataModel {
    
    private String name;      //pod name
    
    private String namespace;   //命名空间

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MetadataModel [name=" + name + ", namespace=" + namespace + "]";
    }

}
