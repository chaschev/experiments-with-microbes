package com.chaschev.microbe.samples;

//import gnu.trove.list.array.TIntArrayList;
//import gnu.trove.map.hash.TIntIntHashMap;

import com.chaschev.microbe.*;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * User: Admin
 * Date: 19.11.11
 */


public class MemConsumptionTest {
    public static final Logger logger = LoggerFactory.getLogger(MemConsumptionTest.class);

    public static void main(String[] args) {
        MemConsumptionTest mem = new MemConsumptionTest();

        mem.arrayListVsLinkedList();
//        mem.testStringMemoryConsumption();
//        mem.testObjectMemoryConsumption();
//        mem.testByteMemoryConsumption();
//        mem.test20ByteMemoryConsumption();
//        mem.test20byteMemoryConsumption();
//        mem.testMapsMemoryConsumption();
//        mem.testByteArrayMemoryConsumption();
//        mem.testDoubleMemoryConsumption();
//        mem.testStringsMemoryConsumption();
    }

    public void testStringMemoryConsumption() {
        final int numberOfTrials = 25;
        new Microbe(numberOfTrials, "testStringMemoryConsumption", new TrialFactory() {
            @Override
            public Microbe.Trial create(final int trialIndex) {
                return new AbstractTrial() {
                    int n = 5000;
                    String[] holder = new String[n];
                    int lFrom = 1500;
                    int lTo = 2000;
                    Random random = new Random(1);
                    long totalLength = 0;
                    int currentTo = (lTo - lFrom) * trialIndex / numberOfTrials + lFrom + 1;

                    public Measurements run(int index) {
                        for (int i = 0; i < n; i++) {
                            holder[i] = RandomStringUtils.random(random.nextInt(currentTo - lFrom) + lFrom);
                            totalLength += holder[i].length();
                        }

                        return new MeasurementsImpl();
                    }

                    public void addResultsAfterCompletion(Measurements result) {
                        result.addCoordinates(new float[]{(float) result.getMemory() / totalLength}, new String[]{"bytes per char"});
                    }
                };
            }
        }).runTrials();
    }




    //16
    public void testObjectMemoryConsumption() {
        new Microbe(50, "testObjectMemoryConsumption",
                new MemConsumptionTrialFactory<Object>(
                        new ObjectFactory<Object>() {
                            @Override
                            public Object create(int trialIndex) {
                                return new Object();
                            }
                        },
                        Object.class, 500000)
        ).runTrials();
    }


    //16
    public void testByteMemoryConsumption() {
        new Microbe(50, "testByteMemoryConsumption",
                new MemConsumptionTrialFactory<Byte>(
                        new ObjectFactory<Byte>() {
                            @Override
                            public Byte create(int trialIndex) {
                                return new Byte((byte) trialIndex);
                            }
                        },
                        Byte.class, 500000)
        ).runTrials();
    }


    static class ByteFields20 {
        Byte b00 = new Byte((byte) 1);
        Byte b01 = new Byte((byte) 1);
        Byte b02 = new Byte((byte) 1);
        Byte b03 = new Byte((byte) 1);
        Byte b04 = new Byte((byte) 1);
        Byte b05 = new Byte((byte) 1);
        Byte b06 = new Byte((byte) 1);
        Byte b07 = new Byte((byte) 1);
        Byte b08 = new Byte((byte) 1);
        Byte b09 = new Byte((byte) 1);
        Byte b10 = new Byte((byte) 1);
        Byte b11 = new Byte((byte) 1);
        Byte b12 = new Byte((byte) 1);
        Byte b13 = new Byte((byte) 1);
        Byte b14 = new Byte((byte) 1);
        Byte b15 = new Byte((byte) 1);
        Byte b16 = new Byte((byte) 1);
        Byte b17 = new Byte((byte) 1);
        Byte b18 = new Byte((byte) 1);
        Byte b19 = new Byte((byte) 1);
    }

    static class ByteFields20_2 {
        byte b00 = 1;
        byte b01 = 2;
        byte b02 = 3;
        byte b03 = 4;
        byte b04 = 5;
        byte b05 = 6;
        byte b06 = 7;
        byte b07 = 8;
        byte b08 = 9;
        byte b09 = 1;
        byte b10 = 2;
        byte b11 = 3;
        byte b12 = 4;
        byte b13 = 5;
        byte b14 = 6;
        byte b15 = 7;
        byte b16 = 8;
        byte b17 = 9;
        byte b18 = 0;
        byte b19 = 1;
    }

    @Test
    public void test(){

    }


    //408 bytes = 8 + 20 fields * (16 + 4 bytes)
    public void test20ByteMemoryConsumption() {
        new Microbe(50, "test20ByteMemoryConsumption",
                new MemConsumptionTrialFactory<ByteFields20>(
                        new ObjectFactory<ByteFields20>() {
                            @Override
                            public ByteFields20 create(int trialIndex) {
                                return new ByteFields20();
                            }
                        },
                        ByteFields20.class, 50000)
        ).runTrials();
    }


    //28 bytes = 8 + 20 fields * 1 byte
    public void test20byteMemoryConsumption() {
        new Microbe(50, "test20byteMemoryConsumption",
                new MemConsumptionTrialFactory<ByteFields20_2>(
                        new ObjectFactory<ByteFields20_2>() {
                            @Override
                            public ByteFields20_2 create(int trialIndex) {
                                return new ByteFields20_2();
                            }
                        },
                        ByteFields20_2.class, 50000)
        ).runTrials();
    }

    public void arrayListVsLinkedList() {
        final int listSize = 50000;

        //2003K, 25.4s
        new Microbe(20,"linkedList",
            new MemConsumptionTrialFactory<List>(
                new ObjectFactory<List>() {
                    @Override
                    public List create(int trialIndex) {
                        List<Integer> integers = new LinkedList<Integer>();

                        for (int i = trialIndex; i < trialIndex + listSize; i++) {
                            integers.add(i * i + trialIndex);
                        }

                        return integers;
                    }

                    @Override
                    public boolean isGranular() {
                        return true;
                    }

                    @Override
                    public int granularity(int trialIndex) {
                        return listSize;
                    }
                },
                List.class, 50)
        ).runTrials();

        //1085K, 24.5s
        new Microbe(20,"arrayList",
            new MemConsumptionTrialFactory<List>(
                new ObjectFactory<List>() {
                    @Override
                    public List create(int trialIndex) {
                        List<Integer> integers = new ArrayList<Integer>();

                        for (int i = trialIndex; i < trialIndex + listSize; i++) {
                            integers.add(i * i + trialIndex);
                        }

                        return integers;
                    }

                    @Override
                    public boolean isGranular() {
                        return true;
                    }

                    @Override
                    public int granularity(int trialIndex) {
                        return listSize;
                    }
                },
                List.class, 50)
        )
            .runTrials();

        System.gc();
        //327K, 15.3s
        new Microbe(20, "TIntArrayList",
            new MemConsumptionTrialFactory<TIntArrayList>(
                new ObjectFactory<TIntArrayList>() {
                    @Override
                    public TIntArrayList create(int trialIndex) {
                        TIntArrayList integers = new TIntArrayList();

                        for (int i = trialIndex; i < trialIndex + listSize; i++) {
                            integers.add(i * i + trialIndex);
                        }

                        return integers;
                    }

                    @Override
                    public boolean isGranular() {
                        return true;
                    }

                    @Override
                    public int granularity(int trialIndex) {
                        return listSize;
                    }
                },
                TIntArrayList.class, 50)
        ).runTrials();
    }


    public void testMapsMemoryConsumption() {
        //231561 = 9000 * (8 + 21)
        new Microbe(50, "TIntIntHashMap",
                new MemConsumptionTrialFactory<TIntIntHashMap>(
                        new ObjectFactory<TIntIntHashMap>() {
                            @Override
                            public TIntIntHashMap create(int trialIndex) {
                                TIntIntHashMap map = new TIntIntHashMap();

                                for (int i = trialIndex; i < trialIndex + 9000; i++) {
                                    map.put(i, i * i + trialIndex);
                                }

                                return map;
                            }
                        },
                        TIntIntHashMap.class, 50)
        ).runTrials();

        //567338
        new Microbe(50, "HashMap<Integer, Integer>",
                new MemConsumptionTrialFactory<Map>(
                        new ObjectFactory<Map>() {
                            @Override
                            public Map<Integer, Integer> create(int trialIndex) {
                                Map<Integer, Integer> map = new HashMap<Integer, Integer>();

                                for (int i = trialIndex; i < trialIndex + 9000; i++) {
                                    map.put(i, i * i + trialIndex);
                                }

                                return map;
                            }
                        },
                        Map.class, 50)
        ).runTrials();
    }


    //1036 = 1024 + 8
    public void testByteArrayMemoryConsumption() {
        new Microbe(50, "testByteArrayMemoryConsumption",
                new MemConsumptionTrialFactory<byte[]>(
                        new ObjectFactory<byte[]>() {
                            @Override
                            public byte[] create(int trialIndex) {
                                return new byte[1024];
                            }
                        },
                        byte[].class, 50000)
        ).runTrials();
    }


    public void testDoubleMemoryConsumption() {
        //16
        new Microbe(50, "testDoubleMemoryConsumption",
                new MemConsumptionTrialFactory<Double>(
                        new ObjectFactory<Double>() {
                            @Override
                            public Double create(int trialIndex) {
                                return new Double(trialIndex);
                            }
                        },
                        Double.class, 200000)
        ).runTrials();
    }

    public void testStringsMemoryConsumption() {
        int[] lengths = new int[]{0, 1, 6, 10, 20, 50, 100, 2000};

        for (final int length : lengths) {
            System.out.println("string[" + length + "]");
            new Microbe(20, "testStringsMemoryConsumption",
                    new MemConsumptionTrialFactory<String>(
                            new ObjectFactory<String>() {
                                @Override
                                public String create(int trialIndex) {
                                    return RandomStringUtils.random(length, true, false);
                                }
                            },
                            String.class, 2000000 / (length + 1)
                    )
            ).runTrials();
        }
        //16
    }





}
