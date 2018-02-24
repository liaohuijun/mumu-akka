package com.lovecws.mumu.akka.actor;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.ConfigFactory;

/**
 * @author babymm
 * @version 1.0-SNAPSHOT
 * @Description: actor 生命周期
 * @date 2018-02-23 14:36:
 */
public class LifecycleActor extends UntypedActor {
    LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    public static enum Msg {
        WORKING, DONE, CLOSE;
    }

    @Override
    public void preStart() {
        logger.info("LifecycleActor starting.");
    }

    @Override
    public void postStop() throws Exception {
        logger.info("LifecycleActor stoping..");
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message == Msg.WORKING) {
            logger.info("i am  working");
        } else if (message == Msg.DONE) {
            logger.info("stop  working");
        } else if (message == Msg.CLOSE) {
            logger.info("stop  close");
            getSender().tell(Msg.CLOSE, getSelf());
            getContext().stop(getSelf());
        } else {
            unhandled(message);
        }
    }

    public static class WatchActor extends UntypedActor {
        LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

        public WatchActor(ActorRef actorRef) {
            getContext().watch(actorRef);
        }

        @Override
        public void onReceive(Object msg) throws InterruptedException {
            if (msg instanceof Terminated) {
                logger.error(((Terminated) msg).getActor().path() + " has Terminated. now shutdown the system");
                getContext().system().terminate();
            } else {
                unhandled(msg);
            }
        }
    }

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("lifecycle", ConfigFactory.load("akka.config"));
        ActorRef lifecycleActor = system.actorOf(Props.create(LifecycleActor.class), "lifecycleActor");
        ActorRef watchActor = system.actorOf(Props.create(WatchActor.class, lifecycleActor), "WatchActor");

        lifecycleActor.tell(LifecycleActor.Msg.WORKING, ActorRef.noSender());
        lifecycleActor.tell(LifecycleActor.Msg.DONE, ActorRef.noSender());

        //中断myWork
        lifecycleActor.tell(PoisonPill.getInstance(), ActorRef.noSender());
    }
}
