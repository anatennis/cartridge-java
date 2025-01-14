package io.tarantool.driver.core;

import io.tarantool.driver.api.TarantoolClientConfig;
import io.tarantool.driver.api.connection.ConnectionSelectionStrategy;
import io.tarantool.driver.api.connection.TarantoolConnection;
import io.tarantool.driver.api.connection.TarantoolConnectionSelectionStrategies.ParallelRoundRobinStrategyFactory;
import io.tarantool.driver.exceptions.NoAvailableConnectionsException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParallelRoundRobinStrategyTest {

    @Test
    public void testGetAddress() {
        List<TarantoolConnection> connections = Arrays.asList(
                new CustomConnection("127.0.0.1", 3001),
                new CustomConnection("127.0.0.2", 3002),
                new CustomConnection("127.0.0.3", 3003),
                new CustomConnection("127.0.0.4", 3004),
                new CustomConnection("127.0.0.5", 3005),
                new CustomConnection("127.0.0.6", 3006)
        );

        TarantoolClientConfig config = TarantoolClientConfig.builder()
                .withConnections(2)
                .build();
        ConnectionSelectionStrategy strategy = ParallelRoundRobinStrategyFactory.INSTANCE.create(config, connections);

        assertEquals("127.0.0.1", ((CustomConnection) strategy.next()).getHost());
        assertEquals("127.0.0.3", ((CustomConnection) strategy.next()).getHost());
        assertEquals("127.0.0.5", ((CustomConnection) strategy.next()).getHost());
        assertEquals("127.0.0.2", ((CustomConnection) strategy.next()).getHost());
        assertEquals("127.0.0.4", ((CustomConnection) strategy.next()).getHost());
        assertEquals("127.0.0.6", ((CustomConnection) strategy.next()).getHost());
        assertEquals("127.0.0.1", ((CustomConnection) strategy.next()).getHost());
    }

    @Test
    public void testBoundaryCases() {
        List<TarantoolConnection> connections = new ArrayList<>();
        TarantoolClientConfig config = new TarantoolClientConfig();

        assertThrows(IllegalArgumentException.class,
                () -> ParallelRoundRobinStrategyFactory.INSTANCE.create(config, null));
        assertThrows(NoAvailableConnectionsException.class,
                () -> ParallelRoundRobinStrategyFactory.INSTANCE.create(config, connections).next());

        connections.add(new CustomConnection("127.0.0.1", 3001));
        ConnectionSelectionStrategy strategy = ParallelRoundRobinStrategyFactory.INSTANCE.create(config, connections);
        assertEquals("127.0.0.1", ((CustomConnection) strategy.next()).getHost());
        assertEquals("127.0.0.1", ((CustomConnection) strategy.next()).getHost());
        assertEquals("127.0.0.1", ((CustomConnection) strategy.next()).getHost());

        connections.clear();
        assertEquals("127.0.0.1", ((CustomConnection) strategy.next()).getHost());
    }

    @Test
    public void testParallelGetAddress() {
        List<TarantoolConnection> connections = IntStream.range(1, 11)
                .mapToObj(i -> new CustomConnection(String.format("127.0.0.%d", i), 3000 + i))
                .collect(Collectors.toList());

        TarantoolClientConfig config = new TarantoolClientConfig();
        ConnectionSelectionStrategy strategy = ParallelRoundRobinStrategyFactory.INSTANCE.create(config, connections);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<?>> futures = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (int j = 0; j < 10; j++) {
                    ((CustomConnection) strategy.next()).count();
                }
            }, executor));
        }
        futures.forEach(CompletableFuture::join);

        for (TarantoolConnection c : connections) {
            assertEquals(10, ((CustomConnection) c).getCount(), "Each connection must have been used 10 times");
        }
    }

    @Test
    public void testNoAvailableConnectionException() {
        List<TarantoolConnection> connections = Collections.emptyList();

        TarantoolClientConfig config = new TarantoolClientConfig();
        ConnectionSelectionStrategy strategy = ParallelRoundRobinStrategyFactory.INSTANCE.create(config, connections);

        assertThrows(NoAvailableConnectionsException.class, strategy::next, "Exception must be thrown");
    }

    @Test
    public void testSkipConnections() {
        List<TarantoolConnection> connections = IntStream.range(1, 11)
                .mapToObj(i -> new CustomConnection(String.format("127.0.0.%d", i), 3000 + i))
                .peek(c -> {
                    if (c.getPort() < 3006) {
                        c.setConnected(false);
                    }
                })
                .collect(Collectors.toList());

        TarantoolClientConfig config = new TarantoolClientConfig();
        ConnectionSelectionStrategy strategy = ParallelRoundRobinStrategyFactory.INSTANCE.create(config, connections);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<?>> futures = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (int j = 0; j < 10; j++) {
                    assertDoesNotThrow(() -> ((CustomConnection) strategy.next()).count());
                }
            }, executor));
        }
        futures.forEach(CompletableFuture::join);

        for (TarantoolConnection c : connections) {
            if (((CustomConnection) c).getPort() < 3006) {
                assertEquals(0, ((CustomConnection) c).getCount(), "Closed connection must have been used 0 times");
            } else {
                assertEquals(20, ((CustomConnection) c).getCount(), "Active connection must have been used 20 times");
            }
        }
    }
}
