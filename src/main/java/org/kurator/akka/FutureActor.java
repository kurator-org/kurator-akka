package org.kurator.akka;

import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.dispatch.OnComplete;
import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.messages.FutureComplete;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.concurrent.Callable;

/**
 * Extension of AkkaActor for actors that wrap the action triggered by a message into
 * a future (which frees the actor up to handle the next incoming message, but 
 * requires special handling of the end of stream control message to prevent the 
 * actor system from shutting down while futures are still unfulfilled. 
 *  
 * @author David Lowery
 *
 */
public class FutureActor extends AkkaActor {
    private int futuresCompleted = 0;
    private int futuresInitialized = 0;

    private boolean waitingForFutures = false;

    public FutureActor() {
        endOnEos = false;
    }

    @Override
    protected void onFutureComplete(FutureComplete message) throws Exception {
        broadcast(message.getValue());
        futuresCompleted++;

        if (waitingForFutures && futuresCompleted == futuresInitialized) {
            endStreamAndStop();
        }
    }

    @Override
    protected void onEndOfStream(EndOfStream eos) throws Exception {
        waitingForFutures = true;
    }

    protected void future(Callable<?> callable, ExecutionContext executionContext) {
        Future future = Futures.future(callable, executionContext);
        futuresInitialized++;

        future.onComplete(new OnComplete() {
            public void onComplete(Throwable throwable, Object obj) throws Throwable {
                self().tell(new FutureComplete(obj), getSelf());
            }
        }, executionContext);
    }

    protected void future(Callable callable, ExecutionContext executionContext, Mapper mapper) {
        Future future = Futures.future(callable, executionContext).map(mapper, executionContext);
        futuresInitialized++;

        future.onComplete(new OnComplete() {
            public void onComplete(Throwable throwable, Object obj) throws Throwable {
                self().tell(new FutureComplete(obj), getSelf());
            }
        }, executionContext);
    }
}
