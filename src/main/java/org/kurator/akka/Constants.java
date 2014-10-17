package org.kurator.akka;

import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.util.Timeout;

public class Constants {
    static final int TIMEOUT_SECONDS = 5000;
    static final FiniteDuration TIMEOUT_DURATION = Duration.create(
            Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS);
    static final Timeout TIMEOUT = new Timeout(TIMEOUT_DURATION);
}
