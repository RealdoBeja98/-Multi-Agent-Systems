import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import java.util.*;

/**
 * TransportAgent - Represents an autonomous transport robot
 *
 * Implements BDI-inspired architecture:
 * - Beliefs: current location, battery level, cargo status
 * - Desires: complete assigned tasks, maintain battery
 * - Intentions: execute transport plan, avoid obstacles
 */
public class TransportAgent extends Agent {

    // Beliefs (agent state)
    private int locationX;
    private int locationY;
    private int batteryLevel = 100; // Percentage
    private boolean hasCargo = false;
    private String currentTask = null;

    // Constants
    private static final int BATTERY_THRESHOLD = 20;
    private static final int GRID_SIZE = 20;

    protected void setup() {
        // Initialize random starting position
        Object[] args = getArguments();
        if (args != null && args.length >= 2) {
            locationX = Integer.parseInt(args[0].toString());
            locationY = Integer.parseInt(args[1].toString());
        } else {
            Random rand = new Random();
            locationX = rand.nextInt(GRID_SIZE);
            locationY = rand.nextInt(GRID_SIZE);
        }

        System.out.println("TransportAgent " + getLocalName() +
                " initialized at (" + locationX + "," + locationY + ")");

        // Register with Directory Facilitator
        registerService();

        // Add behavior to listen for CFPs
        addBehaviour(new RespondToCFPBehaviour());

        // Add behavior to execute assigned tasks
        addBehaviour(new ExecuteTaskBehaviour());

        // Add behavior to simulate battery consumption
        addBehaviour(new TickerBehaviour(this, 5000) {
            protected void onTick() {
                if (currentTask != null && batteryLevel > 0) {
                    batteryLevel -= 2;
                    if (batteryLevel < BATTERY_THRESHOLD) {
                        System.out.println(getLocalName() + " battery low: " + batteryLevel + "%");
                    }
                }
            }
        });
    }

    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("transport-service");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    /**
     * Behavior to respond to Call For Proposals
     */
    private class RespondToCFPBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = receive(mt);

            if (msg != null) {
                if (currentTask == null && batteryLevel > BATTERY_THRESHOLD) {
                    // Parse CFP: taskId:TRANSPORT:itemType:pickupX,pickupY:deliveryX,deliveryY:urgency
                    String content = msg.getContent();
                    String[] parts = content.split(":");

                    if (parts.length >= 5) {
                        String taskId = parts[0];
                        // parts[1] = TRANSPORT, parts[2] = WOOD_PLANKS
                        String[] pickup = parts[3].split(",");
                        int pickupX = Integer.parseInt(pickup[0]);
                        int pickupY = Integer.parseInt(pickup[1]);

                        // Calculate cost based on distance and battery
                        double cost = calculateCost(pickupX, pickupY);

                        // Send proposal
                        ACLMessage proposal = msg.createReply();
                        proposal.setPerformative(ACLMessage.PROPOSE);
                        proposal.setContent("COST:" + cost);
                        send(proposal);

                        System.out.println(getLocalName() + " proposed cost " + cost +
                                " for " + taskId);
                    }
                } else {
                    // Refuse if busy or low battery
                    ACLMessage refuse = msg.createReply();
                    refuse.setPerformative(ACLMessage.REFUSE);
                    refuse.setContent("BUSY or LOW_BATTERY");
                    send(refuse);
                }
            } else {
                block();
            }
        }
    }

    private double calculateCost(int targetX, int targetY) {
        // Cost factors:
        // - Distance (50% weight)
        // - Battery level (30% weight) - lower battery = higher cost
        // - Current load (20% weight)

        int distance = Math.abs(locationX - targetX) + Math.abs(locationY - targetY);
        double distanceCost = distance * 2.0;
        double batteryCost = (100 - batteryLevel) * 0.3;
        double loadCost = hasCargo ? 5.0 : 0.0;

        return distanceCost + batteryCost + loadCost;
    }

    /**
     * Behavior to execute assigned transport tasks
     */
    private class ExecuteTaskBehaviour extends CyclicBehaviour {
        public void action() {
            // Listen for task assignments
            MessageTemplate mt = MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                    MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)
            );
            ACLMessage msg = receive(mt);

            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    // Task assigned!
                    String content = msg.getContent();
                    String[] parts = content.split(":");
                    currentTask = parts[0];

                    System.out.println(getLocalName() + " executing " + currentTask);

                    // Parse coordinates: TASK-1:TRANSPORT:WOOD_PLANKS:x,y:x,y:urgency
                    String[] pickup = parts[3].split(",");
                    String[] delivery = parts[4].split(",");
                    int pickupX = Integer.parseInt(pickup[0]);
                    int pickupY = Integer.parseInt(pickup[1]);
                    int deliveryX = Integer.parseInt(delivery[0]);
                    int deliveryY = Integer.parseInt(delivery[1]);

                    // Execute transport (simplified simulation)
                    addBehaviour(new OneShotBehaviour() {
                        public void action() {
                            simulateTransport(pickupX, pickupY, deliveryX, deliveryY);
                        }
                    });
                } else {
                    // Proposal rejected - remain available
                    System.out.println(getLocalName() + " proposal rejected");
                }
            } else {
                block();
            }
        }
    }

    private void simulateTransport(int pickupX, int pickupY, int deliveryX, int deliveryY) {
        try {
            // Navigate to pickup
            System.out.println(getLocalName() + " navigating to pickup (" +
                    pickupX + "," + pickupY + ")");
            navigateTo(pickupX, pickupY);
            Thread.sleep(1000);

            // Load cargo
            System.out.println(getLocalName() + " loading cargo");
            hasCargo = true;
            Thread.sleep(500);

            // Navigate to delivery
            System.out.println(getLocalName() + " navigating to delivery (" +
                    deliveryX + "," + deliveryY + ")");
            navigateTo(deliveryX, deliveryY);
            Thread.sleep(1000);

            // Unload cargo
            System.out.println(getLocalName() + " unloading cargo");
            hasCargo = false;
            Thread.sleep(500);

            // Task complete
            System.out.println(getLocalName() + " completed " + currentTask);
            currentTask = null;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void navigateTo(int targetX, int targetY) {
        // Simple simulation of A* pathfinding
        // In reality, this would implement actual pathfinding with obstacle avoidance
        int steps = Math.abs(locationX - targetX) + Math.abs(locationY - targetY);

        System.out.println(getLocalName() + " moving from (" + locationX + "," +
                locationY + ") to (" + targetX + "," + targetY +
                ") - " + steps + " steps");

        // Simulate gradual movement
        while (locationX != targetX || locationY != targetY) {
            if (locationX < targetX) locationX++;
            else if (locationX > targetX) locationX--;

            if (locationY < targetY) locationY++;
            else if (locationY > targetY) locationY--;

            try {
                Thread.sleep(200); // Simulate movement time
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println("TransportAgent " + getLocalName() + " terminating.");
    }
}

