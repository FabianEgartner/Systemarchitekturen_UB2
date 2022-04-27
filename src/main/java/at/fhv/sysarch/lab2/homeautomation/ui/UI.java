package at.fhv.sysarch.lab2.homeautomation.ui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.HomeAutomationController;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.WeatherCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.WeatherSensor;

import java.util.Optional;
import java.util.Scanner;

public class UI extends AbstractBehavior<Void> {

    private ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private ActorRef<AirCondition.AirConditionCommand> airCondition;
    private ActorRef<WeatherSensor.WeatherCommand> weatherSensor;

    public static Behavior<Void> create(ActorRef<TemperatureSensor.TemperatureCommand> tempSensor,
                                        ActorRef<AirCondition.AirConditionCommand> airCondition,
                                        ActorRef<WeatherSensor.WeatherCommand> weatherSensor) {
        return Behaviors.setup(context -> new UI(context, tempSensor, airCondition, weatherSensor));
    }

    private UI(ActorContext<Void> context,
                ActorRef<TemperatureSensor.TemperatureCommand> tempSensor,
                ActorRef<AirCondition.AirConditionCommand> airCondition,
                ActorRef<WeatherSensor.WeatherCommand> weatherSensor) {
        super(context);
        // TODO: implement actor and behavior as needed
        // TODO: move UI initialization to appropriate place
        this.airCondition = airCondition;
        this.tempSensor = tempSensor;
        this.weatherSensor = weatherSensor;
        new Thread(() -> { this.runCommandLine(); }).start();

        getContext().getLog().info("UI started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private UI onPostStop() {
        getContext().getLog().info("UI stopped");
        return this;
    }

    public void runCommandLine() {
        // TODO: Create Actor for UI Input-Handling
        Scanner scanner = new Scanner(System.in);
        String[] input = null;
        String reader = "";


        while (!reader.equalsIgnoreCase("quit") && scanner.hasNextLine()) {
            reader = scanner.nextLine();
            // TODO: change input handling
            String[] command = reader.split(" ");
            if(command[0].equals("t")) {
                this.tempSensor.tell(new TemperatureSensor.ReadTemperature(Optional.of(Double.valueOf(command[1]))));
            }
            if(command[0].equals("a")) {
                String booleanInput = command[1].toLowerCase();

                if (booleanInput.equals("true")) {
                    this.airCondition.tell(new AirCondition.PowerAirCondition(true));
                }
                else if (booleanInput.equals("false")) {
                    this.airCondition.tell(new AirCondition.PowerAirCondition(false));
                }
            }
            if(command[0].equals("a_status")) {
                this.airCondition.tell(new AirCondition.LogStatus());
            }
            if(command[0].equals("w")) {
                String weatherInput = command[1].toUpperCase();
                if (weatherInput.equals(WeatherCondition.SUNNY.toString())) {
                    this.weatherSensor.tell(new WeatherSensor.ReadWeather(WeatherCondition.SUNNY));
                }
                else if (weatherInput.equals(WeatherCondition.CLOUDY.toString())) {
                    this.weatherSensor.tell(new WeatherSensor.ReadWeather(WeatherCondition.CLOUDY));
                }
                System.out.println(weatherInput);
            }
            if(command[0].equals("w_status")) {
                this.weatherSensor.tell(new WeatherSensor.LogStatus());
            }
            // TODO: process Input
        }
        getContext().getLog().info("UI done");
    }
}
