package de.cybine.quarkus.util.converter;

import lombok.*;

import java.util.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ConverterChain<I>
{
    private final Class<I> inputType;

    private final List<Class<?>> intermediaryTypes = new ArrayList<>();

    private ConverterTree     tree;
    private ConverterResolver resolver;

    public ConverterChain<I> withResolver(ConverterResolver resolver)
    {
        if(resolver == null)
            throw new IllegalArgumentException("Resolver must not be null");

        this.resolver = resolver;
        return this;
    }

    public ConverterChain<I> withTree(ConverterTree tree)
    {
        if(tree == null)
            throw new IllegalArgumentException("Converter tree must not be null");

        this.tree = tree;
        return this;
    }

    public ConverterChain<I> withIntermediary(Class<?> intermediaryType)
    {
        if(intermediaryType == null)
            throw new IllegalArgumentException("Intermediary type must be provided");

        this.intermediaryTypes.add(intermediaryType);
        return this;
    }

    public <O> ConversionProcessor<I, O> withOutput(Class<O> outputType)
    {
        if(outputType == null)
            throw new IllegalArgumentException("Output type must not be null");

        assert this.tree != null : "No converter tree provided";
        assert this.resolver != null : "No converter resolver provided";
        assert this.inputType != null : "No input type provided";

        Class<?> input = this.inputType;
        List<ConverterType<?, ?>> types = new ArrayList<>(this.intermediaryTypes.size() + 1);
        for(Class<?> type : this.intermediaryTypes)
        {
            types.add(new ConverterType<>(input, type));
            input = type;
        }

        types.add(new ConverterType<>(input, outputType));

        return new ConversionProcessor<>(this.tree, this.resolver, types);
    }

    static <I> ConverterChain<I> withInput(Class<I> inputType)
    {
        if(inputType == null)
            throw new IllegalArgumentException("Input type must not be null");

        return new ConverterChain<>(inputType);
    }
}
