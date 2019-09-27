package BytecodeUtils;

import java.util.HashMap;
import java.util.Objects;

public class DoubleHashMap<T1, T2, E> extends HashMap<DoubleHashMap.KeyClass<T1, T2>, E> {
    public static class KeyClass<T1, T2> {
        private T1 t1;
        private T2 t2;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof KeyClass) {
                return t1.equals(((KeyClass) obj).t1) && t2.equals(((KeyClass) obj).t2);
            } else return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return Objects.hash(t1, t2);
        }

        public KeyClass(T1 t1, T2 t2) {
            this.t1 = t1;
            this.t2 = t2;
        }

        public T1 getT1() {
            return t1;
        }

        public void setT1(T1 t1) {
            this.t1 = t1;
        }

        public T2 getT2() {
            return t2;
        }

        public void setT2(T2 t2) {
            this.t2 = t2;
        }
    }

    public E put(T1 t1, T2 t2, E e) {
        return put(new KeyClass<>(t1, t2), e);
    }

    public E get(T1 t1, T2 t2) {
        return get(new KeyClass<>(t1, t2));
    }
}
