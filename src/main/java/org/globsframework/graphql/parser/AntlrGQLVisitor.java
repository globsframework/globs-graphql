package org.globsframework.graphql.parser;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import graphql.parser.antlr.GraphqlBaseVisitor;
import graphql.parser.antlr.GraphqlParser;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.globsframework.graphql.GQLGlobType;
import org.globsframework.json.ReadJsonWithReaderFieldVisitor;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.MutableGlob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

public class AntlrGQLVisitor extends GraphqlBaseVisitor<AntlrGQLVisitor> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AntlrGQLVisitor.class);
    private static final ReadJsonWithReaderFieldVisitor fieldVisitor = new ReadJsonWithReaderFieldVisitor();
    private final Map<String, String> variables;
    private Map<String, GraphqlParser.SelectionSetContext> fragments;
    private final Deque<GqlGlobBuilder> trees = new ArrayDeque<>();
    private GQLGlobType gqlGlobType;

    public AntlrGQLVisitor(GlobType rootQuery, GlobModel paramTypes, Map<String, String> variables, Map<String, GraphqlParser.SelectionSetContext> fragments) {
        this.variables = variables;
        this.fragments = fragments;
        GQLGlobSelection selection = new GQLGlobSelection(rootQuery, paramTypes, gqlGlobType1 -> gqlGlobType = gqlGlobType1);
        trees.push(selection);
    }

    public GQLGlobType complete() {
        trees.element().complete();
        return gqlGlobType;
    }


    static class Arguments extends GraphqlBaseVisitor<Arguments> {
        private final MutableGlob mutableGlob;
        private final Map<String, String> variables;

        public Arguments(MutableGlob mutableGlob, Map<String, String> variables) {
            this.mutableGlob = mutableGlob;
            this.variables = variables;
        }

        @Override
        public Arguments visitArgument(GraphqlParser.ArgumentContext ctx) {
            LOGGER.debug("visitArgument " + ctx.getText());
            ExtractArgument extractArgument = new ExtractArgument(mutableGlob, variables);
            extractArgument.visitArgument(ctx);
            return this;
        }
    }

    public static class ExtractArgument extends GraphqlBaseVisitor<ExtractArgument> {
        private final MutableGlob mutableGlob;
        private final Map<String, String> variables;
        private Field field;

        public ExtractArgument(MutableGlob mutableGlob, Map<String, String> variables) {
            this.mutableGlob = mutableGlob;
            this.variables = variables;
        }

        public ExtractArgument visitName(GraphqlParser.NameContext ctx) {
            field = mutableGlob.getType().getField(ctx.getText());
            return super.visitName(ctx);
        }

        @Override
        public ExtractArgument visitValueWithVariable(GraphqlParser.ValueWithVariableContext ctx) {
            ExtractValue value = new ExtractValue(variables);
            value.visitValueWithVariable(ctx);
            final JsonReader ctx2 = new JsonReader(new StringReader(value.value));
            try {
                if (ctx2.peek() != JsonToken.NULL) {
                    field.safeVisit(fieldVisitor, mutableGlob, ctx2);
                } else {
                    mutableGlob.setValue(field, null);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }
    }

    public static class ExtractValue extends GraphqlBaseVisitor<ExtractValue> {
        private final Map<String, String> variables;
        private String value = "";

        public ExtractValue(Map<String, String> variables) {
            this.variables = variables;
        }

        @Override
        public ExtractValue visitVariable(GraphqlParser.VariableContext ctx) {
            ExtractName extractName = new ExtractName();
            extractName.visitVariable(ctx);
            if (!variables.containsKey(extractName.name)) {
                throw new RuntimeException("No value for variable " + extractName.name);
            }
            value = variables.get(extractName.name);
            return this;
        }

        @Override
        public ExtractValue visitTerminal(TerminalNode node) {
            value += node.getText();
            return super.visitTerminal(node);
        }
    }

    public static class ExtractName extends GraphqlBaseVisitor<ExtractName> {
        private String name;

        public ExtractName visitName(GraphqlParser.NameContext ctx) {
            name = ctx.getText();
            return super.visitName(ctx);
        }
    }

    public AntlrGQLVisitor visitSelectionSet(GraphqlParser.SelectionSetContext ctx) {
        ExtractSelectionSet selectionSet = new ExtractSelectionSet();
        selectionSet.visitSelectionSet(ctx);
        return this;
    }

    @Override
    public AntlrGQLVisitor visitFragmentDefinition(GraphqlParser.FragmentDefinitionContext ctx) {
        GraphqlBaseVisitor<?> empty = new GraphqlBaseVisitor();
        empty.visitFragmentDefinition(ctx);
        return this;
    }

    public class ExtractSelectionSet extends GraphqlBaseVisitor<ExtractSelectionSet> {

        public ExtractSelectionSet visitSelection(GraphqlParser.SelectionContext ctx) {
            ExtractSelection selection = new ExtractSelection();
            selection.visitSelection(ctx);
            return this;
        }
    }

    public class ExtractSelection extends GraphqlBaseVisitor<ExtractSelection> {
        private String name;
        private String aliasName;

        @Override
        public ExtractSelection visitSelection(GraphqlParser.SelectionContext ctx) {
            LOGGER.debug("visitSelection " + ctx.getText());
            super.visitSelection(ctx);
            trees.pop().complete();
            return this;
        }

        public ExtractSelection visitArguments(GraphqlParser.ArgumentsContext ctx) {
            LOGGER.debug("visitArguments " + ctx.getText());
            MutableGlob mutableGlob = trees.element().getArguments();
            Arguments arguments = new Arguments(mutableGlob, variables);
            arguments.visitArguments(ctx);
            return this;
        }

        public ExtractSelection visitName(GraphqlParser.NameContext ctx) {
            name = ctx.getText();
            GqlGlobBuilder gqlGlobBuilder = trees.element().addSub(name, aliasName == null ? name : aliasName);
            trees.push(gqlGlobBuilder);
            return super.visitName(ctx);
        }

        public ExtractSelection visitAlias(GraphqlParser.AliasContext ctx) {
            final ExtractName extractName = new ExtractName();
            extractName.visitAlias(ctx);
            aliasName = extractName.name;
            return this;
        }

        @Override
        public ExtractSelection visitSelectionSet(GraphqlParser.SelectionSetContext ctx) {
            ExtractSelectionSet selectionSet = new ExtractSelectionSet();
            final GqlGlobBuilder subBuilder = trees.element().getSubBuilder();
            trees.push(subBuilder);
            selectionSet.visitSelectionSet(ctx);
            trees.pop().complete();
            return this;
        }

        @Override
        public ExtractSelection visitFragmentName(GraphqlParser.FragmentNameContext ctx) {
            LOGGER.debug("visit fragment name " + ctx.getText());
            final ExtractSelection selection = super.visitFragmentName(ctx);
            final GraphqlParser.SelectionSetContext selectionSetContext = fragments.get(ctx.getText());
            super.visitSelectionSet(selectionSetContext);
            trees.push(new GqlGlobBuilderWithError() {
                public void complete() {
                }
            });
            return selection;
        }

        @Override
        public ExtractSelection visitFragmentSpread(GraphqlParser.FragmentSpreadContext ctx) {
            LOGGER.debug("visit fragment Spread " + ctx.getText());
            return super.visitFragmentSpread(ctx);
        }

        @Override
        public ExtractSelection visitInlineFragment(GraphqlParser.InlineFragmentContext ctx) {
            LOGGER.debug("visit fragment Inline " + ctx.getText());
            return super.visitInlineFragment(ctx);
        }

        @Override
        public ExtractSelection visitFragmentDefinition(GraphqlParser.FragmentDefinitionContext ctx) {
            LOGGER.debug("visit Fragment Definition " + ctx.getText());
            return super.visitFragmentDefinition(ctx);
        }
    }

}
