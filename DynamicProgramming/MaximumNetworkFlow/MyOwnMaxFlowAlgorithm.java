package DynamicProgramming.MaximumNetworkFlow;


import java.util.*;

public class MyOwnMaxFlowAlgorithm<E> {


    HashMap<E, Node<E>> GraphNodes = new HashMap<>();



    public void SortListEdge(Node<E> gNode) {
        sort<E> eSort = new sort<>();
        eSort.QuickSortTraditionalMethodNonRecursion(gNode.Edges);
    }







    public void insert(E StartName, E destinationName, int distance) {

        Node<E> destination, StartNode;
        destination = this.GraphNodes.get(destinationName);
        StartNode = this.GraphNodes.get(StartName);

        if (destination == null) { // if the destination is not exist
            destination = new Node<>(destinationName);
            this.GraphNodes.put(destinationName, destination); // add destination to graph node
        }

        if (StartNode == null) { // if start is not exist

            Node<E> newNode = new Node<>(StartName);
            StartNode = newNode;
            this.GraphNodes.put(StartName, newNode); // add create start

        }
        StartNode.Edges.add(new Edge<>(StartNode, destination, distance)); // add the edge to the start node.

        SortListEdge(StartNode);


    }




    private void resetEdges (){
        for (Node<E> node: GraphNodes.values()){
            for (Edge<E> edge : node.Edges){
                edge.weight = 0;
            }
        }
    }





    private void resetNode () {
        for (Node<E> node : GraphNodes.values()) {
            node.level = -1;
        }
    }





    private boolean checkLeaf (E StartName) {
        boolean higherLevel = false;
        boolean smallerLevel = false;
        Node<E> StartNode = GraphNodes.get(StartName);
        for (Edge<E> edge : StartNode.Edges){
            if (edge.destination.level > StartNode.level){
                higherLevel = true;
            }else {
                smallerLevel = true;
            }
        }
        return !higherLevel || !smallerLevel;

    }



    private void constructLevel(E StartName, E DestinationName) {

        if (!StartName.equals(DestinationName)) {

            Node<E> StartNode = GraphNodes.get(StartName), currentDestinationNode, desNode = GraphNodes.get(DestinationName);
            Queue<Node<E>> toVisit = new LinkedList<>();
            int len;
            Edge<E> edge;

            if (StartNode != null) {
                resetNode();
                toVisit.add(StartNode); // add start node to the toVisit queue
                StartNode.level = 0;

                while (!toVisit.isEmpty()) { // if the toVisit queue is not empty
                    StartNode = toVisit.remove(); // get the start node
                    len = StartNode.Edges.size();


                    for (int i = 0; i < len; i++) { // ==> check all its edges and update the level

                        edge = StartNode.Edges.get(i);
                        currentDestinationNode = edge.destination;
                        if (!StartNode.item.equals(DestinationName)) {
                            if (currentDestinationNode.level == -1 || StartNode.level <= currentDestinationNode.level|| currentDestinationNode.equals(desNode)) {
                                currentDestinationNode.level = Math.max(currentDestinationNode.level, StartNode.level + 1);
                                toVisit.add(currentDestinationNode); // if the destination not in the Queue ==> add to the queue.
                            }
                        }
                    }
                }

            }
        }
    }





    private int checkCapability (Stack<Edge<E>> stack){
        int remainCapability = Integer.MAX_VALUE;
        for (Edge<E> edge : stack){
            if (edge.Capability - edge.weight < remainCapability){
                remainCapability = edge.Capability - edge.weight;
            }
        }
        return remainCapability;
    }



    /*
        - put nodes to the stack of node, and push it's following edge to the stack of the toVisitEdges
        ==> the node and the stack has the same location in these 2 stack ==>  we pop 1 node => pop 1 stack
        and 2 of those are has a comparable.

        - each time, we pop a node out of the stack, we check the edges collection of that node if it is satisfied
        with the condition (which is explained in the code comment below).

        - whenever we pop a node out of the stack ==> pop an edge ==> that edge is the following edge of the node
        and point at that node.

        - each time we pop a node ==> pop it's following edge, then check and update the weight, based on the remain
        capability, by checking the bottleneck value in the path and update to all edge in that path.

        -
     */

    public List<List<String>> getOptimumPathFlow(E StartName, E DestinationName) {

        if (!StartName.equals(DestinationName)) {
            resetEdges();
            this.constructLevel(StartName, DestinationName); // construct the level to make sure the level increase from start to destination.

            Stack<Node<E>> toVisitNode = new Stack<>(); // stack to contain the Node
            Stack<Edge<E>> toVisitEdges = new Stack<>(); // stack to contain the Edges
            Stack<Edge<E>> edgesInPath = new Stack<>();  // stack to contain the Edges that in the current working branch
            Stack<Node<E>> visitedNodeInPath = new Stack<>();

            Node<E> StartNode = GraphNodes.get(StartName), desNode = GraphNodes.get(DestinationName), currentNode;
            Edge<E> followEdge, fullEdge;
            final int[] newFlowCap = {Integer.MAX_VALUE};
            int remainCapacity;
            int total = 0;

            List<List<String>> PathCollection = new LinkedList<>();

            class local {
                void resetEdgeStacks() { // reset edge stacks
                    if (!toVisitNode.isEmpty()) {
                        popEdges(toVisitEdges, toVisitNode.peek().item);
                    }else {
                        toVisitEdges.clear();
                    }

                    if (!toVisitEdges.isEmpty() && !(toVisitEdges.peek().previous == null)) {
                        popEdges(edgesInPath, toVisitEdges.peek().previous.item);
                    } else {
                        edgesInPath.clear();
                    }
                }

                void resetVisited (){
                    if (edgesInPath.isEmpty()){
                        visitedNodeInPath.clear();
                        visitedNodeInPath.push(StartNode);
                    }else {
                        popNodeVisited(visitedNodeInPath, edgesInPath.peek().destination);
                    }
                }


                void flowMaintain(){
                    if (edgesInPath.isEmpty()) {
                        newFlowCap[0] = Integer.MAX_VALUE;
                    }else {
                        newFlowCap[0] = checkCapability(edgesInPath);
                    }
                }
            }

            local Maintain = new local();



            if (StartNode != null && desNode != null) {
                toVisitNode.push(StartNode);


                while (!toVisitNode.isEmpty()) {

                    currentNode = toVisitNode.pop();// get the current node.

                    // in case the the node run in the wrong way
                    // ==> the node is the leaf but it is not the destination or the path is run further than the destination
                    if ((currentNode.level >= desNode.level && !currentNode.equals(desNode)) || ((currentNode.Edges.size() == 1 && !visitedNodeInPath.isEmpty()
                        && visitedNodeInPath.contains(currentNode.Edges.get(0).destination)) || currentNode.Edges.isEmpty()) && !currentNode.equals(desNode)) {
                        Maintain.resetEdgeStacks();
                        Maintain.flowMaintain();

                    } else {

                        if (!toVisitEdges.isEmpty()) {
                            followEdge = toVisitEdges.pop(); // get the edge that point to the current Node.

                            // the rest capacity that can contain the flow == capacity - the current flow in the link.
                            remainCapacity = followEdge.Capability - followEdge.weight;
//                            System.out.println(followEdge.destination + "  :" + remainCapacity);

                            // find the bottleneck in the graph
                            newFlowCap[0] = Math.min(newFlowCap[0], remainCapacity);
                            edgesInPath.push(followEdge);

                            if (currentNode.item.equals(DestinationName)) {
                                // update and check full edge
                                fullEdge = this.UpdateFlowAndCheckCap(edgesInPath, newFlowCap[0]);
                                PathCollection.add(getPathCollection(StartName, edgesInPath, newFlowCap[0]));
                                total += newFlowCap[0];

                                if (fullEdge != null) {
                                    // pop Node until meet the same level of the fullEdge's destination or it meet the destination
                                    this.popNode(toVisitNode, fullEdge.destination, desNode);
                                    // update Edge stacks after pop Node.
                                    Maintain.resetEdgeStacks();

                                } else {
                                    // if there is no full edge ==> just update the edgesInPath with toVisitEdges
                                    this.popEdges(edgesInPath, toVisitEdges.peek().previous.item);

                                }
                                // also update the visited Node because when ever meet destination, we will run the other
                                // branches
                                Maintain.resetVisited();
                                Maintain.flowMaintain();

                            }
                        }


                        if (!currentNode.equals(desNode)) {

                            // add item of the current node to stack
                            for (Edge<E> edge : currentNode.Edges) {

                                // get the edge that has the destination satisfied with these condition.
                                if ((currentNode.level < edge.destination.level) && edge.Capability - edge.weight != 0 && !visitedNodeInPath.contains(edge.destination)) {

                                        toVisitNode.push(edge.destination);
                                        toVisitEdges.push(edge);

                                } else if (edge.Capability - edge.weight == 0) {
                                    // if we found an edge that has full capability
                                    Maintain.resetEdgeStacks();
                                    Maintain.resetVisited();
                                }
                            }
                            visitedNodeInPath.push(currentNode);
                        }
                    }
                }
                System.out.printf("\n Total Flow : %d \n\n", total);
                System.out.println("" + PathCollection);
                return PathCollection;
            }
        }
        return null;
    }





    private void popEdges(Stack<Edge<E>> EdgeCollection, E currentPrevious) {

        // pop item in stack until meet the condition
        while (!EdgeCollection.isEmpty()) {
            // delete until found out an edge that has the destination reach the condition.
            if (EdgeCollection.peek().destination.item.equals(currentPrevious)) {
                break;
            } else {
                EdgeCollection.pop();
            }
        }
    }




    private void popNode(Stack<Node<E>> stackNode, Node<E> condition, Node<E> desNode) {
        // pop out the item until the condition wrong
        // pop out the item until the condition wrong
        while (!stackNode.isEmpty() && (stackNode.peek().level > condition.level) && !stackNode.peek().equals(desNode)) {
            stackNode.pop();
        }
    }




    private void popNodeVisited(Stack<Node<E>> stackNode, Node<E> condition) {
        // pop out the item until the condition wrong
        while (!stackNode.isEmpty()) {
            // delete until found out an edge that has the destination reach the condition.
            if (stackNode.peek().equals(condition)) {
                break;
            } else {
                stackNode.pop();
            }
        }
    }



    private List<String> getPathCollection(E StartName, Stack<Edge<E>> edges, int flow) {
        StringBuilder Path = new StringBuilder("" + StartName + "--->\t");
        List<String> result = new LinkedList<>();

        result.add("flow : " + flow);
        result.add((String) StartName);

        for (Edge<E> edge : edges) {
            result.add((String) edge.destination.item);
            Path.append(edge.destination.item).append("--->\t");
        }
        System.out.println(Path.append(flow));
        return result;
    }



    private Edge<E> UpdateFlowAndCheckCap(Stack<Edge<E>> PathCollection, int newWeight) {
        Edge<E> fullEdge = null;
        int i = 0;
        for (Edge<E> a : PathCollection) {
            a.weight = Math.min(a.weight + newWeight, a.Capability);
            if (a.Capability - a.weight == 0 && i < 1) {
                i++;
                fullEdge = a;
            }
        }
        return fullEdge;
    }

    public static void main(String[] args) {
        MyOwnMaxFlowAlgorithm<String> NetworkMaxFlowAlgorithm = new MyOwnMaxFlowAlgorithm<>();


        NetworkMaxFlowAlgorithm.insert("V0", "V1", 7);
        NetworkMaxFlowAlgorithm.insert("V0", "V2", 2);
        NetworkMaxFlowAlgorithm.insert("V0", "V3", 1);
        NetworkMaxFlowAlgorithm.insert("V0", "V20", 0);
        NetworkMaxFlowAlgorithm.insert("V0", "V21", 9);


        NetworkMaxFlowAlgorithm.insert("V1", "V0", 7);
        NetworkMaxFlowAlgorithm.insert("V2", "V0", 2);
        NetworkMaxFlowAlgorithm.insert("V3", "V0", 1);

        NetworkMaxFlowAlgorithm.insert("V1", "V4", 2);
        NetworkMaxFlowAlgorithm.insert("V1", "V5", 4);

        NetworkMaxFlowAlgorithm.insert("V4", "V1", 2);
        NetworkMaxFlowAlgorithm.insert("V5", "V1", 4);

        NetworkMaxFlowAlgorithm.insert("V2", "V5", 5);
        NetworkMaxFlowAlgorithm.insert("V2", "V6", 6);
//
        NetworkMaxFlowAlgorithm.insert("V5", "V2", 5);
        NetworkMaxFlowAlgorithm.insert("V6", "V2", 6);

        NetworkMaxFlowAlgorithm.insert("V3", "V4", 4);
        NetworkMaxFlowAlgorithm.insert("V3", "V8", 8);
        NetworkMaxFlowAlgorithm.insert("V3", "V11", 8);
//
        NetworkMaxFlowAlgorithm.insert("V4", "V3", 4);
        NetworkMaxFlowAlgorithm.insert("V8", "V3", 8);
        NetworkMaxFlowAlgorithm.insert("V11", "V3", 8);


        NetworkMaxFlowAlgorithm.insert("V11", "V12", 8);
        NetworkMaxFlowAlgorithm.insert("V12", "V13", 8);
        NetworkMaxFlowAlgorithm.insert("V13", "V14", 8);

        NetworkMaxFlowAlgorithm.insert("V4", "V7", 7);
        NetworkMaxFlowAlgorithm.insert("V4", "V8", 6);

        NetworkMaxFlowAlgorithm.insert("V7", "V4", 7);
        NetworkMaxFlowAlgorithm.insert("V8", "V4", 1);

        NetworkMaxFlowAlgorithm.insert("V5", "V7", 3);
        NetworkMaxFlowAlgorithm.insert("V5", "V9", 3);
        NetworkMaxFlowAlgorithm.insert("V5", "V6", 8);

        NetworkMaxFlowAlgorithm.insert("V7", "V5", 3);
        NetworkMaxFlowAlgorithm.insert("V9", "V5", 3);
        NetworkMaxFlowAlgorithm.insert("V6", "V5", 8);

        NetworkMaxFlowAlgorithm.insert("V6", "V9", 3);
        NetworkMaxFlowAlgorithm.insert("V9", "V6", 3);
//
        NetworkMaxFlowAlgorithm.insert("V7", "V10", 1);
        NetworkMaxFlowAlgorithm.insert("V10", "V7", 1);


        NetworkMaxFlowAlgorithm.insert("V8", "V10", 6);
        NetworkMaxFlowAlgorithm.insert("V9", "V10", 4);
//
        NetworkMaxFlowAlgorithm.insert("V10", "V8", 3);
        NetworkMaxFlowAlgorithm.insert("V10", "V9", 4);
        NetworkMaxFlowAlgorithm.constructLevel("V1", "V9");
//        for (Node<String> node : NetworkMaxFlowAlgorithm.GraphNodes.values()){
//            System.out.println(node.item+ "---" + node.level);
//        }

        NetworkMaxFlowAlgorithm.getOptimumPathFlow("V4", "V10");


//
//

        MyOwnMaxFlowAlgorithm<String> MaxFlowAlgorithmUpgrade = new MyOwnMaxFlowAlgorithm<>();
        float start1 = System.currentTimeMillis();
        MaxFlowAlgorithmUpgrade.insert("V0", "V1", 5);
        MaxFlowAlgorithmUpgrade.insert("V0", "V2", 10);
        MaxFlowAlgorithmUpgrade.insert("V0", "V3", 5);

        MaxFlowAlgorithmUpgrade.insert("V1", "V0", 5);
        MaxFlowAlgorithmUpgrade.insert("V2", "V0", 10);
        MaxFlowAlgorithmUpgrade.insert("V3", "V0", 5);

        MaxFlowAlgorithmUpgrade.insert("V1", "V4", 10);
        MaxFlowAlgorithmUpgrade.insert("V4", "V1", 10);


        MaxFlowAlgorithmUpgrade.insert("V2", "V1", 15);
        MaxFlowAlgorithmUpgrade.insert("V2", "V5", 20);

        MaxFlowAlgorithmUpgrade.insert("V1", "V2", 15);
        MaxFlowAlgorithmUpgrade.insert("V5", "V2", 20);

        MaxFlowAlgorithmUpgrade.insert("V3", "V6", 10);

        MaxFlowAlgorithmUpgrade.insert("V6", "V3", 10);

        MaxFlowAlgorithmUpgrade.insert("V4", "V7", 10);
        MaxFlowAlgorithmUpgrade.insert("V4", "V5", 25);

        MaxFlowAlgorithmUpgrade.insert("V7", "V4", 10);
        MaxFlowAlgorithmUpgrade.insert("V5", "V4", 25);

        MaxFlowAlgorithmUpgrade.insert("V5", "V8", 30);
        MaxFlowAlgorithmUpgrade.insert("V5", "V3", 5);

        MaxFlowAlgorithmUpgrade.insert("V8", "V5", 30);
        MaxFlowAlgorithmUpgrade.insert("V3", "V5", 5);

        MaxFlowAlgorithmUpgrade.insert("V6", "V8", 5);
        MaxFlowAlgorithmUpgrade.insert("V6", "V9", 10);

        MaxFlowAlgorithmUpgrade.insert("V8", "V6", 5);
        MaxFlowAlgorithmUpgrade.insert("V9", "V6", 10);

        MaxFlowAlgorithmUpgrade.insert("V7", "V10", 5);
        MaxFlowAlgorithmUpgrade.insert("V10", "V7", 5);


        MaxFlowAlgorithmUpgrade.insert("V8", "V10", 15);
        MaxFlowAlgorithmUpgrade.insert("V8", "V4", 15);
        MaxFlowAlgorithmUpgrade.insert("V8", "V9", 5);

        MaxFlowAlgorithmUpgrade.insert("V10", "V8", 15);
        MaxFlowAlgorithmUpgrade.insert("V4", "V8", 15);
        MaxFlowAlgorithmUpgrade.insert("V9", "V8", 5);

        MaxFlowAlgorithmUpgrade.insert("V9", "V10", 10);
        MaxFlowAlgorithmUpgrade.insert("V10", "V9", 10);


        MaxFlowAlgorithmUpgrade.getOptimumPathFlow("V0", "V10");
        MaxFlowAlgorithmUpgrade.getOptimumPathFlow("V10", "V0");




    }


}