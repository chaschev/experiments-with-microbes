package stackoverflow.q6462055;

import com.chaschev.microbe.*;
import com.chaschev.microbe.trial.RandomMapTrial;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import java.util.*;

/**
 * User: chaschev
 * Date: 9/6/13
 */

/**
 * When positions are known, ArrayList is 2x faster than a HashMap.
 *
 * When they are not, HashMap is 2x faster for 3-elements arrays, 3x - for ten elements, 6x for 50 elements, 12x for 200, 532x for 10000 which demonstrate the linear dependency on the input data volume vs constant.
 */
public class HashMapVsListMicrobe {
    final static class Foo {
        int i;
        int v;

        Foo(int i, int v) {
            this.i = i;
            this.v = v;
        }
    }

    @Param
    final int elementCount;

    @Param
    final boolean positionsAreKnown;

    @Param
    final int existingElementsCount;

    @Param
    final int notExistingElementsCount;

    final List<Foo> elements;

    final TIntHashSet values;

    TIntArrayList existingElementRequests;
    TIntArrayList nonExistingElementRequests;

    public static void main(String[] args) {
        new HashMapVsListMicrobe(3, false, 10000, 100)
            .runForArrayList(10000, 10000)
            .runForMap(10000, 10000);

        new HashMapVsListMicrobe(10, false, 10000, 100)
            .runForArrayList(10000, 10000)
            .runForMap(10000, 10000);

        new HashMapVsListMicrobe(20, false, 10000, 100)
            .runForArrayList(1000, 1000)
            .runForMap(1000, 1000);

        new HashMapVsListMicrobe(50, false, 10000, 100)
            .runForArrayList(1000, 1000)
            .runForMap(1000, 1000);

        new HashMapVsListMicrobe(200, false, 10000, 100)
            .runForArrayList(1000, 1000)
            .runForMap(1000, 1000);

        new HashMapVsListMicrobe(10000, false, 10000, 100)
            .runForArrayList(50, 50)
            .runForMap(50, 50);

        new HashMapVsListMicrobe(10000, true, 10000, 100)
            .runForArrayList(1000, 1000)
            .runForMap(1000, 1000);
    }

    public HashMapVsListMicrobe(int elementCount, boolean positionsAreKnown, int existingElementsCount, int notExistingElementsCount) {
        this.elementCount = elementCount;
        this.positionsAreKnown = positionsAreKnown;
        this.existingElementsCount = existingElementsCount;
        this.notExistingElementsCount = notExistingElementsCount;

        elements = new ArrayList<Foo>(elementCount);
        values = new TIntHashSet(elementCount);

        final Random r = new Random();

        for (int i = 0; i < elementCount; i++) {
            final int v = r.nextInt();

            elements.add(new Foo(v, i));
            values.add(v);
        }

        Collections.shuffle(elements);

        existingElementRequests = new TIntArrayList(existingElementsCount);
        nonExistingElementRequests = new TIntArrayList(notExistingElementsCount);

        for (int i = 0; i < existingElementsCount; i++) {
            final int randomI = r.nextInt(elementCount);
            if (positionsAreKnown) {
                existingElementRequests.add(randomI);
            } else {
                existingElementRequests.add(elements.get(randomI).v);
            }
        }

        for (int i = 0; i < notExistingElementsCount; i++) {
            if (positionsAreKnown) {
                while (true) {
                    final int randomI = r.nextInt();
                    if (randomI >= 0 && randomI < notExistingElementsCount) continue;

                    nonExistingElementRequests.add(randomI);
                    break;
                }
            } else {
                while (true) {
                    final int randomI = r.nextInt();
                    if (values.contains(randomI)) continue;

                    nonExistingElementRequests.add(randomI);
                    break;
                }
            }
        }
    }

    public HashMapVsListMicrobe runForArrayList(int numberOfTrials, int warmUpTrials) {
        class Pred implements Predicate<Foo>{
            int v;

            @Override
            public boolean apply(Foo input) {
                return input.v == v;
            }

            Pred setV(int v) {
                this.v = v;

                return this;
            }
        }

        final Pred predicate = new Pred();

        final AbstractTrial trial = new RandomArrayListTrial<Foo>(elementCount) {
            @Override
            protected Foo createNew(int i) {
                return elements.get(i);
            }

            @Override
            public Measurements run(int i) {
                long sum = 0;

                final int size1 = existingElementRequests.size();
                final int size2 = nonExistingElementRequests.size();

                if (positionsAreKnown) {
                    for (int j = 0; j < size1; j++) {
                        sum += elements.get(existingElementRequests.getQuick(j)).i;
                    }

                    for (int j = 0; j < size2; j++) {
                        final int idx = nonExistingElementRequests.getQuick(j);

                        if (idx >= 0 && idx < size2) {
                            sum += elements.get(j).i;
                        }
                    }
                }else{
                    sum += calcSum(existingElementRequests);
                    sum += calcSum(nonExistingElementRequests);
                }

                if (sum % 129829381931L == 1) {
                    System.out.println("\n" + sum);
                }

                return new MeasurementsImpl();
            }

            private long calcSum(TIntArrayList list) {
                long sum = 0;
                int size = list.size();
                for (int j = 0; j < size; j++) {
                    final int v = list.getQuick(j);

                    int idx = Iterables.indexOf(elements, predicate.setV(v));

                    if(idx != -1){
                        sum += elements.get(idx).i;
                    }
                }
                return sum;
            }
        };

        System.out.println(this);

        Microbe.newMicroCpu("runForArrayList", numberOfTrials, new TrialFactory() {
            @Override
            public Microbe.Trial create(int trialIndex) {
                return trial;
            }
        }).setWarmUpTrials(warmUpTrials)
            .noSort()
            .runTrials();

        return this;
    }

    public HashMapVsListMicrobe runForMap(int numberOfTrials, int warmUpTrials) {
        final RandomMapTrial<Integer, Foo> trial = new RandomMapTrial<Integer, Foo>(elementCount) {
            @Override
            protected Map.Entry<Integer, Foo> createNew(int i) {
                final Foo foo = elements.get(i);
                return new AbstractMap.SimpleEntry<Integer, Foo>(positionsAreKnown ? foo.i : foo.v, foo);
            }

            @Override
            public Measurements run(int i) {
                long sum = 0;

                sum += calcSum(existingElementRequests);
                sum += calcSum(nonExistingElementRequests);


                if (sum % 129829381931L == 1) {
                    System.out.println(sum);
                }

                return new MeasurementsImpl();
            }

            private long calcSum(TIntArrayList list) {
                long sum = 0;

                int size = list.size();

                for (int j = 0; j < size; j++) {
                    final int v = list.getQuick(j);

                    final Foo foo = map.get(v);

                    if (foo != null) {
                        sum += positionsAreKnown ? foo.v : foo.i;
                    }
                }

                return sum;
            }
        };

        System.out.println(this);

        Microbe.newMicroCpu("runForMap", numberOfTrials, new TrialFactory() {
            @Override
            public Microbe.Trial create(int trialIndex) {
                return trial;
            }
        }).setWarmUpTrials(warmUpTrials)
            .noSort()
            .runTrials();

        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("=== HashMapVsListMicrobe{");
        sb.append("elementCount=").append(elementCount);
        sb.append(", positionsAreKnown=").append(positionsAreKnown);
        sb.append(", existingElementsCount=").append(existingElementsCount);
        sb.append(", notExistingElementsCount=").append(notExistingElementsCount);
        sb.append('}');
        return sb.toString();
    }
}
