package Actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ActorGuardian extends AbstractBehavior<String> {
    private ActorRef<MasterActor.Command> masterRef;
    //region static methods
    public static Behavior<String> create() {
        return Behaviors.setup(ActorGuardian::new);
    }

    //endregion
    //region constructors
    public ActorGuardian(final ActorContext<String> context) {
        super(context);
    }

    //endregion
    //region instance methods
    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals("start", this::onStart)
                .onMessageEquals("shutdown", this::onShutdown)
                .build();
    }

    private Behavior<String> onShutdown() {
        getContext().getLog().info("Shutting down");
        masterRef.tell(new MasterActor.Shutdown());
        return Behaviors.stopped();
    }

    private Behavior<String> onStart() {
        getContext().getLog().info("Starting");
        masterRef =
                getContext().spawn(MasterActor.create(), "master");
        masterRef.tell(new MasterActor.StartWorking());
        return this;
    }
    //endregion
}
