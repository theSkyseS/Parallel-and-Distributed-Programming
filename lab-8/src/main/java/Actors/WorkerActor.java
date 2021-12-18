package Actors;


import Utils.Mapper;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;


public class WorkerActor {
    //region message
    interface Command {

    }

    public static class doJob implements Command {
        public final Path path;
        public final ActorRef<MasterActor.Command> answerTo;

        public doJob(Path path, ActorRef<MasterActor.Command> answerTo) {
            this.path = path;
            this.answerTo = answerTo;
        }
    }

    //endregion
    //region static methods
    public static Behavior<Command> create() {
        return Behaviors.setup(
                context -> {
                    context.getLog().info("Starting worker");
                    return Behaviors.receive(Command.class)
                            .onMessage(doJob.class, doJob -> onDoJob(context, doJob))
                            .build();
                });
    }

    private static Behavior<Command> onDoJob(ActorContext<Command> context, doJob doJob) {
        context.getLog().debug("Got message {} from {}", doJob, context);
        String code = "";
        try {
            Scanner scanner = new Scanner(doJob.path);
            scanner.useDelimiter("\\Z");
            code = scanner.next();
        } catch (IOException e) {
            context.getLog().error("got IOException {}", e.getMessage());
        }
        doJob.answerTo.tell(new MasterActor.Work(Mapper.map(code)));
        return Behaviors.same();
    }

    //endregion
}
