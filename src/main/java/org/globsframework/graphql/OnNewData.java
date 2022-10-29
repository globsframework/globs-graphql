package org.globsframework.graphql;

import org.globsframework.json.GSonUtils;
import org.globsframework.model.Glob;
import org.slf4j.Logger;

public interface OnNewData {
    void push(Glob data);

    default OnNewData log(Logger logger) {
        OnNewData parent = this;
        return data -> {
            logger.info("Receive " + GSonUtils.encode(data, true));
            parent.push(data);
        };
    };
}
