package org.globsframework.graphql.parser;

import org.globsframework.graphql.parser.antlr.GraphqlBaseVisitor;
import org.globsframework.graphql.parser.antlr.GraphqlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class FragmentExtractor extends GraphqlBaseVisitor<FragmentExtractor> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentExtractor.class);
    private Map<String, GraphqlParser.SelectionSetContext> fragments = new HashMap<>();


    public Map<String, GraphqlParser.SelectionSetContext> getFragments() {
        return fragments;
    }

    @Override
    public FragmentExtractor visitFragmentDefinition(GraphqlParser.FragmentDefinitionContext ctx) {
        LOGGER.debug("visitFragmentDefinition " + ctx.getText());
        Extract extract = new Extract();
        extract.visitFragmentDefinition(ctx);
        fragments.put(extract.name, extract.selectionSet);
        return this;
    }

    public static class Extract extends GraphqlBaseVisitor<Extract> {
        String name;
        GraphqlParser.SelectionSetContext selectionSet;

        public Extract visitFragmentName(GraphqlParser.FragmentNameContext ctx) {
            name = ctx.getText();
            return super.visitFragmentName(ctx);
        }

        public Extract visitSelectionSet(GraphqlParser.SelectionSetContext ctx) {
            selectionSet = ctx;
            return this;
        }
    }

}
