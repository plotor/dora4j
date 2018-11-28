package org.zhenchao.dora.support;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author zhenchao.wang 2018-11-14 12:16
 * @version 1.0.0
 */
public class Tuple2<T1, T2> implements ITuple, Iterable<Object>, Serializable {

    private static final long serialVersionUID = 2485675311476376451L;

    protected final T1 value1;
    protected final T2 value2;

    public Tuple2(T1 value1, T2 value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public T1 getValue1() {
        return value1;
    }

    public T2 getValue2() {
        return value2;
    }

    public List<Object> toList() {
        return Arrays.asList(this.toArray());
    }

    @Override
    public Object get(int index) {
        switch (index) {
            case 0:
                return value1;
            case 1:
                return value2;
            default:
                return null;
        }
    }

    @Override
    public Object[] toArray() {
        return new Object[] {value1, value2};
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public Iterator<Object> iterator() {
        return Collections.unmodifiableList(this.toList()).iterator();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) obj;
        return value1.equals(tuple2.value1) && value2.equals(tuple2.value2);
    }

    @Override
    public int hashCode() {
        int result = this.size();
        result = 31 * result + value1.hashCode();
        result = 31 * result + value2.hashCode();
        return result;
    }

}

