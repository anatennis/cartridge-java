package io.tarantool.driver.core.proxy;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is not part of the public API.
 *
 * An abstract class necessary for implementing CRT (curiously recurring template)
 * pattern for the cluster proxy operation options builders.
 *
 * @author Alexey Kuzin
 */
abstract class CRUDAbstractOperationOptions {

    private final Map<String, Object> resultMap = new HashMap<>();

    /**
     * Inheritable Builder for select cluster proxy operation options.
     * <p>
     * This abstract class is necessary for implementing fluent builder inheritance.
     * The solution with {@code self()} method allows to avoid weird java
     * compiler errors when you cannot call the inherited methods from derived
     * concrete {@code Builder} classes because their type erasure doesn't
     * correspond to the {@code AbstractBuilder} type.
     * <p>
     * The {@code self()} method must be implemented only in the derived concrete
     * classes. These concrete classes are used to work with the operation
     * options in the calling code. They doesn't require to specify the generic
     * types and therefore are more convenient. Also they ensure that the right
     * combination of generic types will be used to avoid potential inheritance
     * flaws.
     */
    protected abstract static
    class AbstractBuilder<O extends CRUDAbstractOperationOptions, B extends AbstractBuilder<O, B>> {
        abstract B self();

        public abstract O build();
    }

    protected void addOption(String option, Object value) {
        resultMap.put(option, value);
    }

    /**
     * Return serializable options representation.
     *
     * @return a map
     */
    public Map<String, Object> asMap() {
        return resultMap;
    }
}