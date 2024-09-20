package org.globsframework.graphql.db;

import org.globsframework.core.metamodel.fields.Field;
import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;
import org.globsframework.graphql.GQLGlobConnectionLoad;
import org.globsframework.graphql.parser.GqlField;
import org.globsframework.sql.SqlConnection;
import org.globsframework.sql.constraints.Constraint;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class ConnectionBuilder {
    private final Field dbKey;
    private Glob emptyParam;
    private StringField after;
    private IntegerField first;
    private StringField before;
    private IntegerField last;
    private IntegerField skip;
    private StringField orderBy;
    private StringField dataOrder; // asc, desc

    public ConnectionBuilder(Field dbKey) {
        this.dbKey = dbKey;
    }

    public static ConnectionBuilder withDbKey(Field dbKey) {
        return new ConnectionBuilder(dbKey);
    }

    public ConnectionBuilder withParam(Glob emptyParam, StringField after, IntegerField first,
                                       StringField before, IntegerField last, IntegerField skip) {
        this.emptyParam = emptyParam;
        this.after = after;
        this.first = first;
        this.before = before;
        this.last = last;
        this.skip = skip;
        return this;
    }

    public ConnectionBuilder withOrder(StringField orderBy, StringField dataOrder) {
        this.orderBy = orderBy;
        this.dataOrder = dataOrder;
        return this;
    }


    public CompletableFuture<Void> scanAll(GqlField gqlField,
                                           GQLGlobConnectionLoad.OnConnectionLoad onLoad, Constraint constraint,
                                           SqlConnection sqlConnection) {
        final Glob parameters = gqlField.field().parameters()
                .orElse(emptyParam);

        final Optional<Field> totalCount = gqlField.gqlGlobType().outputType.findOptField("totalCount");

        DbGQLQueryBuilder builder = new DbGQLQueryBuilder(dbKey);

        parameters.getOpt(after)
                .filter(Predicate.not(String::isEmpty))
                .ifPresent(builder::afterB64);
        builder.first(parameters.getOpt(first)
                .map(max -> Math.min(100, max))
                .orElse(10));

        parameters.getOpt(before)
                .filter(Predicate.not(String::isEmpty))
                .ifPresent(builder::beforeB64);
        builder.last(parameters.getOpt(last)
                .map(max -> Math.min(100, max))
                .orElse(10));
        parameters.getOpt(skip)
                .ifPresent(builder::skip);

        parameters.getOpt(orderBy)
                .map(o -> builder.orderBy(dbKey.getGlobType().getField(o),
                        parameters.get(dataOrder, "asc").equals("asc") ?
                                DbGraphqlQuery.Order.asc : DbGraphqlQuery.Order.desc));
        builder.withConstraint(constraint);
        final DbGraphqlQuery build = builder.build();
        final DbGraphqlQuery.CursorPosition cursorPosition =
                build.gqlQuery(sqlConnection, onLoad.onNew()::push);
        final int total = totalCount.map(f -> build.getTotal(sqlConnection))
                .orElse(-1);
        onLoad.onCursor().push(new GQLGlobConnectionLoad.CursorInfo(cursorPosition.hasPrevious(), cursorPosition.hasNext(), total));
        return CompletableFuture.completedFuture(null);
    }

}
