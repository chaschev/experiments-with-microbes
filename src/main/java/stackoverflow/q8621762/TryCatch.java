package stackoverflow.q8621762;

import com.google.caliper.BeforeExperiment;
import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;

/**
 * User: chaschev
 * Date: 9/3/13
 */
public class TryCatch {
    public static final class Foo{
        int i;
    }


    public static final class Microbe1 {
        Foo obj ;

        @Param({"1", "10"}) int m;

        @Param("100") int n;

        @Param({"true", "false"}) boolean useTryCatch;

        @BeforeExperiment
        protected void setUp() throws Exception {
            obj = new Foo();
        }

        @Benchmark
        public void tryCatch(int reps) {
            int errorCount = 0;

            if (useTryCatch) {
                for (int i = 0; i < reps; i++) {
                    try {
                        obj.i = i / (i % n < m ? 0 : 1);
                    } catch (Exception e) {
                        errorCount++;
                    }
                }
            } else {
                for (int i = 0; i < reps; i++) {
                    final int divisor = i % n < m ? 0 : 1;
                    if (divisor != 0){
                        obj.i = i / divisor;
                    }else{
                        errorCount++;
                        obj.i++;
                    }
                }
            }

            System.out.printf("try-catch: %s, pt: %.1f%%, error count: %d, reps:%d%n", useTryCatch,
                m * 100f / n, errorCount, reps);
        }
    }

    public static void main(String[] args) {
        CaliperMain.main(Microbe1.class, "-p -i runtime -Dm=90,1".split("\\s+"));
    }
}
