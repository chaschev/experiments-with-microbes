package stackoverflow.q8621762;

import com.chaschev.microbe.*;
import com.google.caliper.Param;

/**
 * This test shows that having try-catch and no-try-catch are approximately the same in speed.
 * It also shows that integer division made in conditional operator is much slower than catching an exception.
 *
 * There is no cpu difference because of error percentage, so there should be no difference in whether we have try-catch or we don't have it.
 */
public class TryCatchMicrobe {
    public static void main(String[] args) {
        new TryCatchMicrobe(0, 100, true).run();
        new TryCatchMicrobe(0, 100, false).run();
        new TryCatchMicrobe(1, 100, true).run();
        new TryCatchMicrobe(1, 100, false).run();
        new TryCatchMicrobe(10, 100, true).run();
        new TryCatchMicrobe(10, 100, false).run();
        new TryCatchMicrobe(90, 100, true).run();
        new TryCatchMicrobe(90, 100, false).run();
    }

    @Param({"1", "10"})
    int m = 1;

    @Param("100")
    int n = 100;

    @Param({"true", "false"})
    boolean useTryCatch;

    public TryCatchMicrobe(int m, int n, boolean useTryCatch) {
        this.m = m;
        this.n = n;
        this.useTryCatch = useTryCatch;
    }

    public void run() {
        final class Foo {
            int i;
        }

        final Foo obj = new Foo();

        final AbstractTrial trial = new AbstractTrial() {
            int errorCount = 0;

            @Override
            public Measurements run(int i) {

                if (useTryCatch) {
                    try {
                        obj.i = i / (i % n < m ? 0 : 1);
                    } catch (Exception e) {
                        errorCount++;
                        obj.i++;
                    }
                } else {
                    final int divisor = i % n < m ? 0 : 1;
                    if (divisor != 0) {
                        obj.i = i / divisor;
                    } else {
                        errorCount++;
                        obj.i++;
                    }
                }

                if(obj.i < 0){
                    System.out.println(obj.i);
                }

                return MeasurementsImpl.EMPTY;
            }

            @Override
            public void addResultsAfterCompletion(Measurements result) {
                result.add(new Value(errorCount, "error count"));
            }
        };

        System.out.println(this);

        Microbe.newMicroCpu("tryCatch", 5000000, new TrialFactory() {
            @Override
            public Microbe.Trial create(int trialIndex) {
                return trial;
            }
        }).setWarmUpTrials(5000000)
            .noSort()
            .runTrials();
    }

    @Override
    public String toString() {
        return String.format("=== Error pct: %.1f%%, try-catch used: %s%n", 100d * m / n, useTryCatch);
    }
}