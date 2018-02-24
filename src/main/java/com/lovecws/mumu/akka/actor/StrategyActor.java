package com.lovecws.mumu.akka.actor;

import akka.actor.*;
import akka.japi.Function;
import com.typesafe.config.ConfigFactory;
import scala.Option;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * @author babymm
 * @version 1.0-SNAPSHOT
 * @Description: 策略
 * @date 2018-02-23 14:48:
 */
public class StrategyActor extends UntypedActor {

    public enum Msg {
        DONE, RESTART;
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(3, Duration.create(1, TimeUnit.MINUTES),//一分钟内重试3次，超过则kill掉actor
                new Function<Throwable, SupervisorStrategy.Directive>() {
                    @Override
                    public SupervisorStrategy.Directive apply(Throwable throwable) throws Exception {
                        if (throwable instanceof ArithmeticException) {//ArithmeticException是出现异常的运算条件时，抛出此异常。例如，一个整数“除以零”时，抛出此类的一个实例。
                            System.out.println("meet ArithmeticException ,just resume.");
                            return SupervisorStrategy.resume();//继续; 重新开始; 恢复职位;
                        } else if (throwable instanceof NullPointerException) {
                            System.out.println("meet NullPointerException , restart.");
                            return SupervisorStrategy.restart();
                        } else if (throwable instanceof IllegalArgumentException) {
                            System.out.println("meet IllegalArgumentException ,stop.");
                            return SupervisorStrategy.stop();
                        } else {
                            System.out.println("escalate.");
                            return SupervisorStrategy.escalate();//使逐步升级; 使逐步上升; 乘自动梯上升;也就是交给更上层的actor处理。抛出异常
                        }
                    }
                });
    }

    @Override
    public void onReceive(Object o) throws Throwable {
        if (o instanceof Props) {
            getContext().actorOf((Props) o, "restartActor");
        } else {
            unhandled(o);
        }
    }

    public static class RestartActor extends UntypedActor {
        @Override
        public void preStart() throws Exception {
            System.out.println("preStart    hashCode=" + this.hashCode());
        }

        @Override
        public void postStop() throws Exception {
            System.out.println("postStop    hashCode=" + this.hashCode());
        }

        @Override
        public void preRestart(Throwable reason, Option<Object> message) throws Exception {
            System.out.println("preRestart    hashCode=" + this.hashCode());
        }

        @Override
        public void postRestart(Throwable reason) throws Exception {
            super.postRestart(reason);
            System.out.println("postRestart    hashCode=" + this.hashCode());
        }


        @Override
        public void onReceive(Object o) throws Throwable {
            if (o == Msg.DONE) {
                getContext().stop(getSelf());
            } else if (o == Msg.RESTART) {
                System.out.println(((Object) null).toString());
                //抛出异常，默认会被restart，但这里会resume
                //double a = 1/0;
            } else {
                unhandled(o);
            }
        }
    }

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("strategy", ConfigFactory.load("akka.config"));
        ActorRef superVisor = system.actorOf(Props.create(StrategyActor.class), "SuperVisor");
        superVisor.tell(Props.create(RestartActor.class), ActorRef.noSender());

        ActorSelection actorSelection = system.actorSelection("akka://strategy/user/SuperVisor/restartActor");//这是akka的路径。restartActor是在SuperVisor中创建的。

        for(int i = 0 ; i < 100 ; i ++){
            actorSelection.tell(StrategyActor.Msg.RESTART, ActorRef.noSender());
        }
    }
}
