import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Random;
import jade.lang.acl.MessageTemplate;

public class DirtierAgent extends Agent {
    Random random = new Random();

    public static int distance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    protected void setup() {
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                int i = random.nextInt(10);
                int j = random.nextInt(10);
                if (!Board.cells[i][j]) {
                    Board.cells[i][j] = true;
                    System.out.println("Dirtied cell " + i + ", " + j);

                    ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                    ACLMessage[] msgs = new ACLMessage[3];
                    msg.addReceiver(new jade.core.AID("cleaner0", jade.core.AID.ISLOCALNAME));
                    msg.addReceiver(new jade.core.AID("cleaner1", jade.core.AID.ISLOCALNAME));
                    msg.addReceiver(new jade.core.AID("cleaner2", jade.core.AID.ISLOCALNAME));
                    msg.setContent(i + "," + j);
                    send(msg);
                    System.out.println("Sent CFP to cleaner");

                    for (int k = 0; k < 3; k++) {
                        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchSender(new jade.core.AID("cleaner" + k, jade.core.AID.ISLOCALNAME)), 
                        MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
                        msgs[k] = blockingReceive(template);
                    }

                    if (msgs[0] != null && msgs[1] != null && msgs[2] != null) {
                        System.out.println("Received PROPOSE from All cleaners");
                        int x[] = new int[3];
                        int y[] = new int[3];
                        boolean last[] = new boolean[3];

                        for (int k = 0; k < 3; k++) {
                        String content = msgs[k].getContent();
                        String[] coordinates = content.split(",");
                        System.out.println(content);
                        x[k] = Integer.parseInt(coordinates[0]);
                        y[k] = Integer.parseInt(coordinates[1]);
                        last[k] = Boolean.parseBoolean(coordinates[2]);
                        }                        
                        int randomNumberInRange = -1;
                        if (distance(x[0], y[0], i, j) <= distance(x[1], y[1], i, j) && distance(x[0], y[0], i, j) <= distance(x[2], y[2], i, j) && !last[0])

                            randomNumberInRange = 0;
                        else if (distance(x[1], y[1], i, j) <= distance(x[2], y[2], i, j) || last[2])
                            randomNumberInRange = 1;
                        else
                            randomNumberInRange = 2;
                        for (int k = 0; k < 3; k++) {
                        msgs[k] = msgs[k].createReply();
                        if (randomNumberInRange == k)
                            msgs[k].setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        else
                            msgs[k].setPerformative(ACLMessage.REJECT_PROPOSAL);
                        send(msgs[k]);
                        System.out.println("Sent ACCEPT_PROPOSAL to cleaner " + randomNumberInRange);
                        }
                    }
                }
            }
        });
    }
}

//java -cp .;lib\jade.jar jade.Boot -gui -agents "dirtier:DirtierAgent;cleaner0:CleanerAgent;cleaner1:CleanerAgent;cleaner2:CleanerAgent"