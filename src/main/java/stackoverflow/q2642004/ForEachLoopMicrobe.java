package stackoverflow.q2642004;

import com.chaschev.microbe.*;

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

        new ForEachLoopMicrobe(100, true).run(80000, 160000);
        new ForEachLoopMicrobe(100, false).run(80000, 160000);

        new ForEachLoopMicrobe(10, true).run(800000, 1600000);
        new ForEachLoopMicrobe(10, false).run(800000, 1600000);
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

        final AbstractTrial trial = new RandomArrayListTrial<Foo>(elementCount) {
            long r;

            {
                random = new Random(6);
            }

            @Override
            protected Foo createNew(int i) {
                return new Foo(random.nextInt());
            }


            @SuppressWarnings("ForLoopReplaceableByForEach")
            @Override
            public Measurements run(int i) {

                long sum = 0;

                if(useForEach){
                    int j = 0;
                    for (Foo f : arrayList) {
                        sum += f.v * i * j;
                        j++;
                    }
                }else{
                    final int size = arrayList.size();

                    for (int j = 0; j < size; j++) {
                        sum += arrayList.get(j).v * i * j;
                    }
                }

                r = sum;

                return MeasurementsImpl.EMPTY;
            }

            @Override
            public void addResultsAfterCompletion(Measurements result) {
                result.add(new Value(r % 10000, "checksum"));
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