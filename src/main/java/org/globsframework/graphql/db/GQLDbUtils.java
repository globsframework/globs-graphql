package org.globsframework.graphql.db;

import org.globsframework.graphql.OnKey;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.utils.collections.MultiMap;

import java.util.List;

public class GQLDbUtils {
    public static void queryByKey(SqlConnection db, StringField fKeyField, StringField dbKeyField, List<OnKey> parents, Constraint additionalConstraint) {
        MultiMap<String, OnKey> keyOnLoadMap = new MultiMap<>();
        for (OnKey parent : parents) {
            keyOnLoadMap.put(parent.key().get(fKeyField), parent);
        }
        final Constraint in = Constraints.in(dbKeyField, keyOnLoadMap.keySet());
        final SelectQuery query = db.getQueryBuilder(dbKeyField.getGlobType(), Constraints.and(additionalConstraint, in))
                .selectAll()
                .getQuery();
        query.executeAsGlobStream().forEach(glob -> {
            final String s = glob.get(dbKeyField);
            final List<OnKey> onKeys = keyOnLoadMap.get(s);
            for (OnKey onKey : onKeys) {
                onKey.onNew().push(glob);
            }
        });
    }
}
