package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.enums.BlindsState;

public class Blinds extends AbstractBehavior<Blinds.BlindsCommand> {

    // interface
    public interface BlindsCommand {}

    // classes or "methods" callable -> triggered by tell
    public static final class MoveBlinds implements Blinds.BlindsCommand {
        final BlindsState blindsState;

        public MoveBlinds(BlindsState blindsState) {this.blindsState = blindsState; }
    }

    public static final class LogStatus implements Blinds.BlindsCommand {
        public LogStatus() {}
    }

    // initializing (called by HomeAutomationController)
    public static Behavior<BlindsCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new Blinds(context, groupId, deviceId));
    }

    // class attributes
    private final String groupId;
    private final String deviceId;
    private boolean blindsAreUp = true;

    // constructor
    public Blinds(ActorContext<BlindsCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;

        getContext().getLog().info("Blinds started");
    }

    @Override
    public Receive<BlindsCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(Blinds.MoveBlinds.class, this::onMoveBlinds)
                .onMessage(Blinds.LogStatus.class, this::onLogStatus)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<Blinds.BlindsCommand> onMoveBlinds (MoveBlinds moveBlinds) {
        BlindsState blindsState = moveBlinds.blindsState;

        getContext().getLog().info("Blinds received {}", blindsState);

        if (blindsState.equals(BlindsState.OPEN)) {
            getContext().getLog().info("Blinds are up");
            this.blindsAreUp = true;

        }
        else if (blindsState.equals(BlindsState.CLOSED)) {
            getContext().getLog().info("Blinds are down");
            this.blindsAreUp = false;
        }

        return this;
    }

    private Behavior<Blinds.BlindsCommand> onLogStatus(Blinds.LogStatus logStatus) {
        getContext().getLog().info("groupId: " + this.groupId);
        getContext().getLog().info("deviceId: " + this.deviceId);
        getContext().getLog().info("blindsAreUp: " + this.blindsAreUp);

        return Behaviors.same();
    }

    private Blinds onPostStop() {
        getContext().getLog().info("Blinds actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}
