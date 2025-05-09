package org.globsframework.graphql.db;

import org.globsframework.core.metamodel.fields.Field;
import org.globsframework.core.model.Glob;
import org.globsframework.sql.SqlConnection;

import java.util.Optional;
import java.util.function.Consumer;

public interface DbGraphqlQuery {
    int getTotal(SqlConnection db);

    CursorPosition gqlQuery(SqlConnection db, Consumer<Glob> consumer);

    enum Order {
        asc, desc
    }

    record CursorPosition(boolean hasPrevious, boolean hasNext) {
    }

    record Last(String id, Optional<String> value) {
    }

    record OrderBy(Field field, Order order) {
        public OrderBy invert() {
            return new OrderBy(field, order == Order.desc ? Order.asc : Order.desc);
        }
    }
}
