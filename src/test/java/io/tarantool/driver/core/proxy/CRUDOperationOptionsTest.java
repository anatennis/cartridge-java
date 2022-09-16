package io.tarantool.driver.core.proxy;

import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.core.tuple.TarantoolTupleImpl;
import io.tarantool.driver.mappers.DefaultMessagePackMapperFactory;
import io.tarantool.driver.mappers.MessagePackMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sergey Volgin
 */
public class CRUDOperationOptionsTest {

    @Test
    public void selectOperationOptions_createEmptyTest() {
        CRUDSelectOptions options = new CRUDSelectOptions.Builder().build();
        assertEquals(Collections.EMPTY_MAP, options.asMap());
    }

    @Test
    public void selectOperationOptions_createNotEmptyTest() {
        MessagePackMapper defaultMapper = DefaultMessagePackMapperFactory.getInstance().defaultComplexTypesMapper();
        List<Object> values = Arrays.asList(4, "a4", "Nineteen Eighty-Four", "George Orwell", 1984);
        TarantoolTuple tuple = new TarantoolTupleImpl(values, defaultMapper);

        CRUDSelectOptions options = new CRUDSelectOptions.Builder()
                .withTimeout(1000)
                .withSelectLimit(50)
                .withSelectBatchSize(10)
                .withSelectAfter(tuple)
                .build();

        assertEquals(4, options.asMap().size());

        assertEquals(1000, options.asMap().get(CRUDBaseOptions.TIMEOUT));
        assertEquals(50L, options.asMap().get(CRUDSelectOptions.SELECT_LIMIT));
        assertEquals(10L, options.asMap().get(CRUDSelectOptions.SELECT_BATCH_SIZE));
        assertEquals(tuple, options.asMap().get(CRUDSelectOptions.SELECT_AFTER));
    }

    @Test
    public void baseOperationOptions_createNotEmptyTest() {
        CRUDBaseOptions options = new CRUDBaseOptions.Builder()
                .withTimeout(1000)
                .build();

        assertEquals(1, options.asMap().size());
        assertEquals(1000, options.asMap().get(CRUDBaseOptions.TIMEOUT));
    }
}