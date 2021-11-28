package io.agrest.cayenne.processor.update;

import io.agrest.EntityUpdate;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.meta.AgDataMap;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.7
 */
public class CayenneCreateStage extends CayenneMergeChangesStage {

    public CayenneCreateStage(
            @Inject AgDataMap dataMap,
            @Inject ICayennePersister persister) {
        super(dataMap, persister.entityResolver());
    }

    @Override
    protected <T extends DataObject> void merge(UpdateContext<T> context) {
        ObjectRelator relator = createRelator(context);

        for (EntityUpdate<T> u : context.getUpdates()) {
            createSingle(context, relator, u);
        }
    }
}
