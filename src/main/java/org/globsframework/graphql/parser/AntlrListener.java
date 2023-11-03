package org.globsframework.graphql.parser;

import org.globsframework.graphql.parser.antlr.GraphqlBaseListener;
import org.globsframework.graphql.parser.antlr.GraphqlParser;
import org.globsframework.graphql.GQLGlobType;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

public class AntlrListener extends GraphqlBaseListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AntlrListener.class);
    private final GlobType rootQuery;
    private final Deque<GqlGlobBuilder> trees = new ArrayDeque<>();
    private GQLGlobType gqlGlobType;

    public AntlrListener(GlobType rootQuery, GlobModel parameters) {
        this.rootQuery = rootQuery;
    }

    public void enterSelectionSet(GraphqlParser.SelectionSetContext ctx) {
        LOGGER.info("enterSelectionSet ");
    }

    public void exitSelectionSet(GraphqlParser.SelectionSetContext ctx) {
        LOGGER.info("exitSelectionSet ");
    }

    public void enterSelection(GraphqlParser.SelectionContext ctx) {
        LOGGER.info("enterSelection " + ctx.getText());
    }

    public void exitSelection(GraphqlParser.SelectionContext ctx) {
        LOGGER.info("exitSelection " + ctx.getText());
    }


    @Override
    public void enterName(GraphqlParser.NameContext ctx) {
        super.enterName(ctx);
    }

    @Override
    public void exitName(GraphqlParser.NameContext ctx) {
        super.exitName(ctx);
    }

    @Override
    public void enterArguments(GraphqlParser.ArgumentsContext ctx) {
        LOGGER.info("enterArguments " + ctx.getText());
    }

    @Override
    public void exitArguments(GraphqlParser.ArgumentsContext ctx) {
        LOGGER.info("exitArguments " + ctx.getText());
    }

    @Override
    public void enterArgument(GraphqlParser.ArgumentContext ctx) {
        LOGGER.info("enterArgument " + ctx.getText());
    }

    @Override
    public void exitArgument(GraphqlParser.ArgumentContext ctx) {
        LOGGER.info("exitArgument " + ctx.getText());
    }
}
