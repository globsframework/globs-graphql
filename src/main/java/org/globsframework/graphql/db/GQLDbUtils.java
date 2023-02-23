package org.globsframework.graphql.db;

import org.globsframework.functional.FunctionalKey;
import org.globsframework.graphql.OnKey;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.utils.collections.MultiMap;

import java.util.List;

public class GQLDbUtils {
    public interface Cached {
        Cached NULL = new Cached() {
            public Glob get(FunctionalKey functionalKey) {
                return null;
            }

            public void push(FunctionalKey functionalKey, Glob glob) {

            }
        };
        Glob get(FunctionalKey functionalKey);

        void push(FunctionalKey functionalKey, Glob glob);
    }

    public static void queryByKey(SqlConnection db, StringField fKeyField, StringField dbKeyField, List<OnKey> parents,
                                  Constraint additionalConstraint) {
        queryByKey(db, fKeyField, dbKeyField, parents, additionalConstraint, Cached.NULL);
    }

    public static void queryByKey(SqlConnection db, StringField fKeyField, StringField dbKeyField,
                                  List<OnKey> parents, Constraint additionalConstraint, Cached cached) {
        MultiMap<String, OnKey> keyOnLoadMap = new MultiMap<>();
        for (OnKey parent : parents) {
            final Glob glob = cached.get(parent.key());
            if (glob != null) {
                parent.onNew().push(glob);
            }
            else {
                keyOnLoadMap.put(parent.key().get(fKeyField), parent);
            }
        }
        if (keyOnLoadMap.isEmpty()) {
            return;
        }
        final Constraint in = Constraints.in(dbKeyField, keyOnLoadMap.keySet());
        final SelectQuery query = db.getQueryBuilder(dbKeyField.getGlobType(), Constraints.and(additionalConstraint, in))
                .selectAll()
                .getQuery();
        query.executeAsGlobStream().forEach(glob -> {
            final String s = glob.get(dbKeyField);
            final List<OnKey> onKeys = keyOnLoadMap.get(s);
            for (OnKey onKey : onKeys) {
                cached.push(onKey.key(), glob);
                onKey.onNew().push(glob);
            }
        });
    }
}
