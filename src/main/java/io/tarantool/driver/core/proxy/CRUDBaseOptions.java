package io.tarantool.driver.core.proxy;

import java.util.Optional;

/**
 * This class is not part of the public API.
 *
 * Represent basic options for all cluster operations
 *
 * @author Alexey Kuzin
 * @author Artyom Dubinin
 */
class CRUDBaseOptions extends CRUDAbstractOperationOptions {

    public static final String TIMEOUT = "timeout";

    protected
    <O extends CRUDBaseOptions, T extends AbstractBuilder<O, T>>
    CRUDBaseOptions(AbstractBuilder<O, T> builder) {
        addOption(TIMEOUT, builder.timeout);
    }

    /**
     * Inheritable Builder for basic cluster proxy operation options.
     *
     * @see CRUDAbstractOperationOptions.AbstractBuilder
     */
    protected abstract static
    class AbstractBuilder<O extends CRUDBaseOptions, T extends AbstractBuilder<O, T>>
        extends CRUDAbstractOperationOptions.AbstractBuilder<O, T> {
        protected Optional<Integer> timeout = Optional.empty();

        public T withTimeout(Optional<Integer> timeout) {
            this.timeout = timeout;
            return self();
        }
    }

    /**
     * Concrete Builder implementation for basic cluster proxy operation options.
     */
    protected static final class Builder
        extends AbstractBuilder<CRUDBaseOptions, Builder> {

        @Override
        Builder self() {
            return this;
        }

        @Override
        public CRUDBaseOptions build() {
            return new CRUDBaseOptions(this);
        }
    }
}
