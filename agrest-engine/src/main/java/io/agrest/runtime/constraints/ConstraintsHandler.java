package io.agrest.runtime.constraints;

import io.agrest.EntityConstraint;
import io.agrest.ResourceEntity;
import io.agrest.SizeConstraints;
import io.agrest.constraints.Constraint;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * An {@link IConstraintsHandler} that ensures that no target attributes exceed
 * the defined bounds.
 *
 * @since 1.5
 */
public class ConstraintsHandler implements IConstraintsHandler {

    public static final String DEFAULT_READ_CONSTRAINTS_LIST = "agrest.constraints.read.list";
    public static final String DEFAULT_WRITE_CONSTRAINTS_LIST = "agrest.constraints.write.list";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintsHandler.class);

    private RequestConstraintsHandler requestConstraintHandler;
    private EntityConstraintHandler modelConstraintHandler;

    public ConstraintsHandler(
            @Inject(DEFAULT_READ_CONSTRAINTS_LIST) List<EntityConstraint> readConstraints,
            @Inject(DEFAULT_WRITE_CONSTRAINTS_LIST) List<EntityConstraint> writeConstraints
    ) {

        this.requestConstraintHandler = new RequestConstraintsHandler();
        this.modelConstraintHandler = new EntityConstraintHandler(readConstraints, writeConstraints);
    }

    @Override
    public <T> void constrainUpdate(UpdateContext<T> context, Constraint<T> c) {

        if (!requestConstraintHandler.constrainUpdate(context, c)) {
            modelConstraintHandler.constrainUpdate(context);
        }
    }

    @Override
    public <T> void constrainResponse(ResourceEntity<T> entity, SizeConstraints sizeConstraints, Constraint<T> c) {

        if (sizeConstraints != null) {
            applySizeConstraintsForRead(entity, sizeConstraints);
        }

        if (!requestConstraintHandler.constrainResponse(entity, c)) {
            modelConstraintHandler.constrainResponse(entity);
        }
    }

    protected void applySizeConstraintsForRead(ResourceEntity<?> entity, SizeConstraints constraints) {

        // fetchOffset - do not exceed source offset
        int upperOffset = constraints.getFetchOffset();
        if (upperOffset > 0 && (entity.getFetchOffset() < 0 || entity.getFetchOffset() > upperOffset)) {
            LOGGER.info("Reducing fetch offset from " + entity.getFetchOffset() + " to max allowed value of "
                    + upperOffset);
            entity.setFetchOffset(upperOffset);
        }

        // fetchLimit - do not exceed source limit
        int upperLimit = constraints.getFetchLimit();
        if (upperLimit > 0 && (entity.getFetchLimit() <= 0 || entity.getFetchLimit() > upperLimit)) {
            LOGGER.info(
                    "Reducing fetch limit from " + entity.getFetchLimit() + " to max allowed value of " + upperLimit);
            entity.setFetchLimit(upperLimit);
        }
    }

}
