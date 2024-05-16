package org.globsframework.graphql.db;

import org.globsframework.graphql.GQLGlobCaller;
import org.globsframework.graphql.GQLGlobCallerBuilder;
import org.globsframework.graphql.GQLGlobConnectionLoad;
import org.globsframework.graphql.model.GQLMandatory_;
import org.globsframework.graphql.model.GQLPageInfo;
import org.globsframework.graphql.model.GQLQueryParam_;
import org.globsframework.graphql.parser.GqlField;
import org.globsframework.json.GSonUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.InitUniqueGlob;
import org.globsframework.metamodel.annotations.KeyField;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.GlobArrayField;
import org.globsframework.metamodel.fields.GlobField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.impl.DefaultGlobModel;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcConnection;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DefaultDbGraphqlQueryTest {

    @Test
   public void name() {
        JdbcSqlService sqlService = new JdbcSqlService("jdbc:hsqldb:.", "sa", "");
        ArrayList<Glob> data = new ArrayList<>();
        final int dbLine = 1000;
        for (int i = 0; i < dbLine; i++) {
            data.add(DbHumain.TYPE.instantiate()
                    .set(DbHumain.uuid, "" + i)
                    .set(DbHumain.firstName, "firstName " + ((i % 100) + 1))
                    .set(DbHumain.lastName, "lastName " + i));
        }
        final JdbcConnection db = sqlService.getDb();
        db.createTable(DbHumain.TYPE);
        db.populate(data);

        GQLGlobCallerBuilder<GQLGlobCaller.GQLContext> gqlGlobCallerBuilder = new GQLGlobCallerBuilder<>();
        gqlGlobCallerBuilder.registerConnection(HumainQuery.humains, new GQLGlobConnectionLoad<GQLGlobCaller.GQLContext>() {
            @Override
            public CompletableFuture<Void> load(GqlField gqlField, GQLGlobCaller.GQLContext callContext, List<OnConnectionLoad> parents) {
                return
                        ConnectionBuilder.withDbKey(DbHumain.uuid)
                                .withParam(HumainQuery.Parameter.EMPTY, HumainQuery.Parameter.after,
                                        HumainQuery.Parameter.first, HumainQuery.Parameter.before,
                                        HumainQuery.Parameter.last, HumainQuery.Parameter.skip)
                                .withOrder(HumainQuery.Parameter.orderBy, HumainQuery.Parameter.order)
                                .scanAll(gqlField, parents.get(0), null, db);
            }
        }, DbHumain.uuid, HumainQuery.Parameter.orderBy);

        final GQLGlobCaller<GQLGlobCaller.GQLContext> build =
                gqlGlobCallerBuilder.build(SchemaType.TYPE, new DefaultGlobModel(HumainQuery.Parameter.TYPE));

        String before = null;
        String after = null;
        int count = 0;
        boolean hasMore;
        do {
            final CompletableFuture<Glob> query = query(build, after, null);
            final Glob result = query.join();
            final String encode = GSonUtils.encode(result, false);
            final Glob queryResult = GSonUtils.decode(encode, HumainQuery.TYPE);
            final Glob connection = queryResult.get(HumainQuery.humains);
            Assert.assertEquals(dbLine, connection.get(HumainQuery.Connection.totalCount).intValue());
            final int length = connection.getOrEmpty(HumainQuery.Connection.edges).length;
            final Glob pageInfo = connection.get(HumainQuery.Connection.pageInfo);
            if (length != 0) {
                count += length;
                after = pageInfo.get(GQLPageInfo.endCursor);
                before = pageInfo.get(GQLPageInfo.startCursor);
            }
            hasMore = pageInfo.get(GQLPageInfo.hasNextPage);
        } while (hasMore);
        Assert.assertEquals(dbLine, count);

        count = 0;
        do {
            final CompletableFuture<Glob> query = query(build, null, before);
            final Glob result = query.join();
            final String encode = GSonUtils.encode(result, false);
            final Glob queryResult = GSonUtils.decode(encode, HumainQuery.TYPE);
            final Glob connection = queryResult.get(HumainQuery.humains);
            Assert.assertEquals(dbLine, connection.get(HumainQuery.Connection.totalCount).intValue());
            count += connection.getOrEmpty(HumainQuery.Connection.edges).length;
            final Glob pageInfo = connection.get(HumainQuery.Connection.pageInfo);
            before = pageInfo.get(GQLPageInfo.startCursor);
            hasMore = pageInfo.get(GQLPageInfo.hasPreviousPage);
        } while (hasMore);
        Assert.assertEquals(dbLine - 2, count); // -2 pour la page actuelle
    }

    private static CompletableFuture<Glob> query(GQLGlobCaller<GQLGlobCaller.GQLContext> build, String after, String before) {
        final CompletableFuture<Glob> query = build.query("{" +
                        "  humains(first: 2, after: $after, before: $before, orderBy: firstName) {" +
                        "    totalCount" +
                        "    edges {" +
                        "      node {" +
                        "        uuid" +
                        "        firstName" +
                        "      }" +
                        "    }" +
                        "    pageInfo {" +
                        "      hasNextPage" +
                        "      hasPreviousPage" +
                        "      startCursor" +
                        "      endCursor" +
                        "    }" +
                        "  }" +
                        "}", Map.of("after", after == null ? "null" : "\"" + after + "\"",
                        "before", before == null ? "null" : "\"" + before + "\""),
                new GQLGlobCaller.GQLContext() {
                });
        return query;
    }

    public static class SchemaType {
        public static GlobType TYPE;

        @Target(HumainQuery.class)
        public static GlobField query;
        static {
            GlobTypeLoaderFactory.create(SchemaType.class).load();
        }
    }

    public static class HumainQuery {
        public static GlobType TYPE;

        @Target(Connection.class)
        @GQLQueryParam_(Parameter.class)
        public static GlobField humains;

        static {
            GlobTypeLoaderFactory.create(HumainQuery.class).load();
        }

        public static class Parameter {
            public static GlobType TYPE;

            @InitUniqueGlob
            public static Glob EMPTY;

            public static IntegerField first;

            public static StringField after;

            public static IntegerField last;

            public static StringField before;

            public static IntegerField skip;

            public static StringField order; // asc, desc ?

            public static StringField orderBy; //

            static {
                GlobTypeLoaderFactory.create(Parameter.class).load();
            }
        }

        public static class Connection {
            public static GlobType TYPE;

            public static IntegerField totalCount;

            @Target(Hedge.class)
            public static GlobArrayField edges;

            @Target(GQLPageInfo.class)
            @GQLMandatory_
            public static GlobField pageInfo;

            static {
                GlobTypeLoaderFactory.create(Connection.class).load();
            }
        }

        public static class Hedge {
            public static GlobType TYPE;

            @Target(Humain.class)
            public static GlobField node;

            static {
                GlobTypeLoaderFactory.create(Hedge.class).load();
            }
        }

    }

    public static class Humain {
        public static GlobType TYPE;

        @KeyField
        public static StringField uuid;

        public static StringField firstName;

        public static StringField lastName;

        static {
            GlobTypeLoaderFactory.create(Humain.class).load();
        }
    }

    public static class DbHumain {
        public static GlobType TYPE;

        @KeyField
        public static StringField uuid;

        public static StringField firstName;

        public static StringField lastName;

        static {
            GlobTypeLoaderFactory.create(DbHumain.class).load();
        }
    }
}