package stackoverflow.json;

import com.chaschev.chutils.util.Exceptions;
import com.chaschev.microbe.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.caliper.Param;
import com.google.gson.Gson;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JsonMappingMicrobe {
    public static class Foo2 {
        public String f1;
        public String f2;

        public Foo2() {
        }

        public Foo2(String f1, String f2) {
            this.f1 = f1;
            this.f2 = f2;
        }

        public static Foo2 random() {
            return new Foo2(
                RandomStringUtils.random(20),
                RandomStringUtils.random(20)
            );
        }
    }

    public static class Foo {
        public String f1;
        public String f2;
        public String f3;
        public String f4;
        public String f5;
        public int f6;
        public int f7;

        public Foo2 f8;
        public Foo2 f9;

        public List<Foo2> foos;

        public Foo() {
        }

        public Foo(String f1, String f2, String f3, String f4, String f5, int f6, int f7, Foo2 f8, Foo2 f9, List<Foo2> foos) {
            this.f1 = f1;
            this.f2 = f2;
            this.f3 = f3;
            this.f4 = f4;
            this.f5 = f5;
            this.f6 = f6;
            this.f7 = f7;
            this.f8 = f8;
            this.f9 = f9;
            this.foos = foos;
        }

        public static Foo random(Random r) {
            int count = 2 + r.nextInt(5);
            List<Foo2> foos = new ArrayList<Foo2>(count);

            for (int i = 0; i < count; i++) {
                foos.add(Foo2.random());
            }

            return new Foo(
                RandomStringUtils.random(20),
                RandomStringUtils.random(20),
                RandomStringUtils.random(20),
                RandomStringUtils.random(20),
                RandomStringUtils.random(20),
                r.nextInt(),
                r.nextInt(),
                Foo2.random(),
                Foo2.random(),
                foos
            );
        }
    }

    public interface Mapper {
        String toJSON(Object obj);
        Object fromJSON(String s);
    }

    public static class JacksonMapper implements Mapper {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectWriter writer = mapper.writer();

        @Override
        public String toJSON(Object obj) {
            try {
                return writer.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                throw Exceptions.runtime(e);
            }
        }

        @Override
        public Object fromJSON(String s) {
            try {
                final ObjectReader reader = mapper.reader(Foo.class);

                return reader.readValue(s);
            } catch (IOException e) {
                throw Exceptions.runtime(e);
            }
        }
    }

    public static class GsonMapper implements Mapper {
        final Gson gson = new Gson();

        @Override
        public String toJSON(Object obj) {
            return gson.toJson(obj);
        }

        @Override
        public Object fromJSON(String s) {
            return gson.fromJson(s, Foo.class);
        }
    }

    public static void main(String[] args) {
        new JsonMappingMicrobe(true, 10).run(20000);
        new JsonMappingMicrobe(false, 10).run(20000);

        new JsonMappingMicrobe(true, true, 10).run(20000);
        new JsonMappingMicrobe(false, true, 10).run(20000);
    }

    boolean jackson = true;

    boolean fromJSON = false;


    @Param("100")
    int n = 100;

    public JsonMappingMicrobe(boolean jackson, int n) {
        this.jackson = jackson;
        this.n = n;
    }

    public JsonMappingMicrobe(boolean jackson, boolean fromJSON, int n) {
        this.jackson = jackson;
        this.fromJSON = fromJSON;
        this.n = n;
    }

    public void run(int trials) {
        final Mapper mapper = jackson ? new JacksonMapper() : new GsonMapper();

        final AbstractTrial trial = new RandomArrayListTrial<Foo>(n) {
            int checksum1 = 0;
            int checksum2 = 0;

            @Override
            public Measurements run(int i) {
                for (Foo foo : arrayList) {
                    String to = mapper.toJSON(foo);
                    checksum1 += to.length();

                    if(fromJSON){
                        Foo foo2 = (Foo) mapper.fromJSON(to);
                        checksum2 += foo2.f6;
                    }
                }

                return MeasurementsImpl.EMPTY;
            }

            @Override
            public void addResultsAfterCompletion(Measurements result) {
                result.add(new Value((int)(checksum1 % 1000), "checksum"));
            }

            @Override
            protected Foo createNew(int i) {
                return Foo.random(random);
            }
        };

        System.out.println(this);

        Microbe.newMicroCpu("tryCatch", trials, new TrialFactory() {
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
        return String.format("=== n: %d, tool: %s%n", n, jackson ? "jackson" : "gson");
    }
}