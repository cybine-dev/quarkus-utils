package de.cybine.quarkus.util;

import java.util.*;

public interface WithId<T>
{
    T getId( );

    default Optional<T> findId( )
    {
        return Optional.ofNullable(this.getId());
    }
}
