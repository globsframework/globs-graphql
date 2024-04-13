# globs-graphql
Because data are Glob building a graphql library is simple.
To use the API a Schema must be defined.
For exemple : 

 ```
 public class SchemaType {
    public static GlobType TYPE;

    @Target(QueryType.class)
    public static GlobField query;

    @Target(QueryMutation.class)
    public static GlobField mutation;

    static {
        GlobTypeLoaderFactory.create(SchemaType.class).load();
    }
 }
```

the query :
```
public class QueryType {
    public static GlobType TYPE;

    @GQLQueryParam_(HumanQuery.class)
    @Target(Human.class)
    public static GlobField humain;

    @GQLQueryParam_(HumansQuery.class)
    @Target(HumanConnection.class)
    public static GlobField humains;
...

```

The connection must follow the standard for a connection :
```
public class HumanConnection {
    public static GlobType TYPE;

    public static IntegerField totalCount;

    @Target(HumanEdgeConnection.class)
    public static GlobArrayField edges;

    @Target(GQLPageInfo.class)
    public static GlobField pageInfo;

    static {
        GlobTypeLoaderFactory.create(HumanConnection.class, "HumanConnection").load();
    }
}
```

Now we register functor to fetch glob and map field.
A node in the graph is represented by a glob that is push by the parent in the graph.
The type of the Glob is not the graphql type (so a field mapping is mandatory)

The class 
```
GQLGlobCallerBuilder gqlGlobCallerBuilder = new GQLGlobCallerBuilder();
```
is there to register the functor like :
```
gqlGlobCallerBuilder.registerSimpleField(Humain.firstName, DbHumain.firstName);
```
In fact, if the name is the same, the registerField is automatique.

A more complexe mapping : 
```
gqlGlobCallerBuilder.registerField(Human.BirthDate.day, birthDate.getGlobType(), (source, target) -> target.set(Human.BirthDate.day, source.get(birthDate).getDayOfMonth()));
```
It say : for the field Human.BirthDate.day if the GlobType of the node is a of type birthDate.getGlobType()
than apply the mapping to extract the day from the month.

Now to fetch a given Humain
```
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
```
The library group all the parent for a given level, it is why parents is a list: there is not a call for each node but a call for each level.
The humain has a mandatory parameter, we retrieve it, retrieve the associated glob (from the globRepository or from the db)
then the glob is push to each parent.

It is possible to retrieve glob using functionnalKey, it is usefull if the schema allow the access to the same object from different point.

After creating a functionalKeyBuilder, we register a FKeyFetcher that given a functionnalKey return the associated Glob (a cache can be used using the context)
The code bellow is to access a Humain using it's id 
```
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
```
And a KeyExtractor, we want the friends for each humain.
So for each parent we retrieve the id of it's friend and push the functionnalKey using parent.onNew()
```
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
```
The underlying code call the keyFetcher to associate each humain to their friends.

The last register it to managed connection with cursor and order.
```
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
                onConnectionLoad.onCursor().push(new CursorInfo(false, false, all.size()));
                return CompletableFuture.completedFuture(null);
            }
        }, id, HumansQuery.orderBy);

```

By declaring id and orderBy we allow the library to automatically create the next and previous field (in base64)
The cursor give the information for hasNext/hasPrevious.

The more realist query using db :
```
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
```
Using a ConnectionBuilder it will fetch the wanted size, the first cursor, order, etc.
It do all the job!
The cursor are encoded int base64 :
```eyJfa2luZCI6ImN1cnNvclR5cGUiLCJsYXN0SWQiOiI4NjQiLCJsYXN0T3JkZXJWYWx1ZSI6ImZpcnN0TmFtZSA2NSJ9``` -> ```{"_kind":"cursorType","lastId":"864","lastOrderValue":"firstName 65"}```
And used in the db query.

After the declaration of the functor we build a GQLGlobCaller
```
        final GQLGlobCaller<GQLGlobCaller.GQLContext> build =
                gqlGlobCallerBuilder.build(SchemaType.TYPE, new DefaultGlobModel(HumainQuery.Parameter.TYPE));
```
Then The query :
```
            final CompletableFuture<Glob> id1 = gqlGlobCaller.query("""
                    query myFriends {
                       humain(id: $ID) {
                            firstName     
                            lastName     
                            birthDate {  
                                   year     
                                 }     
                            friends(sort: "lastName", name: ["AA", "BB"]) {
                                   firstName        
                                   friends(sort: "lastName") {
                                      firstName           
                                      lastName        
                                   }     
                            }   
                      }
                    }""",
                    Map.of("ID", "\"AZE\""), null);
```

Last, it is possible to generate the schema using
```
        GlobSchemaGenerator globSchemaGenerator = new GlobSchemaGenerator(SchemaType.TYPE,
                new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, Human.FriendQueryParam.TYPE, CreateParam.TYPE, ComplexHumansQuery.TYPE));
        final String s = globSchemaGenerator.generateAll();
```
Given to Graphql-java lib, it is possible to expose the schema on the graphql api.