package de.cybine.quarkus.data.util.primitive;

import java.io.*;
import java.util.*;

public interface Id<T> extends Serializable
{
    T getValue( );

    default Optional<T> findValue( )
    {
        return Optional.ofNullable(this.getValue());
    }
}
