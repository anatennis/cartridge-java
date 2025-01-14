package io.tarantool.driver.api.space.options.proxy;

import io.tarantool.driver.api.space.options.InsertOptions;

/**
 * Represent options for insert cluster proxy operation
 *
 * @author Alexey Kuzin
 * @author Artyom Dubinin
 */
public final class ProxyInsertOptions extends ProxyBucketIdOptions<ProxyInsertOptions> implements InsertOptions {

    private ProxyInsertOptions() {
    }

    /**
     * Create new instance.
     *
     * @return new options instance
     */
    public static ProxyInsertOptions create() {
        return new ProxyInsertOptions();
    }

    @Override
    protected ProxyInsertOptions self() {
        return this;
    }
}
