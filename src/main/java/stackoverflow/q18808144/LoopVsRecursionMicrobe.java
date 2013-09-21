package stackoverflow.q18808144;

import com.chaschev.microbe.*;
import com.chaschev.microbe.trial.AbstractTrial;

import java.util.Random;

/**
 * User: chaschev
 * Date: 9/21/13
 */


/**
 * Loop is 10x faster
 */
public class LoopVsRecursionMicrobe {
    int n;

    boolean recursive;

    public LoopVsRecursionMicrobe(int n, boolean recursive) {
        this.n = n;
        this.recursive = recursive;
    }

    public static void main(String[] args) {
        new LoopVsRecursionMicrobe(60, true).run(100000);
        new LoopVsRecursionMicrobe(60, false).run(100000);
    }

    private static abstract class Calculation{
        int[][] matrix;

        protected Calculation(int[][] matrix) {
            this.matrix = matrix;
        }

        public abstract long calc();
    }

    static class LoopCalculation extends Calculation{

        protected LoopCalculation(int[][] matrix) {
            super(matrix);
        }

        @Override
        public long calc() {
            long r = 0;

            for (int i = 0; i < matrix.length; i++) {
                final int[] row = matrix[i];
                for (int j = 0; j < row.length; j++) {
                    r += row[j];
                }
            }

            return r;
        }
    }

    static class RecursiveCalculation  extends Calculation{
        protected RecursiveCalculation(int[][] matrix) {
            super(matrix);
        }

        @Override
        public long calc() {
            return calcRec(0, 0);
        }

        private long calcRec(int i, int j) {
            if(j < matrix.length){
                return matrix[i][j] + calcRec(i, j + 1);
            }else{
                if(i == matrix.length - 1){
                    return 0;
                }

                return calcRec(i + 1, 0);
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LoopVsRecursionMicrobe{");
        sb.append("n=").append(n);
        sb.append(", recursive=").append(recursive);
        sb.append('}');
        return sb.toString();
    }

    void run(int numberOfTrials) {
        final Random rootRandom = new Random(4);
        final AbstractTrial trial = new com.chaschev.microbe.trial.AbstractTrial() {
            long r;

            int[][] matrix = new int[n][n];

            final RecursiveCalculation recursiveCalculation = new RecursiveCalculation(matrix);
            final LoopCalculation loopCalculation = new LoopCalculation(matrix);

            @Override
            public Microbe.Trial prepare() {
                Random random = new Random(rootRandom.nextInt());

                //speed up generation a bit
                int[] ref = new int[n];

                for (int i = 0; i < ref.length; i++) {
                    ref[i] = random.nextInt();
                }

                for (int i = 0; i < matrix.length; i++) {
                    final int[] row = matrix[i];
                    final int v = random.nextInt();
                    for (int j = 0; j < row.length; j++) {
                        row[j] = ref[i] * v;
                    }
                }

                return this;
            }

            @Override
            public Measurements run(int trialIndex) {
//                Arrays.fill(array, 0);

                if(recursive){
                    r = recursiveCalculation.calc();
                }else{
                    r = loopCalculation.calc();
                }

                return MeasurementsImpl.EMPTY;
            }

            @Override
            public void addResultsAfterCompletion(Measurements result) {
                result.add(new Value(r % 10000, "checksum"));
            }
        };

        System.out.println(this);

        Microbe.newMicroCpu("LoopVsRecursion", numberOfTrials, new TrialFactory() {
            @Override
            public Microbe.Trial create(int trialIndex) {
                return trial;
            }
        }).setWarmUpTrials(numberOfTrials)
            .noSort()
            .runTrials();
    }
}
