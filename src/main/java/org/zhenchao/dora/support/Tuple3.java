package org.zhenchao.dora.support;

/**
 * @author zhenchao.wang 2018-11-14 12:27
 * @version 1.0.0
 */
public class Tuple3<T1, T2, T3> extends Tuple2<T1, T2> {

    private static final long serialVersionUID = -830416232018631938L;

    protected final T3 value3;

    public Tuple3(T1 value1, T2 value2, T3 value3) {
        super(value1, value2);
        this.value3 = value3;
    }

    public T3 getValue3() {
        return value3;
    }

    @Override
    public Object get(int index) {
        switch (index) {
            case 0:
                return value1;
            case 1:
                return value2;
            case 2:
                return value3;
            default:
                return null;
        }
    }

    @Override
    public Object[] toArray() {
        return new Object[] {value1, value2, value3};
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Tuple3)) return false;
        if (!super.equals(obj)) return false;

        Tuple3 tuple3 = (Tuple3) obj;
        return value3.equals(tuple3.value3);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + value3.hashCode();
        return result;
    }
}
