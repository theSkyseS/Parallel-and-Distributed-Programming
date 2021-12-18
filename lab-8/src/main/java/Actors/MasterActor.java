package Actors;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MasterActor extends AbstractBehavior<MasterActor.Command> {
    //region private fields
    private final Map<String, List<String>> classMap;

    //endregion
    //region messages
    interface Command {
    }

    public static final class Work implements Command {
        public final Map<String, List<String>> classMap;

        public Work(Map<String, List<String>> classMap) {
            this.classMap = classMap;
        }
    }

    public static final class StartWorking implements Command {


    }

    public static final class Shutdown implements Command{

    }

    //endregion
    //region static methods
    public static Behavior<Command> create() {
        return Behaviors.setup(MasterActor::new);
    }

    private static void putMergeAll(final Map<String, List<String>> map, final Map<String, List<String>> mapToPut) {
        mapToPut.forEach((key, value) -> {
            if (!map.containsKey(key)) {
                map.put(key, value);
            } else {
                map.get(key).addAll(value);
            }
        });
    }
    private static void printMap(final Map<String, List<String>> map, ActorContext<Command> context) {
        AtomicInteger counter = new AtomicInteger();
        Logger logger = context.getLog();
        map.forEach((x, y) -> {

            y.forEach(z -> {
                counter.getAndIncrement();
                logger.info("   " + z);
            });
            logger.info("\n");
        });
        logger.info(String.valueOf(map.size()));
        logger.info(String.valueOf(counter));
    }

    //endregion
    //region constructors
    public MasterActor(final ActorContext<Command> context) {
        super(context);
        classMap = new HashMap<>();
    }

    //endregion
    //region instance methods
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartWorking.class, command -> onStart())
                .onMessage(Work.class, this::onWork)
                .onMessage(Shutdown.class, this::onShutdown)
                .build();
    }

    private Behavior<Command> onStart() {
        getContext().getLog().info("Got start work message");
        return Behaviors.setup(context -> {
            List<Path> javaFiles = Files.walk(Path.of("../../spring-framework-main")).filter(Files::isRegularFile).filter(x -> x.getFileName().toString().endsWith(".java")).collect(Collectors.toList());
            int poolSize = 16;
            PoolRouter<WorkerActor.Command> pool =
                    Routers.pool(
                            poolSize,
                            Behaviors.supervise(WorkerActor.create()).onFailure(SupervisorStrategy.restart()));
            PoolRouter<WorkerActor.Command> blockingPool =
                    pool.withRouteeProps(DispatcherSelector.blocking());
            ActorRef<WorkerActor.Command> blockingRouter =
                    context.spawn(blockingPool, "blocking-pool", DispatcherSelector.sameAsParent());
            for (Path file : javaFiles) {
                blockingRouter.tell(new WorkerActor.doJob(file, context.getSelf()));
            }
            return Behaviors.same();
        });
    }

    private Behavior<Command> onWork(Work work) {
        getContext().getLog().debug("Got message {}, reducing", work);
        putMergeAll(work.classMap, classMap);
        return Behaviors.same();
    }

    private Behavior<Command> onShutdown(final Command shutdown) {
        printMap(classMap, getContext());
        getContext().getLog().info("Shutting down Master actor");
        return Behaviors.stopped();
    }

    //endregion
}

