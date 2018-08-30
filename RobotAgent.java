import jade.core.AID;
import jade.core.Agent;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;


public class RobotAgent extends Agent {

    private static boolean AgentsCreated = true;
    // number of goal messages received.
    private int numberOfGoalsTeamOne = 0;
    private int numberOfGoalsTeamTwo = 0;

    public String agentName = new String();
    public final static String GOAL = "GOAL";
    public final static String FIRST_TEAM = "0";
    public final static String SECOND_TEAM = "0";
    String t1AgentName;
    private AgentController t1 = null;
    private AID initiator = null;

    public void startAgents(Judge judge) throws StaleProxyException {
        setup();
        // send  a GOAL message
        if (judge != null) {
            if (judge.checkGoal() == 1) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent(FIRST_TEAM);
                msg.addReceiver(new AID(t1AgentName, AID.ISLOCALNAME));
                send(msg);
                System.out.println(getLocalName() + " SENT GOAL MESSAGE OF FIRST TEAM  TO " + t1AgentName);
            } else if (judge.checkGoal() == 2) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent(SECOND_TEAM);
                msg.addReceiver(new AID(t1AgentName, AID.ISLOCALNAME));
                send(msg);
                System.out.println(getLocalName() + " SENT GOAL MESSAGE OF SECOND TEAM  TO " + t1AgentName);

            }

        }
    }


    protected void setup() {
        // create Judge Agent
        if (AgentsCreated) {
            AgentsCreated = false;
        String t1AgentName = "Agent Judge";
        String t2AgentName = "Agent Counter";
        Runtime runtime = Runtime.instance();
        try {
            // create Judge Agent on the same container of the creator agent
            ProfileImpl profileImpl = new ProfileImpl(false);
            profileImpl.setParameter(ProfileImpl.MAIN_HOST, "localhost");
            AgentContainer container = runtime.createAgentContainer(profileImpl); // get a container controller for creating new agents
            t1 = container.createNewAgent(t1AgentName, "RobotAgent", null);
            t1 = container.createNewAgent(t2AgentName, "RobotAgent", null);
            t1.start();
            System.out.println("CREATED AND STARTED NEW ROBOTAGENT:" + t1AgentName + " ON CONTAINER " + container.getContainerName());
            System.out.println("CREATED AND STARTED NEW ROBOTAGENT:" + t2AgentName + " ON CONTAINER " + container.getContainerName());
        } catch (Exception any) {
            any.printStackTrace();
        }


        }


        // if an COUNT to a goals message is arrived
        // then count Goals
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {

                // listen if a goal message arrives
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                if (msg != null) {
                    if (FIRST_TEAM.equalsIgnoreCase(msg.getContent())) {
                        // if  message is arrived then send an GOAL
                        System.out.println(myAgent.getLocalName() + " RECEIVED GOAL OF FIRST TEAM MESSAGE FROM " + msg.getSender().getLocalName());
                        ACLMessage reply = msg.createReply();
                        reply.setContent(GOAL);
                        myAgent.send(reply);
                        numberOfGoalsTeamOne++;
                        System.out.println("NUMBER OF GOALS TEAM ONE:" + numberOfGoalsTeamOne);
                    } else if (SECOND_TEAM.equalsIgnoreCase(msg.getContent())) {
                        System.out.println(myAgent.getLocalName() + " RECEIVED GOAL OF FIRST TEAM MESSAGE FROM " + msg.getSender().getLocalName());
                        ACLMessage reply = msg.createReply();
                        reply.setContent(GOAL);
                        myAgent.send(reply);
                        numberOfGoalsTeamTwo++;
                        System.out.println("NUMBER OF GOALS TEAM TWO:" + numberOfGoalsTeamTwo);
                    } else {
                        System.out.println(myAgent.getLocalName() + " Unexpected message received from " + msg.getSender().getLocalName());
                    }
                } else {
                    // if no message is arrived, block the behaviour
                    block();
                }
                if (numberOfGoalsTeamTwo == numberOfGoalsTeamOne + 3) {
                    System.out.println("TEAM TWO IS AHEAD!!!");
                } else if (numberOfGoalsTeamOne == numberOfGoalsTeamTwo + 3) {
                    System.out.println("TEAM ONE IS AHEAD!!!");
                }
            }
        });
    }

    protected void takeDown() {
        // Deregister with the DF
        try {
            DFService.deregister(this);
            System.out.println(getLocalName() + " DEREGISTERED WITH THE DF");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void beforeMove() {
        System.out.println("Przed przejsciem");
    }

    @Override
    protected void afterMove() {
        System.out.println("Po przejsciu");
    }

}
