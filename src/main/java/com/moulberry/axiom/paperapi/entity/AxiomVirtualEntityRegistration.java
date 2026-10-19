package com.moulberry.axiom.paperapi.entity;

@FunctionalInterface
public interface AxiomVirtualEntityRegistration extends AutoCloseable {
    @Override
    void close();
}
