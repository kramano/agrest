package io.agrest.cayenne.processor.update;

import io.agrest.AgException;
import io.agrest.CompoundObjectId;
import io.agrest.EntityParent;
import io.agrest.EntityUpdate;
import io.agrest.cayenne.processor.CayenneUtil;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.*;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.map.*;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.reflect.ClassDescriptor;

import java.util.*;

/**
 * A superclass of the processors invoked for {@link io.agrest.UpdateStage#MERGE_CHANGES} stage.
 *
 * @since 2.7
 */
public abstract class CayenneMergeChangesStage implements Processor<UpdateContext<?>> {

    private final AgDataMap dataMap;
    protected EntityResolver entityResolver;

    public CayenneMergeChangesStage(AgDataMap dataMap, EntityResolver entityResolver) {
        this.dataMap = dataMap;
        this.entityResolver = entityResolver;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        merge((UpdateContext<DataObject>) context);
        return ProcessorOutcome.CONTINUE;
    }

    protected abstract <T extends DataObject> void merge(UpdateContext<T> context);

    protected <T extends DataObject> void updateSingle(ObjectRelator relator, T o, Collection<EntityUpdate<T>> updates) {

        for (EntityUpdate<T> u : updates) {
            mergeChanges(u, o, relator);
        }

        relator.relateToParent(o);
    }

    protected <T extends DataObject> void createSingle(UpdateContext<T> context, ObjectRelator relator, EntityUpdate<T> u) {

        ObjectContext objectContext = CayenneUpdateStartStage.cayenneContext(context);
        DataObject o = objectContext.newObject(context.getType());
        Map<String, Object> idByAgAttribute = u.getId();

        // set explicit ID
        if (idByAgAttribute != null) {

            if (context.isIdUpdatesDisallowed() && u.isExplicitId()) {
                throw AgException.badRequest("Setting ID explicitly is not allowed: %s", idByAgAttribute);
            }

            ObjEntity objEntity = objectContext.getEntityResolver().getObjEntity(context.getType());
            DbEntity dbEntity = objEntity.getDbEntity();
            AgEntity agEntity = context.getEntity().getAgEntity();

            Map<DbAttribute, Object> idByDbAttribute = mapToDbAttributes(agEntity, idByAgAttribute);

            if (isPrimaryKey(dbEntity, idByDbAttribute.keySet())) {
                createSingleFromPk(objEntity, idByDbAttribute, o);
            } else {
                // need to make an additional check that the AgId is unique
                checkExisting(objectContext, agEntity, idByDbAttribute, idByAgAttribute);
                createSingleFromIdValues(objEntity, idByDbAttribute, idByAgAttribute, o);
            }
        }

        mergeChanges(u, o, relator);
        relator.relateToParent(o);
    }

    // translate "id" expressed in terms on public Ag names to Cayenne DbAttributes
    private Map<DbAttribute, Object> mapToDbAttributes(AgEntity<?> agEntity, Map<String, Object> idByAgAttribute) {

        Map<DbAttribute, Object> idByDbAttribute = new HashMap<>((int) (idByAgAttribute.size() / 0.75) + 1);
        for (Map.Entry<String, Object> e : idByAgAttribute.entrySet()) {

            // I guess this kind of type checking is not too dirty ... CayenneAgDbAttribute was created by Cayenne
            // part of Ag, and we are back again in Cayenne part of Ag, trying to map Ag model back to Cayenne
            DbAttribute dbAttribute = dbAttributeForAgAttribute(agEntity, e.getKey());

            if (dbAttribute == null) {
                throw AgException.badRequest("Not a mapped persistent attribute '%s.%s'", agEntity.getName(), e.getKey());
            }

            idByDbAttribute.put(dbAttribute, e.getValue());
        }

        return idByDbAttribute;
    }

    private void createSingleFromPk(ObjEntity objEntity, Map<DbAttribute, Object> idByDbAttribute, DataObject o) {
        for (Map.Entry<DbAttribute, Object> e : idByDbAttribute.entrySet()) {
            setPrimaryKey(o, objEntity, e.getKey(), e.getValue());
        }
    }

    private <T extends DataObject> void checkExisting(
            ObjectContext objectContext,
            AgEntity<T> agEntity,
            Map<DbAttribute, Object> idByDbAttribute,
            Map<String, Object> idByAgAttribute) {

        ObjectSelect<DataRow> query = ObjectSelect.dataRowQuery(agEntity.getType());
        for (Map.Entry<DbAttribute, Object> e : idByDbAttribute.entrySet()) {
            query.and(ExpressionFactory.matchDbExp(e.getKey().getName(), e.getValue()));
        }

        if (query.selectOne(objectContext) != null) {
            throw AgException.badRequest("Can't create '%s' with id %s - already exists",
                    agEntity.getName(),
                    CompoundObjectId.mapToString(idByAgAttribute));
        }
    }

    private void createSingleFromIdValues(
            ObjEntity entity,
            Map<DbAttribute, Object> idByDbAttribute,
            Map<String, Object> idByAgAttribute,
            DataObject o) {

        for (Map.Entry<DbAttribute, Object> idPart : idByDbAttribute.entrySet()) {

            DbAttribute maybePk = idPart.getKey();
            if (maybePk == null) {
                throw AgException.badRequest("Can't create '%s' with id %s - not an ID DB attribute: %s",
                        entity.getName(),
                        CompoundObjectId.mapToString(idByAgAttribute),
                        idPart.getKey());
            }

            if (maybePk.isPrimaryKey()) {
                setPrimaryKey(o, entity, maybePk, idPart.getValue());
            } else {

                ObjAttribute objAttribute = entity.getAttributeForDbAttribute(maybePk);
                if (objAttribute == null) {
                    throw AgException.badRequest("Can't create '%s' with id %s - unknown object attribute: %s",
                            entity.getName(),
                            CompoundObjectId.mapToString(idByAgAttribute),
                            idPart.getKey());
                }

                o.writeProperty(objAttribute.getName(), idPart.getValue());
            }
        }
    }

    private void setPrimaryKey(DataObject o, ObjEntity entity, DbAttribute pk, Object idValue) {

        // 1. meaningful ID
        // TODO: must precompile all this... figuring this on the fly is slow
        ObjAttribute opk = entity.getAttributeForDbAttribute(pk);
        if (opk != null) {
            o.writeProperty(opk.getName(), idValue);
        }
        // 2. PK is auto-generated ... I guess this is sorta expected to fail - generated meaningless PK should not be
        // pushed from the client
        else if (pk.isGenerated()) {
            throw AgException.badRequest("Can't create '%s' with fixed id", entity.getName());
        }
        // 3. probably a propagated ID.
        else {
            o.getObjectId().getReplacementIdMap().put(pk.getName(), idValue);
        }
    }

    /**
     * @return true if all PK columns are represented in {@code keys}
     */
    private boolean isPrimaryKey(DbEntity entity, Collection<DbAttribute> maybePk) {
        int pkSize = entity.getPrimaryKeys().size();
        if (pkSize > maybePk.size()) {
            return false;
        }

        int countPk = 0;
        for (DbAttribute a : maybePk) {
            if (a.isPrimaryKey()) {
                countPk++;
            }
        }

        return countPk >= pkSize;
    }

    private <T extends DataObject> void mergeChanges(EntityUpdate<T> entityUpdate, DataObject o, ObjectRelator relator) {

        // attributes
        for (Map.Entry<String, Object> e : entityUpdate.getValues().entrySet()) {
            o.writeProperty(e.getKey(), e.getValue());
        }

        // relationships
        ObjectContext context = o.getObjectContext();

        ObjEntity entity = context.getEntityResolver().getObjEntity(o);

        for (Map.Entry<String, Set<Object>> e : entityUpdate.getRelatedIds().entrySet()) {

            ObjRelationship relationship = entity.getRelationship(e.getKey());
            AgRelationship agRelationship = entityUpdate.getEntity().getRelationship(e.getKey());

            // sanity check
            if (agRelationship == null) {
                continue;
            }

            final Set<Object> relatedIds = e.getValue();
            if (relatedIds == null || relatedIds.isEmpty() || allElementsNull(relatedIds)) {

                relator.unrelateAll(agRelationship, o);
                continue;
            }

            if (!agRelationship.isToMany() && relatedIds.size() > 1) {
                throw AgException.badRequest(
                        "Relationship is to-one, but received update with multiple objects: %s",
                        agRelationship.getName());
            }

            ClassDescriptor relatedDescriptor = context.getEntityResolver().getClassDescriptor(
                    relationship.getTargetEntityName());

            relator.unrelateAll(agRelationship, o, new RelationshipUpdate() {
                @Override
                public boolean containsRelatedObject(DataObject relatedObject) {
                    return relatedIds.contains(Cayenne.pkForObject(relatedObject));
                }

                @Override
                public void removeUpdateForRelatedObject(DataObject relatedObject) {
                    relatedIds.remove(Cayenne.pkForObject(relatedObject));
                }
            });

            for (Object relatedId : relatedIds) {

                if (relatedId == null) {
                    continue;
                }

                DataObject related = (DataObject) Cayenne.objectForPK(context, relatedDescriptor.getObjectClass(),
                        relatedId);

                if (related == null) {
                    throw AgException.notFound("Related object '%s' with ID '%s' is not found",
                            relationship.getTargetEntityName(),
                            e.getValue());
                }

                relator.relate(agRelationship, o, related);
            }
        }

        entityUpdate.setMergedTo(o);
        entityUpdate.setCreatedNew(o.getPersistenceState() == PersistenceState.NEW);
    }

    private boolean allElementsNull(Collection<?> elements) {

        for (Object element : elements) {
            if (element != null) {
                return false;
            }
        }

        return true;
    }

    protected <T extends DataObject> ObjectRelator createRelator(UpdateContext<T> context) {

        final EntityParent<?> parent = context.getParent();

        if (parent == null) {
            return new ObjectRelator();
        }

        ObjectContext objectContext = CayenneUpdateStartStage.cayenneContext(context);

        ObjEntity parentEntity = objectContext.getEntityResolver().getObjEntity(parent.getType());
        AgEntity<?> parentAgEntity = dataMap.getEntity(context.getParent().getType());
        final DataObject parentObject = (DataObject) CayenneUtil.findById(objectContext, parent.getType(),
                parentAgEntity, parent.getId().get());

        if (parentObject == null) {
            throw AgException.notFound("No parent object for ID '%s' and entity '%s'", parent.getId(), parentEntity.getName());
        }

        // TODO: check that relationship target is the same as <T> ??
        if (parentEntity.getRelationship(parent.getRelationship()).isToMany()) {
            return new ObjectRelator() {
                @Override
                public void relateToParent(DataObject object) {
                    parentObject.addToManyTarget(parent.getRelationship(), object, true);
                }
            };
        } else {
            return new ObjectRelator() {
                @Override
                public void relateToParent(DataObject object) {
                    parentObject.setToOneTarget(parent.getRelationship(), object, true);
                }
            };
        }
    }

    // TODO: copied verbatim from CayenneQueryAssembler... Unify this code?
    protected DbAttribute dbAttributeForAgAttribute(AgEntity<?> agEntity, String attributeName) {

        ObjEntity entity = entityResolver.getObjEntity(agEntity.getName());
        ObjAttribute objAttribute = entity.getAttribute(attributeName);
        return objAttribute != null
                ? objAttribute.getDbAttribute()
                // this is suspect.. don't see how we would allow DbAttribute names to leak in the Ag model
                : entity.getDbEntity().getAttribute(attributeName);
    }

    interface RelationshipUpdate {
        boolean containsRelatedObject(DataObject o);

        void removeUpdateForRelatedObject(DataObject o);
    }

    static class ObjectRelator {

        void relateToParent(DataObject object) {
            // do nothing
        }

        void relate(AgRelationship agRelationship, DataObject object, DataObject relatedObject) {
            if (agRelationship.isToMany()) {
                object.addToManyTarget(agRelationship.getName(), relatedObject, true);
            } else {
                object.setToOneTarget(agRelationship.getName(), relatedObject, true);
            }
        }

        void unrelateAll(AgRelationship agRelationship, DataObject object) {
            unrelateAll(agRelationship, object, null);
        }

        void unrelateAll(AgRelationship agRelationship, DataObject object, RelationshipUpdate relationshipUpdate) {

            if (agRelationship.isToMany()) {

                @SuppressWarnings("unchecked")
                List<? extends DataObject> relatedObjects =
                        (List<? extends DataObject>) object.readProperty(agRelationship.getName());

                for (int i = 0; i < relatedObjects.size(); i++) {
                    DataObject relatedObject = relatedObjects.get(i);
                    if (relationshipUpdate == null || !relationshipUpdate.containsRelatedObject(relatedObject)) {
                        object.removeToManyTarget(agRelationship.getName(), relatedObject, true);
                        i--;
                    } else {
                        relationshipUpdate.removeUpdateForRelatedObject(relatedObject);
                    }
                }

            } else {
                object.setToOneTarget(agRelationship.getName(), null, true);
            }
        }
    }
}
