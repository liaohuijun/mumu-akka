package com.lovecws.mumu.akka.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.typesafe.config.ConfigFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author babymm
 * @version 1.0-SNAPSHOT
 * @Description: actor不可变性
 * @date 2018-02-23 14:18:
 */
public class UnmodifiableActor extends UntypedActor {

    public static class Message {
        private String message;
        private List<String> list;

        public Message(String message, List<String> list) {
            this.message = message;
            this.list = Collections.unmodifiableList(list);
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<String> getList() {
            return list;
        }

        public void setList(List<String> list) {
            this.list = list;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "message='" + message + '\'' +
                    ", list=" + list +
                    '}';
        }
    }

    public static class Greeter extends UntypedActor {
        @Override
        public void onReceive(Object message) throws Throwable {
            System.out.println(getSelf().path() + ":\t" + message.toString());
            getSender().tell("ok", getSelf());
        }
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        ActorRef greeterActor = getContext().actorOf(Props.create(Greeter.class), "greeter");
        greeterActor.tell(new Message("love", Arrays.asList("cws", "youzi")), getSelf());
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        System.out.println(getSelf().path() + ":\t" + message.toString());
    }


    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("unmodifiable", ConfigFactory.defaultApplication());
        ActorRef unmodifiableActor = system.actorOf(Props.create(UnmodifiableActor.class), "UnmodifiableActor");
    }
}
