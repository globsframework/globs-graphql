package org.globsframework.graphql;

import junit.framework.TestCase;
import org.globsframework.graphql.model.*;
import org.globsframework.graphql.parser.GqlField;
import org.globsframework.json.GSonUtils;
import org.globsframework.metamodel.impl.DefaultGlobModel;

import java.util.Map;

public class GQLQueryParserTest extends TestCase {

    public void testMutation() {
        GQLQueryParser gqlQueryParser = new GQLQueryParser(SchemaType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, CreateParam.TYPE));
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
        GQLQueryParser gqlQueryParser = new GQLQueryParser(SchemaType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE, CreateParam.TYPE));
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

    public void testFragment() {
        GQLQueryParser gqlQueryParser = new GQLQueryParser(SchemaType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE));
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