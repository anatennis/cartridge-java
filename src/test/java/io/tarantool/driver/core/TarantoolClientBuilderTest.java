package io.tarantool.driver.core;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolClientBuilder;
import io.tarantool.driver.api.TarantoolClientConfig;
import io.tarantool.driver.api.TarantoolClientFactory;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.TarantoolServerAddress;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.auth.SimpleTarantoolCredentials;
import io.tarantool.driver.auth.TarantoolCredentials;
import io.tarantool.driver.mappers.DefaultMessagePackMapper;
import org.junit.jupiter.api.Test;
import org.msgpack.value.StringValue;
import org.msgpack.value.ValueFactory;

import javax.net.ssl.SSLException;

import static io.tarantool.driver.api.connection.TarantoolConnectionSelectionStrategyType.PARALLEL_ROUND_ROBIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for client builder {@link TarantoolClientBuilder}
 *
 * @author Oleg Kuznetsov
 */
public class TarantoolClientBuilderTest {

    private final TarantoolCredentials SAMPLE_CREDENTIALS =
            new SimpleTarantoolCredentials("root", "passwd");

    private final DefaultMessagePackMapper SAMPLE_MAPPER =
            new DefaultMessagePackMapper.Builder().build();

    private final TarantoolServerAddress SAMPLE_ADDRESS =
            new TarantoolServerAddress("123.123.123.123", 32123);

    private final int SAMPLE_CONNECTIONS = 3;
    private final int SAMPLE_CONNECT_TIMEOUT = 5000;
    private final int SAMPLE_REQUEST_TIMEOUT = 400;
    private final int SAMPLE_READ_TIMEOUT = 4999;
    private final int SAMPLE_EVENT_LOOP_THREADS_NUMBER = 4;

    @Test
    void test_should_createClient() {
        //when
        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client =
                TarantoolClientFactory.createClient()
                        .withAddresses(SAMPLE_ADDRESS)
                        .withCredentials(SAMPLE_CREDENTIALS)
                        .withMessagePackMapper(SAMPLE_MAPPER)
                        .withConnections(SAMPLE_CONNECTIONS)
                        .withConnectTimeout(SAMPLE_CONNECT_TIMEOUT)
                        .withRequestTimeout(SAMPLE_REQUEST_TIMEOUT)
                        .withReadTimeout(SAMPLE_READ_TIMEOUT)
                        .withEventLoopThreadsNumber(SAMPLE_EVENT_LOOP_THREADS_NUMBER)
                        .build();

        //then
        assertClientParams(client);
    }

    @Test
    void test_should_createClient_withSslContext() throws SSLException {
        final SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        //when
        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client =
                TarantoolClientFactory.createClient()
                        .withAddresses(SAMPLE_ADDRESS)
                        .withCredentials(SAMPLE_CREDENTIALS)
                        .withMessagePackMapper(SAMPLE_MAPPER)
                        .withConnections(SAMPLE_CONNECTIONS)
                        .withConnectTimeout(SAMPLE_CONNECT_TIMEOUT)
                        .withRequestTimeout(SAMPLE_REQUEST_TIMEOUT)
                        .withReadTimeout(SAMPLE_READ_TIMEOUT)
                        .withEventLoopThreadsNumber(SAMPLE_EVENT_LOOP_THREADS_NUMBER)
                        .withSslContext(sslContext)
                        .build();

        //then
        assertClientParams(client);
        assertEquals(sslContext, client.getConfig().getSslContext());
    }

    @Test
    void test_should_configureClient() {
        //when
        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client =
                TarantoolClientFactory.createClient()
                        .withAddresses(SAMPLE_ADDRESS)
                        .withCredentials(SAMPLE_CREDENTIALS)
                        .withMessagePackMapper(SAMPLE_MAPPER)
                        .withConnections(SAMPLE_CONNECTIONS)
                        .withConnectTimeout(SAMPLE_CONNECT_TIMEOUT)
                        .withRequestTimeout(SAMPLE_REQUEST_TIMEOUT)
                        .withReadTimeout(SAMPLE_READ_TIMEOUT)
                        .withEventLoopThreadsNumber(SAMPLE_EVENT_LOOP_THREADS_NUMBER)
                        .build();

        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> configuredClient =
                TarantoolClientFactory.configureClient(client).build();

        //then
        assertClientParams(configuredClient);
    }

    @Test
    void test_should_createClient_withConfig() {
        //given
        final TarantoolClientConfig config = TarantoolClientConfig.builder()
                .withCredentials(SAMPLE_CREDENTIALS)
                .withMessagePackMapper(SAMPLE_MAPPER)
                .withConnections(SAMPLE_CONNECTIONS)
                .withConnectTimeout(SAMPLE_CONNECT_TIMEOUT)
                .withRequestTimeout(SAMPLE_REQUEST_TIMEOUT)
                .withReadTimeout(SAMPLE_READ_TIMEOUT)
                .withEventLoopThreadsNumber(SAMPLE_EVENT_LOOP_THREADS_NUMBER)
                .build();

        //when
        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client =
                TarantoolClientFactory.createClient()
                        .withAddresses(SAMPLE_ADDRESS)
                        .withConnections(123123)
                        .withTarantoolClientConfig(config)
                        .build();

        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> configuredClient =
                TarantoolClientFactory.configureClient(client).build();

        //then
        assertClientParams(configuredClient);
    }

    @Test
    void test_should_createClient_ifMessagePackMapperIsChanged() {
        //given
        String expectedMappingResult = "Hello";

        //when
        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client = TarantoolClientFactory.createClient()
                .withAddresses(SAMPLE_ADDRESS)
                .withCredentials(SAMPLE_CREDENTIALS)
                .withDefaultMessagePackMapperConfiguration(mapperBuilder ->
                        mapperBuilder.withObjectConverter(String.class, StringValue.class,
                                object ->
                                        ValueFactory.newString(expectedMappingResult))
                )
                .withConnections(SAMPLE_CONNECTIONS)
                .withConnectTimeout(SAMPLE_CONNECT_TIMEOUT)
                .withRequestTimeout(SAMPLE_REQUEST_TIMEOUT)
                .withReadTimeout(SAMPLE_READ_TIMEOUT)
                .withEventLoopThreadsNumber(SAMPLE_EVENT_LOOP_THREADS_NUMBER)
                .build();

        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> configuredClient =
                TarantoolClientFactory.configureClient(client).build();

        String convertedTest = configuredClient.getConfig().getMessagePackMapper()
                .toValue("Test").asStringValue().asString();

        //then
        assertEquals(expectedMappingResult, convertedTest);
        assertEquals(ClusterTarantoolTupleClient.class, configuredClient.getClass());
        TarantoolClientConfig config = configuredClient.getConfig();

        assertTrue(((ClusterTarantoolTupleClient) client).getAddressProvider()
                .getAddresses().contains(SAMPLE_ADDRESS));

        assertNotEquals(SAMPLE_MAPPER, config.getMessagePackMapper());

        assertEquals(SAMPLE_CREDENTIALS, config.getCredentials());
        assertEquals(SAMPLE_CONNECTIONS, config.getConnections());
        assertEquals(SAMPLE_READ_TIMEOUT, config.getReadTimeout());
        assertEquals(SAMPLE_REQUEST_TIMEOUT, config.getRequestTimeout());
        assertEquals(SAMPLE_CONNECT_TIMEOUT, config.getConnectTimeout());
        assertEquals(PARALLEL_ROUND_ROBIN.value(), config.getConnectionSelectionStrategyFactory());
    }

    @Test
    void test_should_createClient_with_default_threads() {
        //when
        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client =
                TarantoolClientFactory.createClient()
                        .build();

        //then
        assertEquals(client.getConfig().getEventLoopThreadsNumber(), 0);
    }

    @Test
    void test_should_createClient_with_custom_threads() {
        //when
        int customThreadsNumber = 10;
        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client =
                TarantoolClientFactory.createClient()
                        .withEventLoopThreadsNumber(SAMPLE_EVENT_LOOP_THREADS_NUMBER)
                        .build();

        //then
        assertEquals(client.getConfig().getEventLoopThreadsNumber(), 4);
    }

    @Test
    void test_should_createClient_with_illegal_threads() {
        assertThrows(IllegalArgumentException.class, () -> {
            TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client =
                    TarantoolClientFactory.createClient()
                            .withEventLoopThreadsNumber(-1)
                            .build();
        });
    }

    private void assertClientParams(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client) {
        assertEquals(ClusterTarantoolTupleClient.class, client.getClass());
        TarantoolClientConfig config = client.getConfig();

        assertTrue(((ClusterTarantoolTupleClient) client).getAddressProvider()
                .getAddresses().contains(SAMPLE_ADDRESS));

        assertEquals(SAMPLE_CREDENTIALS, config.getCredentials());
        assertEquals(SAMPLE_MAPPER, config.getMessagePackMapper());
        assertEquals(SAMPLE_CONNECTIONS, config.getConnections());
        assertEquals(SAMPLE_READ_TIMEOUT, config.getReadTimeout());
        assertEquals(SAMPLE_REQUEST_TIMEOUT, config.getRequestTimeout());
        assertEquals(SAMPLE_CONNECT_TIMEOUT, config.getConnectTimeout());
        assertEquals(PARALLEL_ROUND_ROBIN.value(), config.getConnectionSelectionStrategyFactory());
        assertEquals(SAMPLE_EVENT_LOOP_THREADS_NUMBER, config.getEventLoopThreadsNumber());
    }

    @Test
    void test_should_throwIllegalArgumentExceptionForConfiguring_ifPassedNullAsClient() {
        assertThrows(IllegalArgumentException.class, () -> TarantoolClientFactory.configureClient(null).build());
    }
}
