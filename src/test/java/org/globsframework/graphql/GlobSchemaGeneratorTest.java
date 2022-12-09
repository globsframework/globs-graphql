package org.globsframework.graphql;

import graphql.GraphQL;
import graphql.scalar.GraphqlStringCoercing;
import graphql.schema.*;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import junit.framework.TestCase;
import org.globsframework.graphql.model.Human;
import org.globsframework.graphql.model.HumanQuery;
import org.globsframework.graphql.model.HumansQuery;
import org.globsframework.graphql.model.QueryType;
import org.globsframework.metamodel.impl.DefaultGlobModel;
import org.globsframework.model.Glob;
import org.junit.Assert;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class GlobSchemaGeneratorTest extends TestCase {

    public void testGenerateSchema() {
        GlobSchemaGenerator globSchemaGenerator = new GlobSchemaGenerator(QueryType.TYPE,
                new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, Human.FriendQueryParam.TYPE));
        Assert.assertEquals("scalar Date\n" +
                "scalar DateTime\n" +
                "scalar Long\n" +
                "type Query {\n" +
                "humain(id:String) : human\n" +
                "humains(first:Int, after:String, orderBy:String, order:String, startedAt:DateTime) : HumanConnection\n" +
                "}\n" +
                "\n" +
                "\n" +
                "\n" +
                "type human {\n" +
                "id : ID\n" +
                "surName : String\n" +
                "firstName : String\n" +
                "lastName : String\n" +
                "birthDate : birthDate\n" +
                "friends(sort:String, name:[String]) : [human]\n" +
                "}\n" +
                "\n" +
                "\n" +
                "\n" +
                "type birthDate {\n" +
                "day : Int\n" +
                "month : Int\n" +
                "year : Int\n" +
                "}\n" +
                "\n" +
                "\n" +
                "\n" +
                "type HumanConnection {\n" +
                "totalCount : Int\n" +
                "edges : [HumanEdge]\n" +
                "pageInfo : gQLPageInfo\n" +
                "}\n" +
                "\n" +
                "\n" +
                "\n" +
                "type HumanEdge {\n" +
                "node : human\n" +
                "cursor : String\n" +
                "}\n" +
                "\n" +
                "\n" +
                "\n" +
                "type gQLPageInfo {\n" +
                "startCursor : String\n" +
                "endCursor : String\n" +
                "hasNextPage : Boolean!\n" +
                "hasPreviousPage : Boolean!\n" +
                "}\n" +
                "\n" +
                "\n" +
                "\n", globSchemaGenerator.generateAll());
    }

    public void testQuery() {
        GlobSchemaGenerator globSchemaGenerator = new GlobSchemaGenerator(QueryType.TYPE,
                new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, Human.FriendQueryParam.TYPE));

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(globSchemaGenerator.generateAll());

        GraphQLCodeRegistry.Builder newCodeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();
        GraphQLCodeRegistry codeRegistry = newCodeRegistryBuilder
                .defaultDataFetcher(environment -> {
                    GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();
                    String name = fieldDefinition.getName();
                    return environment1 -> {
                        Glob glob = environment1.getSource();
                        return glob.getValue(glob.getType().getField(name));
                    };
                }).build();

        RuntimeWiring runtimeWiring = newRuntimeWiring()
                .codeRegistry(codeRegistry)
                .scalar(GraphQLScalarType.newScalar().name("Date")
                        .coercing(new GraphqlStringCoercing())
                        .build())
                .scalar(GraphQLScalarType.newScalar().name("DateTime")
                        .coercing(new GraphqlStringCoercing())
                        .build())
                .scalar(GraphQLScalarType.newScalar().name("Long")
                        .coercing(new GraphqlStringCoercing())
                        .build())
                .type("Query", builder ->
                        builder.dataFetcher(QueryType.humains.getName(), new DataFetcher() {
                            @Override
                            public Object get(DataFetchingEnvironment environment) throws Exception {
                                return null;
                            }
                        }))
                .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();
        graphQL.execute("{" +
                "  humains {" +
                "    edges {" +
                "     node {" +
                "        firstName" +
                "        lastName" +
                "     }" +
                "   }" +
                "  }" +
                "}");
    }

}