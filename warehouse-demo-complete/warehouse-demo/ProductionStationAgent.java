import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import java.util.Random;

/**
 * ProductionStationAgent - Represents a manufacturing station
 *
 * Responsibilities:
 * - Monitor inventory levels
 * - Generate material requests when stock is low
 * - Simulate production activities
 */
public class ProductionStationAgent extends Agent {

    private String stationType;
    private int inventoryLevel = 10;
    private static final int REORDER_THRESHOLD = 3;
    private AID coordinator;
    private boolean requestPending = false;

    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            stationType = args[0].toString();
        } else {
            stationType = "GENERIC";
        }

        System.out.println("ProductionStationAgent " + getLocalName() +
                " (" + stationType + ") is ready.");

        // Find coordinator
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                findCoordinator();
            }
        });

        // Simulate production consumption
        addBehaviour(new TickerBehaviour(this, 8000) {
            protected void onTick() {
                simulateProduction();
            }
        });

        // Monitor inventory
        addBehaviour(new TickerBehaviour(this, 5000) {
            protected void onTick() {
                checkInventory();
            }
        });
    }

    private void findCoordinator() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("task-coordinator");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                coordinator = result[0].getName();
                System.out.println(getLocalName() + " found coordinator: " +
                        coordinator.getLocalName());
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void simulateProduction() {
        if (inventoryLevel > 0) {
            inventoryLevel--;
            System.out.println(getLocalName() + " produced item. Inventory: " + inventoryLevel);
        } else {
            System.out.println(getLocalName() + " cannot produce - no materials!");
        }
    }

    private void checkInventory() {
        if (inventoryLevel <= REORDER_THRESHOLD && coordinator != null && !requestPending) {
            requestMaterials();
        }
    }

    private void requestMaterials() {
        // Generate transport request
        // Format: TRANSPORT:itemType:pickupX,pickupY:deliveryX,deliveryY:urgency
        Random rand = new Random();
        int pickupX = rand.nextInt(20); // Warehouse location
        int pickupY = rand.nextInt(20);
        int deliveryX = 10; // This station's location (simplified)
        int deliveryY = 10;
        int urgency = (REORDER_THRESHOLD - inventoryLevel) * 10; // Higher urgency when lower stock

        String request = "TRANSPORT:WOOD_PLANKS:" + pickupX + "," + pickupY + ":" +
                deliveryX + "," + deliveryY + ":" + urgency;

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(coordinator);
        msg.setContent(request);
        msg.setConversationId("material-request-" + System.currentTimeMillis());
        send(msg);

        System.out.println(getLocalName() + " requested materials (urgency: " + urgency + ")");

        // Mark request as pending to avoid spam
        requestPending = true;

        // Add behavior to reset pending after delivery (simulated)
        addBehaviour(new WakerBehaviour(this, 15000) {
            protected void onWake() {
                inventoryLevel += 5;
                requestPending = false;
                System.out.println(getLocalName() + " received materials. Inventory: " + inventoryLevel);
            }
        });
    }

    protected void takeDown() {
        System.out.println("ProductionStationAgent " + getLocalName() + " terminating.");
    }
}






