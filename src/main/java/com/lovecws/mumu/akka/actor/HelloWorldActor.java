package com.lovecws.mumu.akka.actor;

import akka.actor.*;
import com.typesafe.config.ConfigFactory;

/**
 * @author babymm
 * @version 1.0-SNAPSHOT
 * @Description: actor hello world
 * @date 2018-02-23 13:47:
 */
public class HelloWorldActor extends UntypedActor {

    @Override
    public void preStart() throws Exception {
        ActorRef greetActor = getContext().actorOf(Props.create(GreetActor.class), "greet");
        System.out.println(getSelf().path() + ":\tsend message to greet...");
        greetActor.tell("greet", getSelf());
        super.preStart();
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message.toString().equals("end")) {
            System.out.println(getSelf().path() + ":\treceive end message close helloworld actor...");
            getContext().stop(getSelf());
            System.exit(1);
        } else {
            unhandled(message);
        }
    }

    public static class GreetActor extends UntypedActor {
        @Override
        public void onReceive(Object message) throws Throwable {
            if (message.toString().equals("greet")) {
                System.out.println(getSelf().path() + ":\treceive greet message print hello world...");
                Thread.sleep(1000);
                getSender().tell("end", getSelf());
            } else {
                unhandled(message);
            }
        }
    }

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("helloworld", ConfigFactory.load("akka.config"));
        ActorRef helloworldActor = system.actorOf(Props.create(HelloWorldActor.class), "helloworld");
        System.out.println(helloworldActor.path());

        //akka.Main.main(new String[]{HelloWorldActor.class.getName()});
    }
}
