import java.util.*;

public class main {

    private static final String RESTAURANT = "R";

    // Weighted, undirected road graph
    private static final Map<String, Map<String, Integer>> graph = new HashMap<>();

    static class NodeDistance {
        String node;
        int distance;

        NodeDistance(String node, int distance) {
            this.node = node;
            this.distance = distance;
        }
    }

    static void addRoad(String from, String to, int distance) {
        graph.get(from).put(to, distance);
        graph.get(to).put(from, distance);
    }

    static void buildGraph() {
        String[] locations = {"R", "A", "B", "C"};
        for (String location : locations) {
            graph.put(location, new HashMap<>());
        }

        // Roads given in the project description
        addRoad("R", "A", 2);
        addRoad("A", "B", 3);
        addRoad("B", "C", 2);
        addRoad("R", "C", 4);
    }

    // Dijkstra's algorithm using a min-heap (PriorityQueue)
    static Map<String, Integer> dijkstra(String source) {
        Map<String, Integer> distance = new HashMap<>();

        for (String location : graph.keySet()) {
            distance.put(location, Integer.MAX_VALUE);
        }
        distance.put(source, 0);

        PriorityQueue<NodeDistance> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));

        minHeap.offer(new NodeDistance(source, 0));

        while (!minHeap.isEmpty()) {
            NodeDistance current = minHeap.poll();

            if (current.distance != distance.get(current.node)) {
                continue;
            }

            for (Map.Entry<String, Integer> road :
                    graph.get(current.node).entrySet()) {

                String neighbour = road.getKey();
                int newDistance = current.distance + road.getValue();

                if (newDistance < distance.get(neighbour)) {
                    distance.put(neighbour, newDistance);
                    minHeap.offer(new NodeDistance(neighbour, newDistance));
                }
            }
        }

        return distance;
    }

    static void addOrder(
            String orderId,
            String location,
            Map<String, List<String>> pendingOrders,
            Set<String> orderIds) {

        orderId = orderId.trim().toUpperCase();
        location = location.trim().toUpperCase();

        if (orderId.isEmpty()) {
            System.out.println("Invalid order ID. Order not added.");
            return;
        }

        if (orderIds.contains(orderId)) {
            System.out.println("Duplicate order ID " + orderId
                    + ". Order not added.");
            return;
        }

        if (!graph.containsKey(location)) {
            System.out.println("Location " + location
                    + " not found. Order " + orderId + " not added.");
            return;
        }

        orderIds.add(orderId);

        // HashMap groups all orders having the same delivery location
        pendingOrders.putIfAbsent(location, new ArrayList<>());
        pendingOrders.get(location).add(orderId);
    }

    static void planRoute(Map<String, List<String>> pendingOrders) {
        String currentLocation = RESTAURANT;
        int totalDistance = 0;

        List<String> route = new ArrayList<>();
        route.add(RESTAURANT);

        System.out.println("\n--- Delivery Plan ---");

        while (!pendingOrders.isEmpty()) {
            Map<String, Integer> distances = dijkstra(currentLocation);

            String nearestLocation = null;
            int nearestDistance = Integer.MAX_VALUE;

            // Select the nearest pending location.
            // If distances are equal, select alphabetically.
            for (String location : pendingOrders.keySet()) {
                int distance = distances.get(location);

                if (distance < nearestDistance ||
                    (distance == nearestDistance &&
                     (nearestLocation == null ||
                      location.compareTo(nearestLocation) < 0))) {

                    nearestDistance = distance;
                    nearestLocation = location;
                }
            }

            totalDistance += nearestDistance;
            currentLocation = nearestLocation;
            route.add(currentLocation);

            List<String> deliveredOrders =
                    pendingOrders.remove(nearestLocation);

            System.out.println("Reached " + nearestLocation
                    + " | Delivered orders: "
                    + String.join(", ", deliveredOrders));
        }

        // Return to the restaurant using the shortest road distance
        Map<String, Integer> returnDistances = dijkstra(currentLocation);
        int returnDistance = returnDistances.get(RESTAURANT);

        totalDistance += returnDistance;
        route.add(RESTAURANT);

        System.out.println("\nRoute: " + String.join(" -> ", route));
        System.out.println("Total distance: " + totalDistance + " km");
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        buildGraph();

        Map<String, List<String>> pendingOrders = new HashMap<>();
        Set<String> orderIds = new HashSet<>();

        System.out.println("===== FOOD DELIVERY ROUTE OPTIMIZER =====");
        System.out.println("Restaurant location: R");
        System.out.println("Available locations: R, A, B, C");
        System.out.println("Roads: R-A (2 km), A-B (3 km), "
                + "B-C (2 km), R-C (4 km)");

        System.out.print("\nEnter number of orders: ");

        if (!sc.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            sc.close();
            return;
        }

        int numberOfOrders = sc.nextInt();

        if (numberOfOrders < 0) {
            System.out.println("Number of orders cannot be negative.");
            sc.close();
            return;
        }

        for (int i = 1; i <= numberOfOrders; i++) {
            System.out.print("Enter Order ID and delivery location "
                    + "(example: O1 A): ");

            String orderId = sc.next();
            String location = sc.next();

            addOrder(orderId, location, pendingOrders, orderIds);
        }

        if (pendingOrders.isEmpty()) {
            System.out.println("\nNo valid orders to deliver.");
            System.out.println("Route: R -> R");
            System.out.println("Total distance: 0 km");
        } else {
            planRoute(pendingOrders);
        }

        sc.close();
    }
}
