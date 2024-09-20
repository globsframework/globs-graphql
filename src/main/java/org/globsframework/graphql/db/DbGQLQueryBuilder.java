package org.globsframework.graphql.db;

import org.globsframework.core.metamodel.fields.Field;
import org.globsframework.core.model.Glob;
import org.globsframework.graphql.GQLGlobCallerBuilder;
import org.globsframework.json.GSonUtils;
import org.globsframework.sql.SqlConnection;
import org.globsframework.sql.constraints.Constraint;
import org.globsframework.sql.constraints.Constraints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.Consumer;

public class DbGQLQueryBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DbGQLQueryBuilder.class);
    private final Field id;
    private Constraint constraint = null;
    private Integer first;
    private DbGraphqlQuery.Last after;
    private Integer last;
    private DbGraphqlQuery.Last before;
    private DbGraphqlQuery.OrderBy orderByOpt;
    private Integer skip;

    public DbGQLQueryBuilder(Field id) {
        this.id = id;
    }

    public DbGQLQueryBuilder withConstraint(Constraint constraint) {
        this.constraint = Constraints.and(this.constraint, constraint);
        return this;
    }

    public void skip(Integer skip) {
        this.skip = skip;
    }

    public DbGQLQueryBuilder after(DbGraphqlQuery.Last after) {
        LOGGER.info("after : id " + after.id() + " value " + after.value());
        this.after = after;
        return this;
    }

    public DbGQLQueryBuilder afterB64(String after) {
        Optional.of(after)
                .map(Base64.getDecoder()::decode)
                .map(s -> new String(s, StandardCharsets.UTF_8))
                .map(s -> GSonUtils.decode(s, GQLGlobCallerBuilder.CursorType.TYPE))
                .map(a -> new DbGraphqlQuery.Last(
                        a.get(GQLGlobCallerBuilder.CursorType.lastId),
                        a.getOpt(GQLGlobCallerBuilder.CursorType.lastOrderValue)))
                .ifPresent(this::after);
        return this;
    }

    public DbGQLQueryBuilder first(int first) {
        this.first = first;
        return this;
    }

    public DbGQLQueryBuilder before(DbGraphqlQuery.Last before) {
        LOGGER.info("Before : id " + before.id() + " value " + before.value());
        this.before = before;
        return this;
    }

    public DbGQLQueryBuilder beforeB64(String before) {
        Optional.of(before)
                .map(Base64.getDecoder()::decode)
                .map(s -> new String(s, StandardCharsets.UTF_8))
                .map(s -> GSonUtils.decode(s, GQLGlobCallerBuilder.CursorType.TYPE))
                .map(a -> new DbGraphqlQuery.Last(
                        a.get(GQLGlobCallerBuilder.CursorType.lastId),
                        a.getOpt(GQLGlobCallerBuilder.CursorType.lastOrderValue)))
                .ifPresent(this::before);
        return this;
    }

    public DbGQLQueryBuilder last(int last) {
        this.last = last;
        return this;
    }

    public DbGQLQueryBuilder orderBy(Field field, DbGraphqlQuery.Order order) {
        this.orderByOpt = new DbGraphqlQuery.OrderBy(field, order);
        return this;
    }

    public DbGraphqlQuery build() {
        if (before != null) {
            return new DbGraphqlQuery() {
                final DbGraphqlQuery dbGraphqlQuery = new DefaultDbGraphqlQuery(id, last, before, orderByOpt != null ? orderByOpt.invert() :
                        new OrderBy(null, Order.desc), constraint, skip);

                @Override
                public int getTotal(SqlConnection db) {
                    return dbGraphqlQuery.getTotal(db);
                }

                @Override
                public CursorPosition gqlQuery(SqlConnection db, Consumer<Glob> consumer) {
                    ArrayList<Glob> data = new ArrayList<>();
                    final CursorPosition cursorPosition = dbGraphqlQuery.gqlQuery(db, data::add);
                    final ListIterator<Glob> globListIterator = data.listIterator(data.size());
                    while (globListIterator.hasPrevious()) {
                        Glob next = globListIterator.previous();
                        consumer.accept(next);
                    }
                    return new CursorPosition(cursorPosition.hasNext(), cursorPosition.hasPrevious());
                }
            };
        } else {
            return new DefaultDbGraphqlQuery(id, first, after, orderByOpt, constraint, skip);
        }
    }
}
