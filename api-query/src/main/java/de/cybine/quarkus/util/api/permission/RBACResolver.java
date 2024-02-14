package de.cybine.quarkus.util.api.permission;

import com.fasterxml.jackson.databind.*;
import de.cybine.quarkus.config.*;
import de.cybine.quarkus.exception.*;
import de.cybine.quarkus.util.*;
import io.quarkus.security.identity.*;
import io.smallrye.mutiny.*;
import jakarta.annotation.*;
import jakarta.inject.*;
import lombok.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

@Singleton
@RequiredArgsConstructor
public class RBACResolver
{
    private final ObjectMapper   objectMapper;
    private final ApiQueryConfig apiQueryConfig;

    private final SecurityIdentity identity;

    private final Map<String, RBACRole> roles = new HashMap<>();

    @PostConstruct
    void setup( ) throws URISyntaxException
    {
        FilePathHelper.resolvePath(this.apiQueryConfig.paths().rbacPath()).ifPresent(this::reload);
    }

    public void reload(Path path)
    {
        try
        {
            JavaType type = this.objectMapper.getTypeFactory().constructParametricType(List.class, RBACRole.class);
            List<RBACRole> rbacRoles = this.objectMapper.readValue(path.toFile(), type);

            this.roles.clear();
            for (RBACRole role : rbacRoles)
                this.roles.put(role.getName(), role);
        }
        catch (IOException exception)
        {
            throw new TechnicalException("Could not load rbac-data", exception);
        }
    }

    public boolean hasPermission(String role, Permission permission)
    {
        RBACRole rbacRole = this.roles.get(role);
        if (rbacRole == null)
            return false;

        if (rbacRole.hasPermission(permission.getName()))
            return true;

        if (rbacRole.getInheritedRoles().isEmpty())
            return false;

        return rbacRole.getInheritedRoles()
                       .orElseThrow()
                       .stream()
                       .map(this.roles::get)
                       .filter(Objects::nonNull)
                       .anyMatch(item -> item.hasPermission(permission.getName()));
    }

    @SuppressWarnings("unused")
    public PermissionChecker toPermissionChecker( )
    {
        return permission -> Uni.createFrom()
                                .item(this.identity.getRoles()
                                                   .stream()
                                                   .anyMatch(item -> this.hasPermission(item, permission)));
    }
}
