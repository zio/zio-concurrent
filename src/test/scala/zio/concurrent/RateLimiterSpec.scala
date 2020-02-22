package zio.concurrent

import zio.concurrent.RateLimiter.Frequency
import zio.{ clock, Ref, ZIO }
import zio.test._
import zio.test.environment.{ Live, TestClock }
import zio.duration._

object RateLimiterSpec
    extends DefaultRunnableSpec(
      suite("RateLimiter") {
        testM("should limit rate") {
          val buffer = 10
          for {
            counter <- Ref.make(0)
            limiter <- RateLimiter.make(Frequency.unsafePerSecond(1.0), buffer = buffer)

            producerFiber <- ZIO
                              .foreach(1 to 1000) { _ =>
                                limiter.rateLimit(counter.update(_ + 1))
                              }
                              .fork
            _ <- TestClock.adjust(1.second)
            // give async stuff some time to work
            _ <- Live.live(clock.sleep(100.millis))
            // stop the producer fiber
            _            <- producerFiber.interrupt
            counterAfter <- counter.get
          } yield {
            assert(counterAfter, Assertion.equalTo(buffer + 2))
          }
        }
      }
    )
