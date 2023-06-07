/*******************************************************************************
 * Copyright 2016 SPECURE GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package at.alladin.rmbt.shared.db.repository;

import at.alladin.rmbt.shared.db.fields.*;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FieldAccessFactory<T> {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(FieldAccessFactory.class);

    final Class<T> clazz;

    final Map<String, FieldSupportAdapter<?>> fieldMap = new HashMap<>();

    public FieldAccessFactory(final Class<T> clazz) {
        this.clazz = clazz;

        for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
            final SerializedName serializedName = f.getAnnotation(SerializedName.class);
            if (serializedName != null) {
                if (f.getType().equals(String.class)) {
                    fieldMap.put(serializedName.value(), new StringFieldAdapter(f));
                } else if (f.getType().equals(Integer.class)) {
                    fieldMap.put(serializedName.value(), new IntFieldAdapter(f));
                } else if (f.getType().equals(Long.class)) {
                    fieldMap.put(serializedName.value(), new LongFieldAdapter(f));
                } else if (f.getType().equals(Double.class)) {
                    fieldMap.put(serializedName.value(), new DoubleFieldAdapter(f));
                } else if (f.getType().equals(Boolean.class)) {
                    fieldMap.put(serializedName.value(), new BooleanFieldAdapter(f));
                } else if (f.getType().equals(Timestamp.class)) {
                    fieldMap.put(serializedName.value(), new TimestampFieldAdapter(f));
                } else if (f.getType().equals(UUID.class)) {
                    fieldMap.put(serializedName.value(), new UuidFieldAdapter(f));
                }
            }
        }
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public Map<String, FieldSupportAdapter<?>> getFieldMap() {
        return fieldMap;
    }

    public abstract class FieldSupportAdapter<X> {
        final java.lang.reflect.Field field;

        public FieldSupportAdapter(java.lang.reflect.Field field) {
            this.field = field;
        }

        public abstract Field generateField(T object, String key);
    }

    public class StringFieldAdapter extends FieldSupportAdapter<String> {
        public StringFieldAdapter(java.lang.reflect.Field field) {
            super(field);
        }

        @Override
        public Field generateField(T object, String key) {
            final Field f = new StringField(key, key);
            try {
                f.setString(String.valueOf(field.get(object)));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return f;
        }
    }

    public class LongFieldAdapter extends FieldSupportAdapter<Long> {
        public LongFieldAdapter(java.lang.reflect.Field field) {
            super(field);
        }

        @Override
        public Field generateField(T object, String key) {
            final LongField f = new LongField(key, key);
            try {
                f.setValue(field.getLong(object));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return f;
        }

    }

    public class IntFieldAdapter extends FieldSupportAdapter<Integer> {
        public IntFieldAdapter(java.lang.reflect.Field field) {
            super(field);
        }

        @Override
        public Field generateField(T object, String key) {
            final IntField f = new IntField(key, key);
            try {
                f.setValue(field.getInt(object));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return f;
        }
    }

    public class BooleanFieldAdapter extends FieldSupportAdapter<Boolean> {
        public BooleanFieldAdapter(java.lang.reflect.Field field) {
            super(field);
        }

        @Override
        public Field generateField(T object, String key) {
            final BooleanField f = new BooleanField(key, key);
            try {
                f.setString(String.valueOf(field.getBoolean(object)));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return f;
        }
    }

    public class DoubleFieldAdapter extends FieldSupportAdapter<Double> {
        public DoubleFieldAdapter(java.lang.reflect.Field field) {
            super(field);
        }

        @Override
        public Field generateField(T object, String key) {
            final DoubleField f = new DoubleField(key, key);
            try {
                f.setValue(field.getDouble(object));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return f;
        }
    }

    public class TimestampFieldAdapter extends FieldSupportAdapter<Timestamp> {
        public TimestampFieldAdapter(java.lang.reflect.Field field) {
            super(field);
        }

        @Override
        public Field generateField(T object, String key) {
            final TimestampField f = new TimestampField(key, key);
            try {
                f.setString(((Timestamp) (field.get(object))).toString());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return f;
        }
    }

    public class UuidFieldAdapter extends FieldSupportAdapter<UUID> {
        public UuidFieldAdapter(java.lang.reflect.Field field) {
            super(field);
        }

        @Override
        public Field generateField(T object, String key) {
            final UUIDField f = new UUIDField(key, key);
            try {
                f.setString(((UUID) field.get(object)).toString());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return f;
        }
    }
}
