package zio.concurrent

import zio.ZIO
import zio.concurrent.RateLimiter.Frequency
import zio.concurrent.TestUtils.AwaitableRef
import zio.test._
import zio.test.environment.TestClock

object RateLimiterSpec
    extends DefaultRunnableSpec(
      suite("RateLimiter") {
        testM("should limit rate") {
          val buffer    = 1
          val frequency = Frequency.unsafePerSecond(1.0)
          val periods   = 3
          def adjustClockAndWait(times: Int, counter: AwaitableRef[Int]) =
            ZIO.foreach_(1 to times) { _ =>
              counter.doAndAwaitChange(TestClock.adjust(frequency.period))
            }
          for {
            counter <- AwaitableRef.make(0)
            limiter <- RateLimiter.make(frequency, buffer)
            producerFiber <- ZIO
                              .foreach(1 to 1000) { _ =>
                                limiter.rateLimit(counter.update(_ + 1))
                              }
                              .fork
            _ <- counter.await(_ == buffer + 1)
            _ <- adjustClockAndWait(periods, counter)
            // stop the producer fiber
            _            <- producerFiber.interrupt
            counterAfter <- counter.get
          } yield {
            assert(counterAfter, Assertion.equalTo(buffer + periods + 1))
          }
        }
      }
    )
