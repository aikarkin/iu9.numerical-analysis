package ru.bmstu.iu9.numan.testgen;

enum TestParameter {
    TESTS(Integer.class, "tests.count", 5),
    START_WITH(Integer.class, "tests.startwith", 1),
    MATRIX_TYPE(MatrixType.class, "tests.block.type", MatrixType.DIAGONAL_DOMINANT),
    SHOULD_OVERWRITE(Boolean.class, "tests.overwrite", false),
    BLOCK_DIM(Integer.class, "tests.block.dim", 4),
    EQUATIONS(Integer.class, "tests.equations.count", 5),
    MIN_VAL(Double.class, "tests.element.minvalue", -3.0),
    MAX_VAL(Double.class, "tests.element.maxvalue", 3.0),
    ;

    private String key;
    private Object defaultValue;
    private Object value;
    private Class<?> paramType;

    TestParameter(Class<?> paramType, String key, Object value) {
        this.key = key;
        this.defaultValue = value;
        this.paramType = paramType;
    }

    public String getKey() {
        return key;
    }

    public Class<?> getType() {
        return paramType;
    }

    public Object getValue() {
        return value == null ? defaultValue : value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
