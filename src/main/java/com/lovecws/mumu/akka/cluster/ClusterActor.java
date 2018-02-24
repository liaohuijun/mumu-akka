package com.lovecws.mumu.akka.cluster;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.ConfigFactory;

/**
 * @author babymm
 * @version 1.0-SNAPSHOT
 * @Description: akka远程服务
 * @date 2018-02-23 16:16:
 */
public class ClusterActor extends UntypedActor {

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    Cluster cluster = Cluster.get(getContext().system());

    @Override
    public void preStart() {
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(),
                ClusterEvent.MemberEvent.class, ClusterEvent.UnreachableMember.class);
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof ClusterEvent.MemberUp) {
            ClusterEvent.MemberUp mUp = (ClusterEvent.MemberUp) message;
            log.info("Member is Up: {}", mUp.member());
        } else if (message instanceof ClusterEvent.UnreachableMember) {
            ClusterEvent.UnreachableMember mUnreachable = (ClusterEvent.UnreachableMember) message;
            log.info("Member detected as unreachable: {}", mUnreachable.member());
        } else if (message instanceof ClusterEvent.MemberRemoved) {
            ClusterEvent.MemberRemoved mRemoved = (ClusterEvent.MemberRemoved) message;
            log.info("Member is Removed: {}", mRemoved.member());
        } else if (message instanceof ClusterEvent.MemberEvent) {
            System.out.println(message);
        } else {
            unhandled(message);
        }
    }

    public static void main(String[] args) {
        System.out.println("Start simpleClusterListener");
        ActorSystem system = ActorSystem.create("akkaClusterTest", ConfigFactory.load("reference.conf"));
        system.actorOf(Props.create(ClusterActor.class), "simpleClusterListener");
        System.out.println("Started simpleClusterListener");
    }
}
