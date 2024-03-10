package de.cybine.quarkus.util.converter;

import com.fasterxml.jackson.annotation.*;
import de.cybine.quarkus.util.*;
import lombok.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@Getter
@ToString
public class ConverterTree
{
    @JsonProperty("root_node_id")
    private final UUID rootNodeId;

    @JsonProperty("constraint")
    private final ConverterConstraint constraint;

    @JsonProperty("type_constraints")
    private final Map<Type, ConverterConstraint> typeConstraints;

    @JsonProperty("key_mappers")
    private final Map<Type, ConverterKeyMapper<?, ?>> keyMappers;

    @JsonProperty("nodes")
    private final Map<UUID, ConverterTreeNode> treeNodes = new HashMap<>();

    @Builder(builderClassName = "Generator")
    private ConverterTree(ConverterConstraint constraint, @Singular Map<Type, ConverterConstraint> typeConstraints,
            @Singular List<ConverterKeyMapper<?, ?>> keyMappers)
    {
        this.constraint = constraint != null ? constraint : ConverterConstraint.builder().build();
        this.typeConstraints = new HashMap<>(typeConstraints);
        this.typeConstraints.put(ConverterTreeRootNodeType.class,
                ConverterConstraint.builder().maxDepth(1).allowEmptyCollection(true).build());

        ConverterTreeNode rootNode = ConverterTreeNode.builder()
                                                      .tree(this)
                                                      .itemType(ConverterTreeRootNodeType.class)
                                                      .build();

        this.rootNodeId = rootNode.getId();
        this.treeNodes.put(rootNode.getId(), rootNode);

        this.keyMappers = new HashMap<>(
                keyMappers.stream().collect(Collectors.toMap(ConverterKeyMapper::getType, Function.identity())));
        this.keyMappers.put(WithId.class, ConverterKeyMapper.create(WithId.class, WithId::getId));
    }

    @JsonIgnore
    public ConverterTreeNode getRootNode( )
    {
        return this.getNodeOrThrow(this.rootNodeId);
    }

    @JsonProperty("total_mapped_items")
    public int getTotalMappedItems( )
    {
        return this.treeNodes.keySet().size() - 1;
    }

    public ConverterConstraint getConstraint(Type type)
    {
        if (type == null)
            return this.constraint;

        ConverterConstraint typeConstraint = this.typeConstraints.get(type);
        if (typeConstraint != null)
            return this.typeConstraints.get(type);

        return this.constraint;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<Object> findItemId(T item)
    {
        if(item == null)
            return Optional.empty();

        return this.findKeyMapper(item.getClass())
                   .map(mapper -> ((ConverterKeyMapper<? super T, ?>) mapper).getKey(item));
    }

    private Optional<ConverterKeyMapper<?, ?>> findKeyMapper(Class<?> itemType)
    {
        assert itemType != null : "No item type provided";

        ConverterKeyMapper<?, ?> keyMapper = this.keyMappers.get(itemType);
        if (keyMapper != null)
            return Optional.of(keyMapper);

        return this.getKeyMappers()
                   .values()
                   .stream()
                   .filter(item -> item.getType().isAssignableFrom(itemType))
                   .max(Comparator.comparing(ConverterKeyMapper::getType, this::compareClassSpecificity));
    }

    private int compareClassSpecificity(Class<?> type, Class<?> other)
    {
        assert type != null : "No type provided";
        assert other != null : "No other type provided";

        if (type == other)
            return 0;

        if (type.isAssignableFrom(other))
            return -1;

        return 1;
    }

    public Optional<ConverterTreeNode> findNode(UUID id)
    {
        if(id == null)
            throw new IllegalArgumentException("Id must not be null");

        return Optional.ofNullable(this.treeNodes.get(id));
    }

    public ConverterTreeNode getNodeOrThrow(UUID id)
    {
        if(id == null)
            throw new IllegalArgumentException("Id must not be null");

        return this.findNode(id).orElseThrow();
    }

    public void addNode(ConverterTreeNode node)
    {
        if(node == null)
            throw new IllegalArgumentException("Node must not be null");

        if (this.treeNodes.containsKey(node.getId()))
            throw new IllegalStateException("Node key already present");

        this.treeNodes.put(node.getId(), node);
    }

    public static ConverterTree create(ConverterConstraint constraint)
    {
        if(constraint == null)
            throw new IllegalArgumentException("Constraint must not be null");

        return ConverterTree.builder().constraint(constraint).build();
    }
}
