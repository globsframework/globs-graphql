package org.globsframework.graphql;

import org.globsframework.graphql.parser.antlr.GraphqlLexer;
import org.globsframework.graphql.parser.antlr.GraphqlParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.globsframework.graphql.parser.AntlrGQLVisitor;
import org.globsframework.graphql.parser.FragmentExtractor;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;

import java.util.Map;

public class GQLQueryParser {
    private final GlobType query;
    private final GlobModel globModel;

    public GQLQueryParser(GlobType query, GlobModel globModel) {
        this.query = query;
        this.globModel = globModel;
    }

    public GQLGlobType parse(String query, Map<String, String> variables) {
        CharStream charStream = CharStreams.fromString(query);
        GraphqlLexer graphqlLexer = new GraphqlLexer(charStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(graphqlLexer);
        GraphqlParser graphQLParser = new GraphqlParser(commonTokenStream);
        final GraphqlParser.DocumentContext document = graphQLParser.document();

        FragmentExtractor fragmentExtractor = new FragmentExtractor();
        document.accept(fragmentExtractor);

        AntlrGQLVisitor antlrGQLVisitor = new AntlrGQLVisitor(this.query, globModel, variables, fragmentExtractor.getFragments());
        document.accept(antlrGQLVisitor);

        return antlrGQLVisitor.complete();
    }
}
