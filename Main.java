import jade.Boot;
import jade.wrapper.StaleProxyException;

public class Main extends Boot {
    public static void main(String[] args) {
        RobotAgent robotAgent = new RobotAgent();
        try {
            robotAgent.startAgents();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }


    }
}