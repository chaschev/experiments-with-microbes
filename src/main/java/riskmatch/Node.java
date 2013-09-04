package riskmatch;

import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: chasc_000
 * Date: 9/4/13
 */
public class Node<T> {
    protected T value;

    @Nullable
    protected List<Node<T>> children;

    public Node(T value) {
        this.value = value;
    }

    public Node(T value, Node... children) {
        this.children = (ArrayList) Lists.newArrayList(children);
    }

    public List<Node<T>> getChildren() {
        return children;
    }

    public boolean isLeaf(){
        return children == null || children.isEmpty();
    }

    public static <T> Node<T> node(T value, Node... children){
        return new Node<T>(value, children);
    }
}
