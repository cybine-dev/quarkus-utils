package de.cybine.quarkus.util.api.permission;

import io.smallrye.mutiny.*;

import java.security.*;
import java.util.function.*;

@FunctionalInterface
public interface PermissionChecker extends Function<Permission, Uni<Boolean>>
{ }
