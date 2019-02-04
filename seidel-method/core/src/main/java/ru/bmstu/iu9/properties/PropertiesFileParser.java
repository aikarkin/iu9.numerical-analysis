package ru.bmstu.iu9.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;


public final class PropertiesFileParser {

    private static final Set<Class<?>> SUPPORTED_NUMBER_CLASSES;

    static {
        SUPPORTED_NUMBER_CLASSES = new HashSet<>();
        SUPPORTED_NUMBER_CLASSES.add(Integer.class);
        SUPPORTED_NUMBER_CLASSES.add(Double.class);
        SUPPORTED_NUMBER_CLASSES.add(Float.class);
    }

    private PropertiesFileParser() {
    }

    @SuppressWarnings("unchecked")
    public static void parseFile(File propertyFile, final IPropertyField[] propertyFields) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Properties props = new Properties();
        InputStream inputStream = new FileInputStream(propertyFile);

        props.load(inputStream);

        for (IPropertyField param : propertyFields) {
            if (props.containsKey(param.getKey())) {
                Object paramVal = null;
                String propVal = props.getProperty(param.getKey());
                if (SUPPORTED_NUMBER_CLASSES.contains(param.getType())) {
                    Optional<? extends Number> numOpt = tryParseNumber((Class<? extends Number>) param.getType(), propVal);
                    paramVal = numOpt.isPresent() ? numOpt.get() : propVal;
                } else if (param.getType() == Boolean.class) {
                    paramVal = Boolean.parseBoolean(propVal);
                } else if (param.getType().isEnum()) {
                    Method valuesMethod = param.getType().getDeclaredMethod("values");
                    Enum[] enumValues = (Enum[]) valuesMethod.invoke(null);

                    Optional<? extends Number> intOpt = tryParseNumber(Integer.class, propVal);
                    if (intOpt.isPresent()) {
                        int idx = (Integer) intOpt.get();
                        if (idx > enumValues.length) {
                            System.out.printf("[error] Invalid ordinal '%d' for enum type: %s%n", idx, param.getType().toString());
                            continue;
                        } else {
                            paramVal = enumValues[idx];
                        }
                    } else {
                        boolean isValidEnumName = false;

                        for (Enum val : enumValues) {
                            if (val.name().equals(propVal)) {
                                paramVal = val;
                                isValidEnumName = true;
                                break;
                            }
                        }

                        if (!isValidEnumName) {
                            System.out.printf("[error] Invalid enum type name: %s%n", propVal);
                            continue;
                        }
                    }

                } else {
                    paramVal = propVal;
                }

                param.setValue(paramVal);
            }
        }

        inputStream.close();
    }

    private static Optional<? extends Number> tryParseNumber(Class<? extends Number> clazz, String val) {
        if (val != null && !val.isEmpty()) {
            try {
                if (clazz == Integer.class) {
                    return Optional.of(Integer.parseInt(val));
                } else if (clazz == Double.class) {
                    return Optional.of(Double.parseDouble(val));
                } else if (clazz == Float.class) {
                    return Optional.of(Float.parseFloat(val));
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return Optional.empty();
    }

}
