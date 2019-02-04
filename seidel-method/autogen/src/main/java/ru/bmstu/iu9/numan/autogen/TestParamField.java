package ru.bmstu.iu9.numan.autogen;

import ru.bmstu.iu9.properties.IPropertyField;

enum TestParamField implements IPropertyField {

    TESTS(Integer.class, "tests.count", 5),
    START_WITH(Integer.class, "tests.startwith", 1),
    MATRIX_TYPE(MatrixType.class, "tests.block.type", MatrixType.DIAGONAL_DOMINANT),
    SHOULD_OVERWRITE(Boolean.class, "tests.overwrite", false),
    BLOCK_DIM(Integer.class, "tests.block.dim", 4),
    EQUATIONS(Integer.class, "tests.equations.count", 5),
    MIN_VAL(Double.class, "tests.element.minvalue", -3.0),
    MAX_VAL(Double.class, "tests.element.maxvalue", 3.0),
    OUT_DIR(String.class, "tests.outdir", "./tests")
    ;

    private String key;
    private Object value;
    private Class<?> paramType;

    TestParamField(Class<?> paramType, String key, Object value) {
        this.key = key;
        this.value = value;
        this.paramType = paramType;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Class<?> getType() {
        return paramType;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }

}
