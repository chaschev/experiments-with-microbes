package stackoverflow.q2642004;

import com.chaschev.microbe.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * In all four cases for-each is 2-3 times faster.
 */
public class ForEachLoopMicrobe {
    public static void main(String[] args) {
        new ForEachLoopMicrobe(100000, true).run(2000, 4000);
        new ForEachLoopMicrobe(100000, false).run(2000, 4000);

        new ForEachLoopMicrobe(10000, true).run(20000, 40000);
        new ForEachLoopMicrobe(10000, false).run(20000, 40000);

        new ForEachLoopMicrobe(100, true).run(20000, 40000);
        new ForEachLoopMicrobe(100, false).run(20000, 40000);

        new ForEachLoopMicrobe(10, true).run(200000, 400000);
        new ForEachLoopMicrobe(10, false).run(200000, 400000);
    }

    int elementCount;

    boolean useForEach;

    public ForEachLoopMicrobe(int elementCount, boolean useForEach) {
        this.elementCount = elementCount;
        this.useForEach = useForEach;
    }

    public void run(int numberOfTrials, int warmUpTrials) {
        final class Foo{
            int v;

            Foo(int v) {
                this.v = v;
            }
        }

        final Microbe.AbstractTrial trial = new Microbe.AbstractTrial() {
            List<Foo> integers = new ArrayList<Foo>(elementCount);

            @Override
            public Microbe.Trial prepare() {
                integers.clear();

                final Random r = new Random();

                for(int i = 0;i<elementCount;i++){
                    integers.add(new Foo(r.nextInt()));
                }

                return this;
            }

            @SuppressWarnings("ForLoopReplaceableByForEach")
            @Override
            public Measurements run(int i) {

                long sum = 0;

                if(useForEach){
                    for (Foo f : integers) {
                        sum += f.v;
                    }
                }else{
                    final int size = integers.size();

                    for (int j = 0; j < size; j++) {
                        sum += integers.get(j).v;
                    }
                }

                if(sum % 293829381 == 1){
                    System.out.println(sum);
                }


                return new MeasurementsImpl();
            }

        };

        System.out.println(this);

        Microbe.newMicroCpu("forEach", numberOfTrials, new TrialFactory() {
            @Override
            public Microbe.Trial create(int trialIndex) {
                return trial;
            }
        }).setWarmUpTrials(warmUpTrials)
            .noSort()
            .runTrials();
    }

    @Override
    public String toString() {
        return String.format("=== Element count: %d, for-each: %s %n", elementCount, useForEach);
    }
}