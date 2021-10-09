package io.agrest.meta;

import io.agrest.property.PropertyReader;

import java.util.Objects;

/**
 * @since 4.7
 */
public class DefaultAgAttributeOverlay implements AgAttributeOverlay {

    private final String name;
    private final Class<?> sourceType;
    private final Class<?> javaType;
    private final PropertyReader propertyReader;

    public DefaultAgAttributeOverlay(String name, Class<?> sourceType, Class<?> javaType, PropertyReader propertyReader) {
        this.name = Objects.requireNonNull(name);
        this.sourceType = Objects.requireNonNull(sourceType);

        // optional attributes. NULL means not overlaid
        this.javaType = javaType;
        this.propertyReader = propertyReader;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AgAttribute resolve(AgAttribute maybeOverlaid) {
        return maybeOverlaid != null ? resolveOverlaid(maybeOverlaid) : resolveNew();
    }

    private AgAttribute resolveOverlaid(AgAttribute overlaid) {
        Class<?> javaType = this.javaType != null ? this.javaType : overlaid.getType();
        PropertyReader propertyReader = this.propertyReader != null ? this.propertyReader : overlaid.getPropertyReader();

        return new DefaultAgAttribute(name, javaType, propertyReader);
    }

    private AgAttribute resolveNew() {

        // we can't use properties from the overlaid attribute, so make sure we have all the required ones present
        checkPropertyDefined("javaType", javaType);
        checkPropertyDefined("propertyReader", propertyReader);

        return new DefaultAgAttribute(name, javaType, propertyReader);
    }

    private void checkPropertyDefined(String property, Object value) {
        if (value == null) {
            String message = String.format(
                    "Overlay can't be resolved: '%s' is not defined and no overlaid attribute '%s.%s' exists",
                    property,
                    this.sourceType.getName(),
                    this.name);

            throw new IllegalStateException(message);
        }
    }
}
