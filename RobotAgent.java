import jade.core.AID;
import jade.core.Agent;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class RobotAgent extends Agent {

    private static boolean IAmTheCreator = true;
    // number of answer messages received.
    private int numberOfGoals = 0;

    public final static String GOAL = "GOAL";
    public final static String ANSWER = "ANSWER";
    public final static String THANKS = "THANKS";
    String t1AgentName;
    private AgentController t1 = null;
    private AID initiator = null;
    private  Judge judge;

    public void startAgents() throws StaleProxyException {
       setup();
        }



    protected void setup() {
        System.out.println(getLocalName()+" STARTED");
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            initiator = new AID((String) args[0], AID.ISLOCALNAME);
        }

        try {
            // create the agent descrption of itself
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            // register the description with the DF
            DFService.register(this, dfd);
            System.out.println(getLocalName()+" REGISTERED WITH THE DF");
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        if (IAmTheCreator) {
            IAmTheCreator = false;  // next agent in this JVM will not be a creator

            // create Judge Agent
            String t1AgentName = getLocalName()+" Judge";

            try {
                // create Judge Agent on the same container of the creator agent
                AgentContainer container = (AgentContainer)getContainerController(); // get a container controller for creating new agents
                t1 = container.createNewAgent(t1AgentName, "RobotAgent", null);
                t1.start();
                System.out.println(getLocalName()+" CREATED AND STARTED NEW ROBOTAGENT:"+t1AgentName + " ON CONTAINER "+container.getContainerName());
            } catch (Exception any) {
                any.printStackTrace();
            }

            // send  a GOAL message

        }

        // add a Behaviour that listen if a greeting message arrives
        // and sends back an ANSWER.
        // if an ANSWER to a greetings message is arrived
        // then send a THANKS message
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {

                 if (judge.checkGoal()){
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setContent(GOAL);

                    msg.addReceiver(new AID(t1AgentName, AID.ISLOCALNAME));

                    send(msg);
                    System.out.println(getLocalName()+" SENT GOAL MESSAGE  TO "+t1AgentName);
                }

                // listen if a greetings message arrives
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                if (msg != null) {
                    if (GOAL.equalsIgnoreCase(msg.getContent())) {
                        // if a greetings message is arrived then send an ANSWER
                        System.out.println(myAgent.getLocalName()+" RECEIVED GOAL MESSAGE FROM "+msg.getSender().getLocalName());
                        ACLMessage reply = msg.createReply();
                        reply.setContent(ANSWER);
                        myAgent.send(reply);
                        numberOfGoals++;
                        System.out.println("NUMBER OF GOALS:" + numberOfGoals);
                    }
                    else if (ANSWER.equalsIgnoreCase(msg.getContent())) {
                        // if an ANSWER to a greetings message is arrived
                        // then send a THANKS message
                        System.out.println(myAgent.getLocalName()+" RECEIVED ANSWER MESSAGE FROM "+msg.getSender().getLocalName());
                        ACLMessage replyT = msg.createReply();
                        replyT.setContent(THANKS);
                        myAgent.send(replyT);
                        System.out.println(myAgent.getLocalName()+" SENT THANKS MESSAGE");

                    }
                    else if (THANKS.equalsIgnoreCase(msg.getContent())) {
                        System.out.println(myAgent.getLocalName()+" RECEIVED THANKS MESSAGE FROM "+msg.getSender().getLocalName());
                    }
                    else {
                        System.out.println(myAgent.getLocalName()+" Unexpected message received from "+msg.getSender().getLocalName());
                    }
                }
                else {
                    // if no message is arrived, block the behaviour
                    block();
                }
            }
        });
    }

    protected void takeDown() {
        // Deregister with the DF
        try {
            DFService.deregister(this);
            System.out.println(getLocalName()+" DEREGISTERED WITH THE DF");
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
