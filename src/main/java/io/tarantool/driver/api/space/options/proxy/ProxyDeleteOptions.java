package io.tarantool.driver.api.space.options.proxy;

import io.tarantool.driver.api.space.options.DeleteOptions;

/**
 * Represent options for delete cluster proxy operation
 *
 * @author Alexey Kuzin
 * @author Artyom Dubinin
 */
public final class ProxyDeleteOptions extends ProxyBucketIdOptions<ProxyDeleteOptions> implements DeleteOptions {

    private ProxyDeleteOptions() {
    }

    /**
     * Create new instance.
     *
     * @return new options instance
     */
    public static ProxyDeleteOptions create() {
        return new ProxyDeleteOptions();
    }

    @Override
    protected ProxyDeleteOptions self() {
        return this;
    }
}
