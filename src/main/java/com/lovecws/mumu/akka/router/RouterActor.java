package com.lovecws.mumu.akka.router;

import akka.actor.*;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import com.lovecws.mumu.akka.actor.InboxActor;
import com.typesafe.config.ConfigFactory;
import scala.Option;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author babymm
 * @version 1.0-SNAPSHOT
 * @Description: 路由
 * @date 2018-02-23 15:00:
 */
public class RouterActor extends UntypedActor {

    public static AtomicBoolean flag = new AtomicBoolean(true);
    private static Router router = null;

    public synchronized void init() {
        if (router == null) {
            ArrayList<Routee> routees = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                ActorRef worker = getContext().actorOf(Props.create(InboxActor.class), "worker_" + i);
                getContext().watch(worker);//监听
                routees.add(new ActorRefRoutee(worker));
            }
            /**
             * RoundRobinRoutingLogic: 轮询
             * BroadcastRoutingLogic: 广播
             * RandomRoutingLogic: 随机
             * SmallestMailboxRoutingLogic: 空闲
             */
            router = new Router(new RoundRobinRoutingLogic(), routees);
        }
    }

    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        init();
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof InboxActor.Msg) {
            if (router == null) {
                init();
            }
            router.route(message, getSender());//进行路由转发
        } else if (message instanceof Terminated) {
            router = router.removeRoutee(((Terminated) message).actor());//发生中断，将该actor删除。当然这里可以参考之前的actor重启策略，进行优化，为了简单，这里仅进行删除处理
            System.out.println(((Terminated) message).actor().path() + " 该actor已经删除。router.size=" + router.routees().size());
            if (router.routees().size() == 0) {//没有可用actor了
                System.out.print("没有可用actor了，系统关闭。");
                flag.compareAndSet(true, false);
                getContext().system().terminate();
            }
        } else {
            unhandled(message);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("strategy", ConfigFactory.load("akka.config"));
        ActorRef routerTest = system.actorOf(Props.create(RouterActor.class), "RouterTest");

        int i = 1;
        while (flag.get()) {
            routerTest.tell(InboxActor.Msg.WORKING, ActorRef.noSender());
            if (i++ % 10 == 0) routerTest.tell(InboxActor.Msg.CLOSE, ActorRef.noSender());
            Thread.sleep(1000);
        }
    }
}
