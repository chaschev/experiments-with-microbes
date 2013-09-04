package riskmatch;

/**
 * User: chasc_000
 * Date: 9/4/13
 */
public class TreeUtils {
    public static int depth(Tree tree){
        return depth(tree.root);
    }

    public  static <T> int depth(Node<T> node){
        if(node.isLeaf()){
            return 1;
        }else{
            int maxChildDepth = -1;

            for (Node child : node.getChildren()) {
                maxChildDepth = Math.max(maxChildDepth, depth(child));
            }

            return maxChildDepth + 1;
        }
    }
}
