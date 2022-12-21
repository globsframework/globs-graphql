package org.globsframework.graphql;

import junit.framework.TestCase;
import org.globsframework.functional.FunctionalKeyBuilder;
import org.globsframework.functional.impl.DefaultFunctionalKeyBuilderFactory;
import org.globsframework.graphql.model.*;
import org.globsframework.graphql.parser.GqlField;
import org.globsframework.json.GSonUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.KeyAnnotationType;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.impl.DefaultGlobModel;
import org.globsframework.metamodel.impl.DefaultGlobTypeBuilder;
import org.globsframework.model.*;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.utils.collections.MultiMap;
import org.junit.Assert;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class GQLGlobCallerBuilderTest extends TestCase {

    private StringField firstName;
    private StringField lastName;
    private DateField birthDate;
    private StringField id;
    private DefaultGlobRepository globRepository;
    private GlobType humainType;


    public void setUp(){
        DefaultGlobTypeBuilder defaultGlobTypeBuilder = new DefaultGlobTypeBuilder("Humain");
        firstName = defaultGlobTypeBuilder.declareStringField("firstName");
        lastName = defaultGlobTypeBuilder.declareStringField("lastName");
        birthDate = defaultGlobTypeBuilder.declareDateField("birthDate");
        id = defaultGlobTypeBuilder.declareStringField("id", KeyAnnotationType.create(0));

        globRepository = new DefaultGlobRepository();
        humainType = defaultGlobTypeBuilder.get();
    }

    public void testLoad() {
        globRepository.create(humainType, FieldValue.value(id, "AZE"),
                FieldValue.value(firstName, "LA"),
                FieldValue.value(lastName, "GG"),
                FieldValue.value(birthDate, LocalDate.of(1980, 1, 1)))
        ;

        globRepository.create(humainType, FieldValue.value(id, "QDS"),
                FieldValue.value(firstName, "DSS"),
                FieldValue.value(lastName, "GCW"));

        globRepository.create(humainType, FieldValue.value(id, "YYY"),
                FieldValue.value(firstName, "DDD"),
                FieldValue.value(lastName, "QQQ"));

        MultiMap<String, String> friends = new MultiMap<>();
        friends.put("AZE", "QDS");
        friends.put("AZE", "YYY");
        friends.put("YYY", "QDS");

        GQLGlobCallerBuilder gqlGlobCallerBuilder = new GQLGlobCallerBuilder();

        FunctionalKeyBuilder functionalKeyBuilder = new DefaultFunctionalKeyBuilderFactory(humainType)
                .add(id).create();
        gqlGlobCallerBuilder.registerFKeyFetcher(Human.TYPE, functionalKeyBuilder, new GQLGlobFetcher<>() {
            @Override
            public CompletableFuture<Void> load(GQLGlobType gqlGlobType, GQLGlobCaller.GQLContext callContext, List<OnKey> onKeys) {
                for (OnKey parent : onKeys) {
                    parent.onNew().push(globRepository.get(KeyBuilder.create(humainType, parent.key().getValue(id))));
                }
                return CompletableFuture.completedFuture(null);
            }
        });

        gqlGlobCallerBuilder.registerLoader(Human.birthDate, new GQLGlobLoad<>() {
            @Override
            public CompletableFuture<Object> load(GqlField gqlField, GQLGlobCaller.GQLContext callContext, List<OnLoad> parents) {
                for (OnLoad parent : parents) {
                    parent.onNew().push(parent.parent());
                }
                return CompletableFuture.completedFuture(null);
            }
        });
        gqlGlobCallerBuilder.registerField(Human.BirthDate.day, birthDate.getGlobType(), (source, target) -> target.set(Human.BirthDate.day, source.get(birthDate).getDayOfMonth()));
        gqlGlobCallerBuilder.registerField(Human.BirthDate.month, birthDate.getGlobType(), (source, target) -> target.set(Human.BirthDate.month, source.get(birthDate).getMonthValue()));
        gqlGlobCallerBuilder.registerField(Human.BirthDate.year, birthDate.getGlobType(), (source, target) -> target.set(Human.BirthDate.year, source.get(birthDate).getYear()));

        gqlGlobCallerBuilder.registerLoader(QueryType.humain, new GQLGlobLoad<>() {
            @Override
            public CompletableFuture<Object> load(GqlField gqlField, GQLGlobCaller.GQLContext callContext, List<OnLoad> parents) {
                Glob parameters = gqlField.field().parameters().orElseThrow();
                if (parameters.isSet(HumanQuery.id)) {
                    String id = parameters.get(HumanQuery.id);
                    for (OnLoad parent : parents) {
                        parent.onNew().push(globRepository.get(KeyBuilder.create(humainType, id)));
                    }
                }
                return CompletableFuture.completedFuture(null);
            }
        });

        gqlGlobCallerBuilder.registerFKeyExtractor(Human.friends, id.getGlobType(), new GQLKeyExtractor<>() {
            @Override
            public CompletableFuture<Void> extract(GqlField gqlField, GQLGlobCaller.GQLContext callContext, List<OnExtract> parents) {
                for (OnExtract parent : parents) {
                    List<String> value = new ArrayList<>(friends.get(parent.parent().get(id)));
                    value.sort(String::compareTo);
                    for (String s : value) {
                        parent.onNew().push(functionalKeyBuilder.create()
                                .setValue(id, s).create());
                    }
                }
                return CompletableFuture.completedFuture(null);
            }
        });

        gqlGlobCallerBuilder.registerConnection(QueryType.humains, new GQLGlobConnectionLoad<>() {
            @Override
            public CompletableFuture<Void> load(GqlField gqlField, GQLGlobCaller.GQLContext callContext, List<OnConnectionLoad> parents) {
                final OnConnectionLoad onConnectionLoad = parents.get(0);
                List<Glob> all = globRepository.getAll(humainType);
                final Optional<String> order = gqlField.field().parameters().map(HumansQuery.orderBy);
                if (order.isPresent()) {
                    List<Glob> l = new ArrayList<Glob>(all);
                    l.sort(Comparator.comparing(g -> g.get(humainType.getField(order.get()).asStringField())));
                    all = l;
                }
                for (Glob glob : all) {
                    onConnectionLoad.onNew().push(glob);
                }
                onConnectionLoad.onCursor().push(new CursorInfo(false, false, 3));
                return CompletableFuture.completedFuture(null);
            }
        }, id, HumansQuery.orderBy);

        GQLGlobCaller gqlGlobCaller = gqlGlobCallerBuilder.build(SchemaType.TYPE,
                new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, Human.FriendQueryParam.TYPE));
        {
            final CompletableFuture<Glob> id1 = gqlGlobCaller.query("query toto {" +
                    "   humain(id: $ID) {" +
                    "     firstName" +
                    "     lastName" +
                    "     birthDate {" +
                    "       year" +
                    "     }" +
                    "     friends(sort: \"lastName\" name: []) {" +
                    "        firstName" +
                    "        friends(sort: \"lastName\") {" +
                    "           firstName" +
                    "           lastName" +
                    "        }" +
                    "     }" +
                    "   }" +
                    "}", Map.of("ID", "\"AZE\""), null);
            Glob query = id1.join();

            Assert.assertEquals("{\"humain\":{\"firstName\":\"LA\",\"lastName\":\"GG\",\"birthDate\":{\"year\":1980},\"friends\":[{\"firstName\":\"DSS\"},{\"firstName\":\"DDD\",\"friends\":[{\"firstName\":\"DSS\",\"lastName\":\"GCW\"}]}]}}",
                    GSonUtils.encode(query, false));
        }
        {
            final CompletableFuture<Glob> id1 = gqlGlobCaller.query("{" +
                    "   first: humain(id: $ID) {" +
                    "     ...common" +
                    "     }" +
                    "   second: humain(id: $ID) {" +
                    "     ...common" +
                    "     }" +
                    "}" +
                    "fragment common on Humain {" +
                    "   firstName" +
                    "   lastName" +
                    "}", Map.of("ID", "\"AZE\""), null);
            Glob query = id1.join();

            Assert.assertEquals("{\"first\":{\"firstName\":\"LA\",\"lastName\":\"GG\"},\"second\":{\"firstName\":\"LA\",\"lastName\":\"GG\"}}",
                    GSonUtils.encode(query, false));
        }
        {
            final CompletableFuture<Glob> id1 = gqlGlobCaller.query("{" +
                    "   humains(after: $after, orderBy: \"lastName\") {" +
                    "     totalCount" +
                    "     edges {" +
                    "       node {" +
                    "         __typename" +
                    "         firstName" +
                    "         lastName" +
                    "       }" +
                    "     }" +
                    "     pageInfo {" +
                    "         startCursor" +
                    "         endCursor" +
                    "      }" +
                    "}" +
                    "}", Map.of("after", "null"), null);
            Glob query = id1.join();
            final Glob decode = GSonUtils.decode(GSonUtils.encode(query, false), QueryType.TYPE);
            ((MutableGlob) decode.get(QueryType.humains)
                    .get(HumanConnection.pageInfo))
                    .set(GQLPageInfo.startCursor, "startXXXX")
                    .set(GQLPageInfo.endCursor, "endXXXX")
            ;

            Assert.assertEquals("{\"humains\":{\"totalCount\":3,\"edges\":[{\"node\":{\"firstName\":\"DSS\",\"lastName\":\"GCW\"}},{\"node\":{\"firstName\":\"LA\",\"lastName\":\"GG\"}},{\"node\":{\"firstName\":\"DDD\",\"lastName\":\"QQQ\"}}],\"pageInfo\":{\"startCursor\":\"startXXXX\",\"endCursor\":\"endXXXX\"}}}",
                    GSonUtils.encode(decode, false));

        }
    }
}