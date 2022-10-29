package org.globsframework.graphql;

import junit.framework.TestCase;
import org.globsframework.graphql.model.HumanQuery;
import org.globsframework.graphql.model.HumansQuery;
import org.globsframework.graphql.model.QueryType;
import org.globsframework.graphql.parser.GqlField;
import org.globsframework.metamodel.impl.DefaultGlobModel;

import java.util.Map;

public class GQLQueryParserTest extends TestCase {


    public void testName() {
        GQLQueryParser gqlQueryParser = new GQLQueryParser(QueryType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE));
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
        GQLQueryParser gqlQueryParser = new GQLQueryParser(QueryType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE));
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
        GQLQueryParser gqlQueryParser = new GQLQueryParser(QueryType.TYPE, new DefaultGlobModel(HumanQuery.TYPE, HumansQuery.TYPE));
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