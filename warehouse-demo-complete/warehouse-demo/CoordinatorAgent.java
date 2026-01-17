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
 * CoordinatorAgent - Implements contract net protocol for task allocation
 * in the BeaverWoodHome warehouse automation system.
 * 
 * Responsibilities:
 * - Receive transport requests from ProductionStationAgents
 * - Broadcast call-for-proposals to available TransportAgents
 * - Evaluate bids and assign tasks
 * - Track task completion
 */
public class CoordinatorAgent extends Agent {
    
    private List<AID> transportAgents = new ArrayList<>();
    private Map<String, TaskRequest> activeTasks = new HashMap<>();
    private int taskCounter = 0;
    
    protected void setup() {
        System.out.println("CoordinatorAgent " + getLocalName() + " is ready.");
        
        // Register with Directory Facilitator
        registerService();
        
        // Add behavior to handle transport requests from production stations
        addBehaviour(new ReceiveRequestsBehaviour());
        
        // Add behavior to handle proposals from transport agents
        addBehaviour(new EvaluateProposalsBehaviour());
        
        // Periodic behavior to discover available transport agents
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                discoverTransportAgents();
            }
        });
    }
    
    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("task-coordinator");
        sd.setName("warehouse-coordinator");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
    
    private void discoverTransportAgents() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("transport-service");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            transportAgents.clear();
            for (DFAgentDescription agent : result) {
                transportAgents.add(agent.getName());
            }
            System.out.println("Discovered " + transportAgents.size() + " transport agents");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Behavior to receive transport requests from production stations
     */
    private class ReceiveRequestsBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = receive(mt);
            
            if (msg != null) {
                // Parse request: format is "TRANSPORT:itemType:pickupX,pickupY:deliveryX,deliveryY:urgency"
                String content = msg.getContent();
                System.out.println("Received transport request: " + content);
                
                String taskId = "TASK-" + (++taskCounter);
                TaskRequest task = new TaskRequest(taskId, content, msg.getSender());
                activeTasks.put(taskId, task);
                
                // Broadcast call for proposals to transport agents
                broadcastCFP(task);
                
            } else {
                block();
            }
        }
    }
    
    private void broadcastCFP(TaskRequest task) {
        if (transportAgents.isEmpty()) {
            System.out.println("No transport agents available!");
            return;
        }
        
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        for (AID agent : transportAgents) {
            cfp.addReceiver(agent);
        }
        cfp.setContent(task.taskId + ":" + task.details);
        cfp.setConversationId(task.taskId);
        cfp.setReplyWith(task.taskId + System.currentTimeMillis());
        send(cfp);
        
        System.out.println("Broadcasted CFP for " + task.taskId + " to " + transportAgents.size() + " agents");
    }
    
    /**
     * Behavior to evaluate proposals and select winner
     */
    private class EvaluateProposalsBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage msg = receive(mt);
            
            if (msg != null) {
                String conversationId = msg.getConversationId();
                TaskRequest task = activeTasks.get(conversationId);
                
                if (task != null && !task.assigned) {
                    // Parse proposal: format is "COST:value"
                    String content = msg.getContent();
                    double cost = Double.parseDouble(content.split(":")[1]);
                    
                    task.addProposal(msg.getSender(), cost);
                    System.out.println("Received proposal from " + msg.getSender().getLocalName() + 
                                     " with cost: " + cost);
                    
                    // Simple strategy: assign to first reasonable proposal
                    // In production, would wait for multiple proposals and compare
                    if (cost < 100) { // Basic threshold
                        assignTask(task, msg.getSender());
                    }
                }
            } else {
                block();
            }
        }
    }
    
    private void assignTask(TaskRequest task, AID winner) {
        task.assigned = true;
        task.assignee = winner;
        
        // Send ACCEPT_PROPOSAL to winner
        ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        accept.addReceiver(winner);
        accept.setContent(task.taskId + ":" + task.details);
        accept.setConversationId(task.taskId);
        send(accept);
        
        // Send REJECT_PROPOSAL to others
        for (AID agent : task.proposals.keySet()) {
            if (!agent.equals(winner)) {
                ACLMessage reject = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                reject.addReceiver(agent);
                reject.setConversationId(task.taskId);
                send(reject);
            }
        }
        
        System.out.println("Assigned " + task.taskId + " to " + winner.getLocalName());
    }
    
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println("CoordinatorAgent " + getLocalName() + " terminating.");
    }
    
    /**
     * Inner class to represent a transport task request
     */
    private class TaskRequest {
        String taskId;
        String details;
        AID requester;
        Map<AID, Double> proposals = new HashMap<>();
        boolean assigned = false;
        AID assignee;
        
        TaskRequest(String taskId, String details, AID requester) {
            this.taskId = taskId;
            this.details = details;
            this.requester = requester;
        }
        
        void addProposal(AID agent, double cost) {
            proposals.put(agent, cost);
        }
    }
}
