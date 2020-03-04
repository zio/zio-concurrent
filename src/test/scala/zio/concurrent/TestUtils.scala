package zio.concurrent

import zio.clock.Clock
import zio.duration._
import zio.{ UIO, ZIO }
import zio.stm.{ STM, TRef }
import zio.test.environment.Live

import scala.concurrent.TimeoutException

object TestUtils {
  trait AwaitableRef[A] {
    def set(a: A): UIO[Unit]
    def update(f: A => A): UIO[A]
    def get: UIO[A]
    def await(p: A => Boolean): UIO[A]
    def doAndAwaitChange[R, E, A1](
      effect: => ZIO[R, E, A1],
      timeout: Duration = 1000.millis
    ): ZIO[R with Live[Clock], Any, A1] =
      for {
        before <- get
        _      = println(s"before $before")
        res    <- effect
        _      = println("after effect")
        _      <- Live.live(await(_ != before).timeoutFail(new TimeoutException)(timeout).orDie)
      } yield res
  }

  object AwaitableRef {
    def make[A](a: A): UIO[AwaitableRef[A]] =
      for {
        ref <- TRef.make(a).commit
      } yield new AwaitableRef[A] {
        override def set(a: A): UIO[Unit] = update(_ => a).unit

        override def update(f: A => A): UIO[A] =
          ref
            .update(f)
            .commit
            .tap(a => UIO(println(s"updated to $a")))

        override def get: UIO[A] = ref.get.commit.tap(a => UIO(println(s"got $a")))

        override def await(p: A => Boolean): UIO[A] =
          (for {
            v <- ref.get
            _ <- STM.check(p(v))
          } yield v).commit.tap(a => UIO(println(s"await returned with $a")))
      }
  }
}
