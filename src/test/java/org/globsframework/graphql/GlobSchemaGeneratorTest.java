package org.globsframework.graphql;

import junit.framework.TestCase;
import org.globsframework.core.metamodel.impl.DefaultGlobModel;
import org.globsframework.graphql.model.*;
import org.junit.Assert;

public class GlobSchemaGeneratorTest extends TestCase {

    public void testGenerateSchema() {
        GlobSchemaGenerator globSchemaGenerator = new GlobSchemaGenerator(SchemaType.TYPE,
                new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, Human.FriendQueryParam.TYPE, CreateParam.TYPE, ComplexHumansQuery.TYPE));
        final String s = globSchemaGenerator.generateAll();
        System.out.println(s);
        Assert.assertTrue(
                s.contains("""
                        scalar Date
                        scalar DateTime
                        scalar Long
                        """));
        Assert.assertTrue(
                s.contains("""
                        schema  {
                        query : Query
                        mutation : Mutation
                        }
                        """));
        Assert.assertTrue(s.contains("""
                type Query {
                humain(id:String) : Human
                humains(first:Int, after:String, orderBy:String, order:String, startedAt:DateTime) : HumanConnection
                complexHumains(who:String, subInfo:Subinfo) : HumanConnection
                }
                """));
        Assert.assertTrue(s.contains("""
                type Human {
                id : ID
                surName : String
                firstName : String
                lastName : String
                birthDate : BirthDate
                friends(sort:String, name:[String]) : [Human]
                }
                """));
        Assert.assertTrue(s.contains("""
                type BirthDate {
                day : Int
                month : Int
                year : Int
                }
                """));
        Assert.assertTrue(s.contains("""
                type HumanConnection {
                totalCount : Int
                edges : [HumanEdge]
                pageInfo : GQLPageInfo
                }
                """));
        Assert.assertTrue(s.contains("""
                type HumanEdge {
                node : Human
                cursor : String
                }
                """));
        Assert.assertTrue(s.contains("""
                type GQLPageInfo {
                startCursor : String
                endCursor : String
                hasNextPage : Boolean!
                hasPreviousPage : Boolean!
                }
                """));
        Assert.assertTrue(s.contains("""
                type Mutation {
                createHumain(humain:HumanInput) : Human
                }
                """));
        Assert.assertTrue(s.contains("""
                input HumanInput {
                firstName : String
                lastName : String
                }
                """));
        Assert.assertTrue(s.contains("""
                input Subinfo {
                firstName : String
                lastName : String
                }
                """));
    }

//    public void testQuery() {
//        GlobSchemaGenerator globSchemaGenerator = new GlobSchemaGenerator(SchemaType.TYPE,
//                new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, Human.FriendQueryParam.TYPE, CreateParam.TYPE, ComplexHumansQuery.TYPE));
//
//        SchemaParser schemaParser = new SchemaParser();
//        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(globSchemaGenerator.generateAll());
//
//        GraphQLCodeRegistry.Builder newCodeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();
//        GraphQLCodeRegistry codeRegistry = newCodeRegistryBuilder
//                .defaultDataFetcher(environment -> {
//                    GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();
//                    String name = fieldDefinition.getName();
//                    return environment1 -> {
//                        Glob glob = environment1.getSource();
//                        return glob.getValue(glob.getType().getField(name));
//                    };
//                }).build();
//
//        RuntimeWiring runtimeWiring = newRuntimeWiring()
//                .codeRegistry(codeRegistry)
//                .scalar(GraphQLScalarType.newScalar().name("Date")
//                        .coercing(new GraphqlStringCoercing())
//                        .build())
//                .scalar(GraphQLScalarType.newScalar().name("DateTime")
//                        .coercing(new GraphqlStringCoercing())
//                        .build())
//                .scalar(GraphQLScalarType.newScalar().name("Long")
//                        .coercing(new GraphqlStringCoercing())
//                        .build())
//                .type("Query", builder ->
//                        builder.dataFetcher(QueryType.humains.getName(), new DataFetcher() {
//                            @Override
//                            public Object get(DataFetchingEnvironment environment) throws Exception {
//                                return null;
//                            }
//                        }))
//                .build();
//
//        SchemaGenerator schemaGenerator = new SchemaGenerator();
//        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
//
//        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();
//        graphQL.execute("{" +
//                "  humains {" +
//                "    edges {" +
//                "     node {" +
//                "        firstName" +
//                "        lastName" +
//                "     }" +
//                "   }" +
//                "  }" +
//                "}");
//    }

}
