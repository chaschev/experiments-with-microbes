package riskmatch;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static riskmatch.Node.node;
import static riskmatch.TreeUtils.depth;

/**
 * User: chaschev
 * Date: 9/4/13
 */
public class TreeUtilsTest {
    @Test
    public void testDepth() throws Exception {
        assertThat(depth(new Tree(node("root")))).isEqualTo(1);

        assertThat(depth(new Tree(
            node("root",
                node("lev1"))))).isEqualTo(2);

        assertThat(depth(new Tree(
            node("root",
                node("a",
                    node("aa")))
        ))).isEqualTo(3);


        assertThat(depth(new Tree(
            node("root",
                node("a",
                    node("aa"),
                    node("ab")
                ))
        ))).isEqualTo(3);

        assertThat(depth(new Tree(
            node("root",
                node("a",
                    node("aa")),
                node("b",
                    node("ba")))
        ))).isEqualTo(3);

        assertThat(depth(new Tree(
            node("root",
                node("a"),
                node("b",
                    node("ba")))
        ))).isEqualTo(3);

        assertThat(depth(new Tree(
            node("root",
                node("a",
                    node("aa")),
                node("b"))
        ))).isEqualTo(3);
    }
}
