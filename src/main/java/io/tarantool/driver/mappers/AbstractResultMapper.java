package io.tarantool.driver.mappers;

import io.tarantool.driver.mappers.converters.ValueConverter;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.util.Optional;

/**
 * Base class for result tuple mappers
 *
 * @param <T> target result type
 * @author Alexey Kuzin
 * @author Artyom Dubinin
 */
public abstract class AbstractResultMapper<T> implements MessagePackValueMapper {

    protected final MessagePackValueMapper valueMapper;

    /**
     * Basic constructor
     *
     * @param valueMapper MessagePack value-to-object mapper for result contents
     * @param resultConverter converter from MessagePack result array to result type
     * @param resultClass target result class
     */
    public AbstractResultMapper(MessagePackValueMapper valueMapper,
                                ValueConverter<ArrayValue, ? extends T> resultConverter,
                                Class<? extends T> resultClass) {
        this.valueMapper = valueMapper;
        valueMapper.registerValueConverter(ValueType.ARRAY, resultClass, resultConverter);
    }

    @Override
    public <V extends Value, O> O fromValue(V v) throws MessagePackValueMapperException {
        return valueMapper.fromValue(v);
    }

    @Override
    public <V extends Value, O> O fromValue(V v, Class<O> targetClass) throws MessagePackValueMapperException {
        return valueMapper.fromValue(v, targetClass);
    }

    @Override
    public <V extends Value, O> void registerValueConverter(ValueType valueType,
                                                            Class<? extends O> objectClass,
                                                            ValueConverter<V, ? extends O> converter) {
        valueMapper.registerValueConverter(valueType, objectClass, converter);
    }

    @Override
    public <V extends Value, O> Optional<ValueConverter<V, O>> getValueConverter(ValueType valueType,
                                                                                 Class<O> objectClass) {
        return valueMapper.getValueConverter(valueType, objectClass);
    }
}
