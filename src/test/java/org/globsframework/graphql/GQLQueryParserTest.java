package org.globsframework.graphql;

import junit.framework.TestCase;
import org.globsframework.core.metamodel.impl.DefaultGlobModel;
import org.globsframework.graphql.model.*;
import org.globsframework.graphql.parser.GqlField;
import org.globsframework.json.GSonUtils;

import java.util.Collection;
import java.util.Map;

public class GQLQueryParserTest extends TestCase {

    public void testMutation() {
        GQLQueryParser gqlQueryParser = new GQLQueryParser(SchemaType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, CreateParam.TYPE, ComplexHumansQuery.TYPE));
        gqlQueryParser.parse("""
                mutation {
                    createHumain(humain: $humain){
                       firstName
                       lastName
                  }
                }
                """, Map.of("humain", GSonUtils.encode(CreateParam.HumanInput.TYPE.instantiate()
                .set(CreateParam.HumanInput.firstName, "XX")
                .set(CreateParam.HumanInput.lastName, "YY"), false)));
    }

    public void testName() {
        GQLQueryParser gqlQueryParser = new GQLQueryParser(SchemaType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, CreateParam.TYPE, ComplexHumansQuery.TYPE));
        GQLGlobType parse = gqlQueryParser.parse("{" +
                "   humain(id: $AZE) {" +
                "     firstName" +
                "     lastName" +
                "     friends {" +
                "        firstName" +
                "     }" +
                "   }" +
                "}", Map.of("AZE", "\"AZE\""));
        assertEquals(1, parse.aliasToField.size());
        GqlField gqlField = parse.aliasToField.get(parse.outputType.findField(QueryType.humain.getName()));
        assertEquals("AZE", gqlField.field().parameters().map(HumanQuery.id).get());
        assertEquals(3, gqlField.gqlGlobType().aliasToField.size());
    }

    public void testWithArray() {
        GQLQueryParser gqlQueryParser = new GQLQueryParser(SchemaType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, CreateParam.TYPE, ComplexHumansQuery.TYPE, Human.FriendQueryParam.TYPE));
        {
            final GQLGlobType type = gqlQueryParser.parse("query toto {" +
                    "  humain{" +
                    "     friends(sort: \"lastName\" name: [\"a\",\"b\"]) {" +
                    "        firstName" +
                    "     }" +
                    "   }" +
                    "  }" +
                    "}", Map.of("ID", "\"AZE\""));
            final Collection<GqlField> values = type.aliasToField.values();
        }
    }

    public void testWithArrayAndEmptyVariable() {
        GQLQueryParser gqlQueryParser = new GQLQueryParser(SchemaType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, CreateParam.TYPE, ComplexHumansQuery.TYPE, Human.FriendQueryParam.TYPE));
        {
            final GQLGlobType type = gqlQueryParser.parse("query toto {" +
                    "  humain{" +
                    "     friends(sort: \"lastName\" name: [$AAA,\"b\"]) {" +
                    "        firstName" +
                    "     }" +
                    "   }" +
                    "  }" +
                    "}", Map.of());
            final Collection<GqlField> values = type.aliasToField.values();
        }
    }


    public void testWithQuery() {
        GQLQueryParser gqlQueryParser = new GQLQueryParser(SchemaType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE));
        GQLGlobType parse = gqlQueryParser.parse("query {" +
                "   humain(id: $AZE) {" +
                "     firstName" +
                "     lastName" +
                "     friends {" +
                "        firstName" +
                "     }" +
                "   }" +
                "}", Map.of("AZE", "\"AZE\""));

    }

    public void testWithComplexQuery() {
        GQLQueryParser gqlQueryParser = new GQLQueryParser(SchemaType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, ComplexHumansQuery.TYPE));
        GQLGlobType parse = gqlQueryParser.parse("""
                query {
                   complexHumains(subInfo: $VAR) {
                     totalCount
                   }
                }
                """, Map.of("VAR", GSonUtils.encode(ComplexHumansQuery.TYPE.instantiate()
                .set(ComplexHumansQuery.subInfo, ComplexHumansQuery.Subinfo.TYPE.instantiate()
                        .set(ComplexHumansQuery.Subinfo.firstName, "titi")), false)));

    }

    public void testWithEmptyVariableInQueryIsOK() {
        GQLQueryParser gqlQueryParser = new GQLQueryParser(SchemaType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, ComplexHumansQuery.TYPE));
        GQLGlobType parse = gqlQueryParser.parse("""
                query {
                   complexHumains(subInfo: $VAR) {
                     totalCount
                   }
                }
                """, Map.of());

    }

    public void testWithComplexInParamAndEmptyVar() {
        GQLQueryParser gqlQueryParser = new GQLQueryParser(SchemaType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, ComplexHumansQuery.TYPE));
        ;
        GQLGlobType parse = gqlQueryParser.parse("""
                        query {
                           complexHumains(who: "chat", subInfo: { firstName: $VAR, lastName: "toto" }) {
                             totalCount
                           }
                        }
                        """
                , Map.of());
    }

    public void testWithComplexInParam() {
        GQLQueryParser gqlQueryParser = new GQLQueryParser(SchemaType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, ComplexHumansQuery.TYPE));
        final String titi = GSonUtils.encode(ComplexHumansQuery.TYPE.instantiate()
                .set(ComplexHumansQuery.subInfo, ComplexHumansQuery.Subinfo.TYPE.instantiate()
                        .set(ComplexHumansQuery.Subinfo.firstName, "titi")), false);
        ;
        GQLGlobType parse = gqlQueryParser.parse("""
                        query {
                           complexHumains(who: "chat", subInfo: { firstName: "titi", lastName: "toto" }) {
                             totalCount
                           }
                        }
                        """
                , Map.of());
    }

    /*
    '{"query":"mutation{\n  createTreatment(input: { name: \"xxx\", description: \"yyy\", namespace: \"ddd\"}){\n    name\n  }\n}","variables":{}}' --compressed

     */
    public void testFragment() {
        GQLQueryParser gqlQueryParser = new GQLQueryParser(SchemaType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, ComplexHumansQuery.TYPE));
        GQLGlobType parse = gqlQueryParser.parse("{" +
                "   first: humain(id: $AZE) {" +
                "     ...common" +
                "   }" +
                "   second: humain(id: $AZE) {" +
                "     ...common" +
                "   }" +
                "}" +
                "" +
                "fragment common on Humain {" +
                "  __typename" +
                "  lastName" +
                "  friends {" +
                "     firstName" +
                "  }" +
                "}" +
                "", Map.of("AZE", "\"AZE\""));
        assertEquals(2, parse.aliasToField.size());
        final GqlField humain = parse.aliasToField.values().iterator().next();
        assertEquals(3, humain.gqlGlobType().aliasToField.size());
        assertNotNull(humain.gqlGlobType().aliasToField.get(humain.gqlGlobType().outputType.findField("__typename")));

    }
}
