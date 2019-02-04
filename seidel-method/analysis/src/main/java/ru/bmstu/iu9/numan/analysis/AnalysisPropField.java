package ru.bmstu.iu9.numan.analysis;

import ru.bmstu.iu9.properties.IPropertyField;

enum AnalysisPropField implements IPropertyField {

    MIN_DEVIATION(Double.class, "analysis.cond.deviation.min", null),
    MAX_DEVIATION(Double.class, "analysis.cond.deviation.max", null),
    PRECISION(Double.class, "analysis.cond.precision", null),
    MIN_ELEM_VAL(Double.class, "analysis.cond.element.min", null),
    MAX_ELEM_VAL(Double.class, "analysis.cond.element.max", null),
    TESTS_COUNT(Integer.class, "analysis.cond.tests.count", null),
    MIN_EQUATIONS_COUNT(Integer.class, "analysis.cond.equations.min", null),
    MAX_EQUATIONS_COUNT(Integer.class, "analysis.cond.equations.max", null),
    MIN_BLOCK_DIM(Integer.class, "analysis.cond.block.dim.min", null),
    MAX_BLOCK_DIM(Integer.class, "analysis.cond.block.dim.max", null),
    ;

    private String key;
    private Object value;
    private Class<?> paramType;

    AnalysisPropField(Class<?> paramType, String key, Object value) {
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
