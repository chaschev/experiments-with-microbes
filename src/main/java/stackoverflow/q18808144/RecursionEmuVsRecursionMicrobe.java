package stackoverflow.q18808144;

import com.chaschev.microbe.*;
import com.chaschev.microbe.trial.RandomIntArrayTrial;

import java.util.Random;

/**
 * User: chaschev
 * Date: 9/21/13
 */

/**
 * Recursion 2x (small usage) - 5x (high usage) faster than it's emulation.
 */
public class RecursionEmuVsRecursionMicrobe {
    int n;

    boolean recursive;

    public RecursionEmuVsRecursionMicrobe(int n, boolean recursive) {
        this.n = n;
        this.recursive = recursive;
    }

    public static void main(String[] args) {
        new RecursionEmuVsRecursionMicrobe(10, true).run(1000);
        new RecursionEmuVsRecursionMicrobe(10, false).run(1000);

        new RecursionEmuVsRecursionMicrobe(20, true).run(100);
        new RecursionEmuVsRecursionMicrobe(20, false).run(100);

        new RecursionEmuVsRecursionMicrobe(30, true).run(100);
        new RecursionEmuVsRecursionMicrobe(30, false).run(100);
    }

    static class RecursiveCalculation {
        int[] numbers;

        RecursiveCalculation setNumbers(int[] numbers) {
            this.numbers = numbers;
            return this;
        }

        long calc(int n) {
            switch (n) {
                case 0:
                    return 0 + numbers[0];
                case 1:
                    return 1 + numbers[1];
                default:
                    return calc(n - 1) + calc(n - 2) + numbers[n];
            }
        }
    }

    static class StackEntry {
        int n;

        long v;

        byte state;

        public static final byte WAITING_CHILDREN = 1;

        public static final byte INPUT = 2;

        public static final byte CHILD_RESULT = 3;


        final void setArg(int n) {
            state = INPUT;
            this.n = n;
        }

        final void setResult(int n, long v) {
            state = CHILD_RESULT;
            this.n = n;
            this.v = v;
        }

        String stateToString() {
            switch (state) {
                case WAITING_CHILDREN:
                    return "WAITING_CHILDREN";
                case INPUT:
                    return "INPUT";
                case CHILD_RESULT:
                    return "CHILD_RESULT";
                default:
                    return "NOT_INIT";
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("StackEntry{");
            sb.append("n=").append(n);
            sb.append(", state=").append(stateToString());
            sb.append(", v=").append(v);
            sb.append('}');
            return sb.toString();
        }

        public void setWaiting(int n) {
            state = WAITING_CHILDREN;
            this.n = n;
        }

        public void assign(StackEntry e) {
            n = e.n;
            v = e.v;
            state = e.state;
        }
    }

    static class TestRecEmu {
        public static void main(String[] args) {
            final RecEmuCalculation loopCalculation = new RecEmuCalculation(10);

            loopCalculation.setNumbers(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 );

            System.out.println(loopCalculation.calc(1));
            System.out.println(loopCalculation.calc(2));
            System.out.println(loopCalculation.calc(3));
            System.out.println(loopCalculation.calc(4));
            System.out.println(loopCalculation.calc(5));
            System.out.println(loopCalculation.calc(6));
            System.out.println(loopCalculation.calc(7));
            System.out.println(loopCalculation.calc(8));
            System.out.println(loopCalculation.calc(9));
        }
    }

    static class RecEmuCalculation {
        final StackEntry[] stack;
        int size;

        int[] numbers;

        RecEmuCalculation setNumbers(int... numbers) {
            this.numbers = numbers;
            return this;
        }

        RecEmuCalculation(int n) {
            stack = new StackEntry[n * 4];

            for (int i = 0; i < stack.length; i++) {
                stack[i] = new StackEntry();
            }
        }

        final void pushInput(int n) {
            stack[size++].setArg(n);
        }

        final void pushResult(int n, long v) {
            stack[size++].setResult(n, v);
        }

        final void pushWaiting(int n) {
            stack[size++].setWaiting(n);
        }

        final StackEntry pop() {
            return stack[--size];
        }

        final StackEntry peek() {
            return stack[size - 1];
        }

        final long calc(int N) {
            size = 0;

            calcCall(N);

            while (!(size == 1 && peek().state == StackEntry.CHILD_RESULT)) {
                //10 -> 9?,8?,7?
                //10 -> 9?,8?, 7 -> 6?,5?
                //...
                //10 -> 9?,8?, 7 -> 6?, 5!
                //10 -> 9?,8?, 7 -> 5!, 6 -> ...
                //10 -> 9?,7!, 8 ->...
                //10 -> 7!, 8!, 9 ->

                //basically we need to scroll down the stack in search for either an input or waiting for child
                for (int i = size - 1; i >= 0; i--) {
                    StackEntry e = stack[i];

                    if (e.state == StackEntry.INPUT) {
                        int n = e.n;
                        //shift params left by 1
                        shift(i, size);
                        pop();
                        calcCall(n);
                        break;
                    } else if (e.state == StackEntry.WAITING_CHILDREN) {
                        //args are now in a reverse order, we don't care as we add them
                        e.state = StackEntry.CHILD_RESULT;
                        e.v = pop().v + pop().v + numbers[e.n];
                        size = i + 1;
                        break;
                    }
                }
            }

            return peek().v;
        }

        private void shift(int i, int n) {
            n--;
            for (; i < n; i++) {
                stack[i].assign(stack[i + 1]);
            }
        }

        private void calcCall(final int n) {
            switch (n) {
                case 0:
                    pushResult(n, 0 + numbers[0]);
                    break;
                case 1:
                    pushResult(n, 1 + numbers[1]);
                    break;

                default:
                    pushWaiting(n);
                    pushInput(n - 1);
                    pushInput(n - 2);
                    break;
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RecEmuVsRecursionMicrobe{");
        sb.append("n=").append(n);
        sb.append(", recursive=").append(recursive);
        sb.append('}');
        return sb.toString();
    }

    void run(int numberOfTrials) {
        final Random rootRandom = new Random(3);

        final RandomIntArrayTrial trial = new RandomIntArrayTrial(n + 1) {
            long r;

            final RecursiveCalculation recursiveCalculation = new RecursiveCalculation();
            final RecEmuCalculation loopCalculation = new RecEmuCalculation(n * 10);

            @Override
            public Microbe.Trial prepare() {
                random = new Random(rootRandom.nextInt());

                return super.prepare();
            }

            @Override
            public Measurements run(int trialIndex) {
//                Arrays.fill(array, 0);

                if(recursive){
                    r = recursiveCalculation.setNumbers(array).calc(n);
                }else{
                    r = loopCalculation.setNumbers(array).calc(n);
                }

                return MeasurementsImpl.EMPTY;
            }

            @Override
            public void addResultsAfterCompletion(Measurements result) {
                result.add(new Value(r % 10000, "checksum"));
            }
        };

        System.out.println(this);

        Microbe.newMicroCpu("RecEmuVsRecursion", numberOfTrials, new TrialFactory() {
            @Override
            public Microbe.Trial create(int trialIndex) {
                return trial;
            }
        }).setWarmUpTrials(numberOfTrials)
            .noSort()
            .runTrials();
    }
}
