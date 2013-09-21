package stackoverflow.q18686037;

import com.chaschev.chutils.util.Exceptions;
import com.chaschev.microbe.*;
import com.chaschev.microbe.trial.RandomArrayTrial;

import java.util.Random;

/**
 * User: chaschev
 * Date: 9/21/13
 */

/**
 * This doesn't answer the stackoverflow question, but says that creating objects in multiple threads can be more effective when there are a lot of objects.
 */
public class MultiThreadedNewMicrobe {
    public int dimensionSize = 400;
    public int threadCount = 4;

    public MultiThreadedNewMicrobe() {
    }

    private static class Tile {
        final int i, j;

        final long v;

        private Tile(int i, int j, long v) {
            this.i = i;
            this.j = j;
            this.v = v;
        }
    }

    public MultiThreadedNewMicrobe(int dimensionSize, int threadCount) {
        this.dimensionSize = dimensionSize;
        this.threadCount = threadCount;
    }

    public static void main(String[] args) {
        new MultiThreadedNewMicrobe(1000, 1).run(50, 50);
        new MultiThreadedNewMicrobe(1000, 4).run(50, 50);
    }

    public void run(int numberOfTrials, int warmUpTrials) {
        final RandomArrayTrial<Integer> trial = new RandomArrayTrial<Integer>(dimensionSize, new Integer[dimensionSize]) {
            Tile[][] tiles;

            {
                random = new Random(3);
            }

            @Override
            protected Integer createNew(int i) {
                return random.nextInt();
            }

            @Override
            public Microbe.Trial prepare() {
                super.prepare();

                tiles = new Tile[dimensionSize][dimensionSize];

                return this;
            }

            @Override
            public Measurements run(int trialIndex) {
                if (threadCount == 1) {
                    for (int i = 0; i < tiles.length; i++) {
                        for (int j = 0; j < tiles.length; j++) {
                            tiles[i][j] = new Tile(i, j, array[i] + array[j]);
                        }
                    }
                } else {
                    Thread[] threads = new Thread[threadCount];
                    for (int k = 0; k < threadCount; k++) {
                        final int finalK = k;
                        threads[k] = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = finalK; i < tiles.length; i += threadCount) {
                                    for (int j = 0; j < tiles.length; j++) {
                                        tiles[i][j] = new Tile(i, j, array[i] + array[j]);
                                    }
                                }
                            }
                        });

                        threads[k].start();
                    }

                    for (int i = 0; i < threads.length; i++) {
                        try {
                            threads[i].join();
                        } catch (InterruptedException e) {
                            throw Exceptions.runtime(e);
                        }
                    }
                }

                return new MeasurementsImpl();
            }

            @Override
            public void addResultsAfterCompletion(Measurements result) {
                long r = 0;
                for (Tile[] row : tiles) {
                    for (Tile tile : row) {
                        r += tile.v;
                    }
                }

                result.add(new Value(r % 10000, "checksum"));
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
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MultiThreadedNewMicrobe{");
        sb.append("dimensionSize=").append(dimensionSize);
        sb.append(", threadCount=").append(threadCount);
        sb.append('}');
        return sb.toString();
    }
}
