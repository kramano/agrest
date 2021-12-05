package io.agrest.runtime.processor.update;

import io.agrest.meta.AgEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.entity.IChangeAuthorizer;
import org.apache.cayenne.di.Inject;

/**
 * A processor associated with {@link io.agrest.UpdateStage#AUTHORIZE_CHANGES} that runs change authorization filters
 * against request data change operations. It would fail the chain if at least one rule is not satisfied.
 *
 * @since 4.8
 */
public class AuthorizeChangesStage implements Processor<UpdateContext<?>> {

    private final IChangeAuthorizer changeAuthorizer;

    public AuthorizeChangesStage(@Inject IChangeAuthorizer changeAuthorizer) {
        this.changeAuthorizer = changeAuthorizer;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {

        AgEntity<T> entity = context.getEntity().getAgEntity();

        changeAuthorizer.checkCreate(
                context.getChangeOperations().get(ChangeOperationType.CREATE),
                entity.getCreateAuthorizer());

        changeAuthorizer.checkUpdate(
                context.getChangeOperations().get(ChangeOperationType.UPDATE),
                entity.getUpdateAuthorizer());

        changeAuthorizer.checkDelete(
                context.getChangeOperations().get(ChangeOperationType.DELETE),
                entity.getDeleteAuthorizer());
    }
}
