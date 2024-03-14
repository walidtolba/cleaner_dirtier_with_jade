import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Random;

public class CleanerAgent extends Agent {
    Random random = new Random();
    public int x;
    public int y;
    public boolean last;
    protected void setup() {
        x = random.nextInt(10);
        y = random.nextInt(10);
        last = false;
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String content = msg.getContent();
                    if (content != null) {
                        String[] coordinates = content.split(",");
                        int i = Integer.parseInt(coordinates[0]);
                        int j = Integer.parseInt(coordinates[1]);
                        if (msg.getPerformative() == ACLMessage.CFP && Board.cells[i][j]) {
                            System.out.println("Received CFP from dirtier");

                            // Send a PROPOSE message to the DirtierAgent
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.PROPOSE);
                            reply.setContent(x + "," + y + "," + last);
                            send(reply);
                            System.out.println("Sent PROPOSE to dirtier");

                            // Wait for an ACCEPT_PROPOSAL message from the DirtierAgent
                            msg = blockingReceive(
                                    MessageTemplate.or(
                                            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                                            MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)));
                            if (msg != null && msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                                System.out.println("Received ACCEPT_PROPOSAL from dirtier");

                                // Clean the cell
                                Board.cells[i][j] = false;
                                x = i;
                                y = j;
                                last = true;
                                System.out.println("Cleaned cell " + i + ", " + j);
                            }
                            else
                                last = false;
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }
}