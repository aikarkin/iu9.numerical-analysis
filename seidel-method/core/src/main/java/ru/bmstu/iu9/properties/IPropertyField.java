package ru.bmstu.iu9.properties;

public interface IPropertyField {

    String getKey();

    Class<?> getType();

    Object getValue();

    void setValue(Object value);

}
