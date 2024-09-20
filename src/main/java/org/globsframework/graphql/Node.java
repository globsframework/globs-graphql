package org.globsframework.graphql;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.Field;
import org.globsframework.core.metamodel.fields.GlobArrayField;
import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.model.AbstractFieldSetter;
import org.globsframework.core.model.FieldSetter;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.utils.collections.MultiMap;
import org.globsframework.core.utils.exceptions.ItemNotFound;
import org.globsframework.graphql.model.GQLMandatory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Node {
    public final Glob data;
    private final GQLGlobType glQuery;
    private MultiMap<Field, Node> children = new MultiMap<>();

    public Node(GQLGlobType glQuery, Glob data) {
        this.glQuery = glQuery;
        this.data = data;
    }

    public Node addChild(Field field, GQLGlobType glQuery, Glob child) {
        Node node = new Node(glQuery, child);
        children.put(field, node);
        return node;
    }

    public Glob buildResponse(GQLGlobCallerBuilder<?> gqlGlobCallerBuilder) {
        MutableGlob out = glQuery.outputType.instantiate();
        glQuery.aliasToField.forEach((field, qglFieldWithParameter) -> {
            if (qglFieldWithParameter.gqlGlobType() == null) {
                if (field.getName().equals("__typename")) {
                    out.set(field.asStringField(), field.getGlobType().getName());
                } else {
                    if (data == null) {
                        return;
                    }
                    GQLGlobFieldMapper gqlGlobFieldMapper = gqlGlobCallerBuilder.get(qglFieldWithParameter.field().field(), data.getType());
                    gqlGlobFieldMapper.update(data, new AbstractFieldSetter<>() {
                        @Override
                        public FieldSetter<?> setValue(Field f, Object value) throws ItemNotFound {
                            return out.setValue(field, value);
                        }
                    });
                }
            }
        });
        glQuery.aliasToField.forEach((field, gqlField) -> {
            if (gqlField.gqlGlobType() != null) {
                List<Node> nodes = children.get(field);
                if (!nodes.isEmpty()) {
                    Node firstNode = nodes.get(0);
                    if (field instanceof GlobField globField) {
                        out.set(globField, firstNode.buildResponse(gqlGlobCallerBuilder));
                    } else if (field instanceof GlobArrayField globArrayField) {
                        Glob[] res = new Glob[nodes.size()];
                        final Optional<Glob> parameters = gqlField.field().parameters();
                        final Optional<Comparator> comparator =
                                parameters.map(p -> {
                                    final Field sort = p.getType().findField("sort");
                                    if (sort != null) {
                                        final Optional<String> s = p.getOptNotEmpty(sort.asStringField());
                                        final Glob data1 = firstNode.data;
                                        if (data1 == null) {
                                            return Comparator.naturalOrder();
                                        }
                                        final Field field1 = data1.getType().getField(s.orElse(null));
                                        final Comparator comparing = Comparator.comparing((Node o) -> ((Comparable) o.data.getValue(field1)));
                                        return comparing;
                                    }
                                    return null;
                                });

                        int i = 0;
                        if (comparator.isPresent()) {
                            final ArrayList<Node> nodes1 = new ArrayList<>(nodes);
                            nodes1.sort(comparator.get());
                            for (Node node : nodes1) {
                                res[i++] = node.buildResponse(gqlGlobCallerBuilder);
                            }
                        } else {
                            for (Node node : nodes) {
                                res[i++] = node.buildResponse(gqlGlobCallerBuilder);
                            }
                        }
                        out.set(globArrayField, res);
                    } else {
                        throw new RuntimeException("Not managed.");
                    }
                } else {
                    if (field.hasAnnotation(GQLMandatory.KEY) && field instanceof GlobArrayField globArrayField) {
                        out.set(globArrayField, new Glob[0]);
                    }
                }
            }
        });
        return out;
    }

    public GlobType getType() {
        return data.getType();
    }
}
