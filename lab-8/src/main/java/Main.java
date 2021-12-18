import Actors.ActorGuardian;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.ActorRef;
import akka.event.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        ActorSystem<String> guardian = ActorSystem.create(ActorGuardian.create(), "ActorGuardian");
        guardian.tell("start");
        try {
            System.out.println(">>> Press any key to exit <<<");
            System.in.read();
        }catch (IOException ignored) {
        } finally {
            guardian.tell("shutdown");
            guardian.terminate();
        }
    }
}
