package stackoverflow.q709961;

import com.chaschev.microbe.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author Andrey Chaschev chaschev@gmail.com
 */
public class WrappersMicrobe {
    private static class Impl1 {
        public static boolean isWrapperType(Class<?> clazz) {
            return WRAPPER_TYPES.contains(clazz);
        }

        private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

        private static Set<Class<?>> getWrapperTypes() {
            Set<Class<?>> ret = new HashSet<Class<?>>();
            ret.add(Boolean.class);
            ret.add(Character.class);
            ret.add(Byte.class);
            ret.add(Short.class);
            ret.add(Integer.class);
            ret.add(Long.class);
            ret.add(Float.class);
            ret.add(Double.class);
            ret.add(Void.class);
            return ret;
        }
    }

    private static class Impl2 {
        public static boolean isWrapperType(Class<?> clazz) {
            return Number.class.isAssignableFrom(clazz) ||
                clazz == Boolean.class ||
                clazz == Character.class ||
                clazz == Void.class;
        }
    }


    final int n;
    final boolean implementation1;

    public WrappersMicrobe(int n, boolean implementation1) {
        this.n = n;
        this.implementation1 = implementation1;
    }

    public static void main(String[] args) {
        new WrappersMicrobe(100000, true).run(1000);
        new WrappersMicrobe(100000, false).run(1000);

        new WrappersMicrobe(1000000, true).run(100);
        new WrappersMicrobe(1000000, false).run(100);
    }

    public void run(int trials) {
        final ArrayList<Class<?>> classes = new ArrayList<Class<?>>(Impl1.WRAPPER_TYPES);

        //add more non-wrapper types for a more real situation
        classes.add(String.class);
        classes.add(String.class);
        classes.add(String.class);
        classes.add(String.class);
        classes.add(String.class);
        classes.add(String.class);
        classes.add(String.class);
        classes.add(int.class);
        classes.add(int.class);
        classes.add(int.class);
        classes.add(double.class);
        classes.add(double.class);
        classes.add(float.class);
        classes.add(float.class);

        for (Class<?> aClass : classes) {
            if(Impl1.isWrapperType(aClass) != Impl2.isWrapperType(aClass)){
                throw new RuntimeException("oops!");
            }
        }

        final AbstractTrial trial = new RandomArrayListTrial<Class>(n) {
            int checksum = 0;

            {
                random = new Random(2);
            }

            @Override
            public Measurements run(int i) {
                for (Class foo : arrayList) {
                    checksum += (implementation1 ? Impl1.isWrapperType(foo) : Impl2.isWrapperType(foo)) ? 1 : 0;
                }

                return MeasurementsImpl.EMPTY;
            }

            @Override
            public void addResultsAfterCompletion(Measurements result) {
                result.add(new Value((int)(checksum % 1000), "checksum"));
            }

            @Override
            protected Class createNew(int i) {
                return classes.get(random.nextInt(classes.size()));
            }
        };

        System.out.println(this);

        Microbe.newMicroCpu("wrappers", trials, new TrialFactory() {
            @Override
            public Microbe.Trial create(int trialIndex) {
                return trial;
            }
        })
            .setWarmUpTrials(trials)
            .noSort()
            .runTrials();
    }

    @Override
    public String toString() {
        return "Primitives Microbe: Impl" + (implementation1 ? 1:2) + ", n = " + n;
    }
}
