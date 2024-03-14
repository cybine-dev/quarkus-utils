package de.cybine.quarkus.util;

import lombok.experimental.*;
import lombok.extern.slf4j.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

@Slf4j
@UtilityClass
public class FilePathHelper
{
    public static final String RESOURCES_PLACEHOLDER = "%resources%";

    public static Optional<Path> resolvePath(String path) throws URISyntaxException
    {
        try
        {
            if(!path.startsWith("%resources%/"))
                return Optional.of(Path.of(path));

            URL resourceUrl = Thread.currentThread()
                                    .getContextClassLoader()
                                    .getResource(path.replace(RESOURCES_PLACEHOLDER + "/", ""));

            if (resourceUrl == null)
            {
                log.warn("Cloud not find resource-path '{}'. Please consider configuring a custom path.", path);
                return Optional.empty();
            }

            return Optional.of(Path.of(resourceUrl.toURI()));
        }
        catch (IllegalArgumentException | FileSystemNotFoundException | SecurityException exception)
        {
            log.warn("Could not find file at '{}'. Please define a valid path.", path);
            return Optional.empty();
        }
    }

    public static Optional<String> tryRead(Path path)
    {
        try
        {
            return Optional.of(Files.readString(path));
        }
        catch (IOException e)
        {
            return Optional.empty();
        }
    }
}
