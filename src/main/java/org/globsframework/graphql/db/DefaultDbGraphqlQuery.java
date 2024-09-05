package org.globsframework.graphql.db;

import org.globsframework.graphql.GQLGlobCallerBuilder;
import org.globsframework.json.GSonUtils;
import org.globsframework.metamodel.fields.Field;
import org.globsframework.model.Glob;
import org.globsframework.sql.SelectBuilder;
import org.globsframework.sql.SelectQuery;
import org.globsframework.sql.SqlConnection;
import org.globsframework.sql.constraints.Constraint;
import org.globsframework.sql.constraints.Constraints;
import org.globsframework.streams.accessors.LongAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DefaultDbGraphqlQuery implements DbGraphqlQuery {
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultDbGraphqlQuery.class);
    private final Field idField;
    private final Optional<Integer> top;
    private final Optional<Last> after;
    private final Optional<OrderBy> orderByOpt;
    private final Constraint additionalConstraint;
    private final Optional<Integer> skip;

    public DefaultDbGraphqlQuery(Field idField, Integer top, Last after, OrderBy orderByOpt, Constraint additionalConstraint, Integer skip) {
        this.idField = idField;
        this.top = Optional.ofNullable(top);
        this.after = Optional.ofNullable(after);
        this.orderByOpt = Optional.ofNullable(orderByOpt);
        this.additionalConstraint = additionalConstraint;
        this.skip = Optional.ofNullable(skip);
    }

    public int getTotal(SqlConnection db) {
        final SelectBuilder queryBuilder = db.getQueryBuilder(idField.getGlobType(), additionalConstraint);
        final LongAccessor count = queryBuilder.count(idField);
        try (SelectQuery selectQuery = queryBuilder.getQuery()) {
            try (Stream<?> stream = selectQuery.executeAsStream()) {
                return stream.findFirst().map(g -> ((int) count.getValue(0)))
                        .orElse(-1);
            }
        }
    }

    public CursorPosition gqlQuery(SqlConnection db, Consumer<Glob> consumer) {
        try {
            boolean hasPrevious = false;
            Constraint constraint = null;
            Optional<Field> orderByField = orderByOpt.map(OrderBy::field);
            final Optional<Order> wantedOrder = orderByOpt.map(OrderBy::order);
            if (after.isPresent()) {
                hasPrevious = true;
                if (orderByField.isPresent()) {
                    final Optional<String> afterValue = after.get().value();
                    if (afterValue.isPresent()) {
                        final Field field = orderByField.get();
                        final String value = afterValue.get();
                        final Object convertedValue = GQLGlobCallerBuilder.ToStringSerialiser.toObject(field, value);
                        if (wantedOrder.isEmpty() || wantedOrder.get() == Order.asc) {
                            constraint = Constraints.or(
                                    Constraints.and(Constraints.equalsObject(field, convertedValue),
                                            Constraints.strictlyGreater(idField, after.get().id())),
                                    Constraints.and(Constraints.strictlyGreater(field, convertedValue))
                            );
                        } else {
                            constraint = Constraints.or(
                                    Constraints.and(Constraints.equalsObject(field, convertedValue),
                                            Constraints.strictlyLessUnchecked(idField, after.get().id())),
                                    Constraints.and(Constraints.strictlyLessUnchecked(field, convertedValue)));
                        }
                    } else {
                        if (wantedOrder.isEmpty() || wantedOrder.get() == Order.asc) {
                            constraint = Constraints.strictlyGreater(idField, after.get().id());
                        } else {
                            constraint = Constraints.strictlyLessUnchecked(idField, after.get().id());
                        }
                    }
                } else {
                    if (wantedOrder.isEmpty() || wantedOrder.get() == Order.asc) {
                        constraint = Constraints.strictlyGreater(idField, after.get().id());
                    } else {
                        constraint = Constraints.strictlyLessUnchecked(idField, after.get().id());
                    }
                }
            }
            constraint = Constraints.and(constraint, additionalConstraint);
            SelectBuilder queryBuilder = db.getQueryBuilder(idField.getGlobType(), constraint);
            top.ifPresent(n -> queryBuilder.top(n + 1));
            if (orderByField.isPresent()) {
                final Order order = orderByOpt.get().order();
                if (wantedOrder.isEmpty() || order == Order.asc) {
                    queryBuilder.orderAsc(orderByField.get());
                    queryBuilder.orderAsc(idField);
                } else {
                    queryBuilder.orderDesc(orderByField.get());
                    queryBuilder.orderDesc(idField);
                }
            } else {
                if (wantedOrder.isEmpty() || wantedOrder.get() == Order.asc) {
                    queryBuilder.orderAsc(idField);
                }
                else {
                    queryBuilder.orderDesc(idField);
                }
            }
            skip.map(queryBuilder::skip);
            SelectQuery query = queryBuilder.selectAll().getQuery();
            final CountConsumer count = new CountConsumer(top.orElse(-1), consumer, idField);
            query.executeAsGlobStream().forEach(count);
            boolean hasNext = top.map(m -> m + 1 == count.count).orElse(false);
            return new CursorPosition(count.count != 0 && hasPrevious, hasNext);
        } catch (Exception e) {
            final String msg = "Fail to query " + idField.getGlobType().getName();
            LOGGER.error(msg, e);
            throw new RuntimeException(e);
        }
    }

    public static class CountConsumer implements Consumer<Glob> {
        int count = 0;
        private int maxCount;
        private Consumer<Glob> next;
        private Field idField;

        public CountConsumer(int maxCount, Consumer<Glob> next, Field idField) {
            this.maxCount = maxCount;
            this.next = next;
            this.idField = idField;
        }

        public void accept(Glob t) {
            ++count;
            if (maxCount == -1 || count <= maxCount) {
                next.accept(t);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("push " + GSonUtils.encode(t, true));
                } else if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("push " + t.getValue(idField));
                }
            }
        }
    }
}
