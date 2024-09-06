package de.cybine.quarkus.util.test;

import io.quarkus.test.junit.*;
import lombok.experimental.*;

import java.util.*;

@UtilityClass
@SuppressWarnings("unused")
public class TestProfiles
{
    public static class Unit implements QuarkusTestProfile
    {
        @Override
        public Set<String> tags( )
        {
            return Set.of("unit");
        }
    }

    public static class Integration implements QuarkusTestProfile
    {
        @Override
        public Set<String> tags( )
        {
            return Set.of("integration", "container");
        }
    }

    public static class Container implements QuarkusTestProfile
    {
        @Override
        public Set<String> tags( )
        {
            return Set.of("container");
        }
    }
}
