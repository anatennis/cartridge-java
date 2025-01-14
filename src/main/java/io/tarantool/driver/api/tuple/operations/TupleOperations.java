package io.tarantool.driver.api.tuple.operations;


import io.tarantool.driver.api.tuple.TarantoolField;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.exceptions.TarantoolSpaceOperationException;
import io.tarantool.driver.utils.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Create list of {@link TupleOperation} for update and upsert requests
 *
 * @author Sergey Volgin
 */
public final class TupleOperations {

    private final List<TupleOperation> operations = new ArrayList<>();
    private final List<TupleOperation> proxyOperations = new ArrayList<>();

    private TupleOperations(List<TupleOperation> operations) {
        this.operations.addAll(operations);
        this.proxyOperations.addAll(
                operations.stream().map(TupleOperation::toProxyTupleOperation).collect(Collectors.toList()));
    }

    private TupleOperations(TupleOperation operation) {
        addOperationToList(operation);
    }

    private void addOperationToList(TupleOperation operation) {
        this.operations.add(operation);
        this.proxyOperations.add(operation.toProxyTupleOperation());
    }

    /**
     * Add operation to the list
     *
     * @param operation for field
     * @return this
     */
    public TupleOperations addOperation(TupleOperation operation) {
        Integer fieldIndex = operation.getFieldIndex();
        String fieldName = operation.getFieldName();

        Optional<TupleOperation> existField;
        if (fieldIndex != null) {
            existField = this.operations.stream().filter(op -> fieldIndex.equals(op.getFieldIndex())).findFirst();
        } else {
            existField = this.operations.stream().filter(op -> fieldName.equals(op.getFieldName())).findFirst();
        }

        if (existField.isPresent()) {
            throw new TarantoolSpaceOperationException("Double update of the same field (%s)",
                    fieldIndex != null ? fieldIndex : fieldName);
        }

        addOperationToList(operation);
        return this;
    }

    /**
     * Get list of operations
     *
     * @return list of operations
     */
    public List<TupleOperation> asList() {
        return operations;
    }

    /**
     * Get a list of operations by converting field indexes starts with 0
     * to position numbers starts with 1 for working with lua box.space API
     *
     * @return list of operations
     */
    public List<TupleOperation> asProxyOperationList() {
        return proxyOperations;
    }

    /**
     * Build a collection of set operations from passed tuple
     *
     * @param tuple tuple, must not be null or empty
     * @return new instance
     */
    public static TupleOperations fromTarantoolTuple(TarantoolTuple tuple) {
        Assert.notNull(tuple, "Tuple for update must not be null");

        List<TarantoolField> tupleFields = tuple.getFields();
        if (tupleFields.isEmpty()) {
            throw new TarantoolSpaceOperationException("Cannot perform update with an empty tuple");
        }
        List<TupleOperation> operations = new ArrayList<>(tupleFields.size());
        for (int i = 0; i < tupleFields.size(); i++) {
            operations.add(new TupleOperationSet(i, tupleFields.get(i)));
        }
        return new TupleOperations(operations);
    }

    /**
     * Adds the specified value to the field value
     *
     * @param fieldIndex field number starting with 0
     * @param value      increment
     * @return new instance
     */
    public static TupleOperations add(int fieldIndex, Number value) {
        return new TupleOperations(new TupleOperationAdd(fieldIndex, value));
    }

    /**
     * Adds the specified value to the field value
     *
     * @param fieldIndex field number starting with 0
     * @param value      increment
     * @return this
     */
    public TupleOperations andAdd(int fieldIndex, Number value) {
        return addOperation(new TupleOperationAdd(fieldIndex, value));
    }

    /**
     * Adds the specified value to the field value
     *
     * @param fieldName field name
     * @param value     increment
     * @return new instance
     */
    public static TupleOperations add(String fieldName, Number value) {
        return new TupleOperations(new TupleOperationAdd(fieldName, value));
    }

    /**
     * Adds the specified value to the field value
     *
     * @param fieldName field name
     * @param value     increment
     * @return this
     */
    public TupleOperations andAdd(String fieldName, Number value) {
        return addOperation(new TupleOperationAdd(fieldName, value));
    }

    /**
     * Bitwise AND(&amp;) operation
     *
     * @param fieldIndex field number starting with 0
     * @param value      value
     * @return new instance
     */
    public static TupleOperations bitwiseAnd(int fieldIndex, long value) {
        return new TupleOperations(new TupleOperationBitwiseAnd(fieldIndex, value));
    }

    /**
     * Bitwise AND(&amp;) operation
     *
     * @param fieldIndex field number starting with 0
     * @param value      value
     * @return this
     */
    public TupleOperations andBitwiseAnd(int fieldIndex, long value) {
        return addOperation(new TupleOperationBitwiseAnd(fieldIndex, value));
    }

    /**
     * Bitwise AND(&amp;) operation
     *
     * @param fieldName field name
     * @param value     value
     * @return new instance
     */
    public static TupleOperations bitwiseAnd(String fieldName, long value) {
        return new TupleOperations(new TupleOperationBitwiseAnd(fieldName, value));
    }

    /**
     * Bitwise AND(&amp;) operation
     *
     * @param fieldName field name
     * @param value     value
     * @return this
     */
    public TupleOperations andBitwiseAnd(String fieldName, long value) {
        return addOperation(new TupleOperationBitwiseAnd(fieldName, value));
    }

    /**
     * Bitwise OR(|) operation
     *
     * @param fieldIndex field number starting with 0
     * @param value      value
     * @return new instance
     */
    public static TupleOperations bitwiseOr(int fieldIndex, long value) {
        return new TupleOperations(new TupleOperationBitwiseOr(fieldIndex, value));
    }

    /**
     * Bitwise OR(|) operation
     *
     * @param fieldIndex field number starting with 0
     * @param value      value
     * @return this
     */
    public TupleOperations andBitwiseOr(int fieldIndex, long value) {
        return addOperation(new TupleOperationBitwiseOr(fieldIndex, value));
    }

    /**
     * Bitwise OR(|) operation
     *
     * @param fieldName field name
     * @param value     value
     * @return new instance
     */
    public static TupleOperations bitwiseOr(String fieldName, long value) {
        return new TupleOperations(new TupleOperationBitwiseOr(fieldName, value));
    }

    /**
     * Bitwise OR(|) operation
     *
     * @param fieldName field name
     * @param value     value
     * @return this
     */
    public TupleOperations andBitwiseOr(String fieldName, long value) {
        return addOperation(new TupleOperationBitwiseOr(fieldName, value));
    }

    /**
     * Bitwise XOR(^) operation
     *
     * @param fieldIndex field number starting with 0
     * @param value      value
     * @return new instance
     */
    public static TupleOperations bitwiseXor(int fieldIndex, long value) {
        return new TupleOperations(new TupleOperationBitwiseXor(fieldIndex, value));
    }

    /**
     * Bitwise XOR(^) operation
     *
     * @param fieldIndex field number starting with 0
     * @param value      value
     * @return this
     */
    public TupleOperations andBitwiseXor(int fieldIndex, long value) {
        return addOperation(new TupleOperationBitwiseXor(fieldIndex, value));
    }

    /**
     * Bitwise XOR(^) operation
     *
     * @param fieldName field name
     * @param value     value
     * @return new instance
     */
    public static TupleOperations bitwiseXor(String fieldName, long value) {
        return new TupleOperations(new TupleOperationBitwiseXor(fieldName, value));
    }

    /**
     * Bitwise XOR(^) operation
     *
     * @param fieldName field name
     * @param value     value
     * @return this
     */
    public TupleOperations andBitwiseXor(String fieldName, long value) {
        return addOperation(new TupleOperationBitwiseXor(fieldName, value));
    }

    /**
     * Remove field value
     *
     * @param fieldIndex  start field number starting with 0 to start with
     * @param fieldsCount the number of fields to remove
     * @return new instance
     */
    public static TupleOperations delete(int fieldIndex, int fieldsCount) {
        return new TupleOperations(new TupleOperationDelete(fieldIndex, fieldsCount));
    }

    /**
     * Remove field value
     *
     * @param fieldIndex  start field number starting with 0 to start with
     * @param fieldsCount the number of fields to remove
     * @return this
     */
    public TupleOperations andDelete(int fieldIndex, int fieldsCount) {
        return addOperation(new TupleOperationDelete(fieldIndex, fieldsCount));
    }

    /**
     * Remove field value
     *
     * @param fieldName   field name to start with
     * @param fieldsCount the number of fields to remove
     * @return new instance
     */
    public static TupleOperations delete(String fieldName, int fieldsCount) {
        return new TupleOperations(new TupleOperationDelete(fieldName, fieldsCount));
    }

    /**
     * Remove field value
     *
     * @param fieldName   field name to start with
     * @param fieldsCount the number of fields to remove
     * @return this
     */
    public TupleOperations andDelete(String fieldName, int fieldsCount) {
        return addOperation(new TupleOperationDelete(fieldName, fieldsCount));
    }

    /**
     * Insert field value
     *
     * @param fieldIndex field number starting with 0 to insert after
     * @param value      the value to insert
     * @return new instance
     */
    public static TupleOperations insert(int fieldIndex, Object value) {
        return new TupleOperations(new TupleOperationInsert(fieldIndex, value));
    }

    /**
     * Insert field value
     *
     * @param fieldIndex field number starting with 0 to insert after
     * @param value      the value to insert
     * @return this
     */
    public TupleOperations andInsert(int fieldIndex, Object value) {
        return addOperation(new TupleOperationInsert(fieldIndex, value));
    }

    /**
     * Insert field value
     *
     * @param fieldName field name to insert after
     * @param value     the value to insert
     * @return new instance
     */
    public static TupleOperations insert(String fieldName, Object value) {
        return new TupleOperations(new TupleOperationInsert(fieldName, value));
    }

    /**
     * Insert field value
     *
     * @param fieldName field name to insert after
     * @param value     the value to insert
     * @return this
     */
    public TupleOperations andInsert(String fieldName, Object value) {
        return addOperation(new TupleOperationInsert(fieldName, value));
    }

    /**
     * Set field value
     *
     * @param fieldIndex field number starting with 0
     * @param value      the new value of a field
     * @return new instance
     */
    public static TupleOperations set(int fieldIndex, Object value) {
        return new TupleOperations(new TupleOperationSet(fieldIndex, value));
    }

    /**
     * Set field value
     *
     * @param fieldIndex field number starting with 0
     * @param value      the new value of a field
     * @return this
     */
    public TupleOperations andSet(int fieldIndex, Object value) {
        return addOperation(new TupleOperationSet(fieldIndex, value));
    }

    /**
     * Set field value
     *
     * @param fieldName field name
     * @param value     the new value of a field
     * @return new instance
     */
    public static TupleOperations set(String fieldName, Object value) {
        return new TupleOperations(new TupleOperationSet(fieldName, value));
    }

    /**
     * Set field value
     *
     * @param fieldName field name
     * @param value     the new value of a field
     * @return this
     */
    public TupleOperations andSet(String fieldName, Object value) {
        return addOperation(new TupleOperationSet(fieldName, value));
    }

    /**
     * Replace substring
     *
     * @param fieldIndex  field number starting with 0
     * @param position    the start'th position
     * @param offset      length of substring
     * @param replacement new value
     * @return new instance
     */
    public static TupleOperations splice(int fieldIndex, int position, int offset, String replacement) {
        return new TupleOperations(new TupleSpliceOperation(fieldIndex, position, offset, replacement));
    }

    /**
     * Replace substring
     *
     * @param fieldIndex  field number starting with 0
     * @param position    the start'th position
     * @param offset      length of substring
     * @param replacement new value
     * @return this
     */
    public TupleOperations andSplice(int fieldIndex, int position, int offset, String replacement) {
        return addOperation(new TupleSpliceOperation(fieldIndex, position, offset, replacement));
    }

    /**
     * Replace substring
     *
     * @param fieldName   field name
     * @param position    the start'th position
     * @param offset      length of substring
     * @param replacement new value
     * @return new instance
     */
    public static TupleOperations splice(String fieldName, int position, int offset, String replacement) {
        return new TupleOperations(new TupleSpliceOperation(fieldName, position, offset, replacement));
    }

    /**
     * Replace substring
     *
     * @param fieldName   field name
     * @param position    the start'th position
     * @param offset      length of substring
     * @param replacement new value
     * @return this
     */
    public TupleOperations andSplice(String fieldName, int position, int offset, String replacement) {
        return addOperation(new TupleSpliceOperation(fieldName, position, offset, replacement));
    }

    /**
     * Subtracts the specified value to the field value
     *
     * @param fieldIndex field number starting with 0
     * @param value      increment
     * @return this
     */
    public static TupleOperations subtract(int fieldIndex, Number value) {
        return new TupleOperations(new TupleOperationSubtract(fieldIndex, value));
    }

    /**
     * Subtracts the specified value to the field value
     *
     * @param fieldIndex field number starting with 0
     * @param value      increment
     * @return this
     */
    public TupleOperations andSubtract(int fieldIndex, Number value) {
        return addOperation(new TupleOperationSubtract(fieldIndex, value));
    }

    /**
     * Subtracts the specified value to the field value
     *
     * @param fieldName field name
     * @param value     increment
     * @return this
     */
    public static TupleOperations subtract(String fieldName, Number value) {
        return new TupleOperations(new TupleOperationSubtract(fieldName, value));
    }

    /**
     * Subtracts the specified value to the field value
     *
     * @param fieldName field name
     * @param value     increment
     * @return this
     */
    public TupleOperations andSubtract(String fieldName, Number value) {
        return addOperation(new TupleOperationSubtract(fieldName, value));
    }
}
