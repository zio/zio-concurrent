package zio.concurrent

import java.util.concurrent.TimeUnit

import zio.{ Queue, Schedule, UIO, ZIO }
import zio.clock.Clock
import zio.duration._

/**
 * A rate limiter based on a blocking [[zio.Queue]] being drained at a constant rate.
 * An effect wrapped by the rate limiter first `offer`s an element to the queue (suspending the fiber if the queue is full),
 * before proceeding to run the original effect.
 */
sealed trait RateLimiter {

  /**
   * Converts the given `effect` to a new one, that will suspend if the Rate Limiter's max throughput rate was reached, before running
   * the original effect.
   */
  def rateLimit[R, E, A](effect: => ZIO[R, E, A]): ZIO[R, E, A]

  /**
   * Releases the underlying resources (the queue and the draining fiber).
   */
  def close(): UIO[Unit]
}

object RateLimiter {

  /**
   * makes a new instance of [[RateLimiter]]
   * @param rate the max throughput rate allowed by the rate limiter
   * @param buffer the queue size to allows bursts
   */
  def make(rate: Frequency, buffer: Int): ZIO[Clock, Nothing, RateLimiter] =
    for {
      queue <- Queue.bounded[Unit](buffer)
      // keeps draining the queue at given rate
      fiber <- queue.take.repeat(Schedule.fixed(rate.period)).fork
    } yield {
      new RateLimiter {
        override def rateLimit[R, E, A](effect: => ZIO[R, E, A]): ZIO[R, E, A] =
          queue.offer(()) *> effect
        override def close(): UIO[Unit] = fiber.interrupt.unit *> queue.shutdown
      }
    }

  private val secondInNanos = TimeUnit.SECONDS.toNanos(1).toDouble

  final case class Frequency private (perSecond: Double) {
    val period: Duration = (secondInNanos / perSecond).toInt.nanos
  }

  object Frequency {
    def perSecond(f: Double): Either[String, Frequency] =
      if (f <= 0) Left(s"0 or negative Frequency not supported (was $f)")
      else Right(Frequency(f))

    def unsafePerSecond(f: Double): Frequency =
      perSecond(f).fold(msg => throw new IllegalArgumentException(msg), identity)
  }
}
