# zio-concurrent

| CI | Release | Snapshot | Discord |
| --- | --- | --- | --- |
| [![Build Status][Badge-Circle]][Link-Circle] | [![Release Artifacts][Badge-SonatypeReleases]][Link-SonatypeReleases] | [![Snapshot Artifacts][Badge-SonatypeSnapshots]][Link-SonatypeSnapshots] | [![Badge-Discord]][Link-Discord] |

# Summary
A collection of lightweight, non-blocking, incorruptible concurrency primitives built for ZIO ecosystem.

## Rate Limiter
Allows slowing down some a series of effects (or a single effect run multiple times) to match the given throughput.
Example: 

```scala
import zio.concurrent.RateLimiter
import zio.concurrent.RateLimiter._
import zio.console._
import zio._

for {
  limiter <- RateLimiter.make(Frequency.unsafePerSecond(1.0), buffer = 1)
  _       <- ZIO.foreachPar(1 to 1000)  { i =>
             limiter.rateLimit(putStrLn(i.toString))
          }
  _       <- limiter.close() 
} yield ()
 
```
The code above will print at most one number a second (after the initial burst of up to `buffer`).

# Documentation
[zio-concurrent Microsite](https://zio.github.io/zio-concurrent/)

# Contributing
[Documentation for contributors](https://zio.github.io/zio-concurrent/docs/about/about_contributing)

## Code of Conduct

See the [Code of Conduct](https://zio.github.io/zio-concurrent/docs/about/about_coc)

## Support

Come chat with us on [![Badge-Discord]][Link-Discord].


# License
[License](LICENSE)

[Badge-SonatypeReleases]: https://img.shields.io/nexus/r/https/oss.sonatype.org/dev.zio/zio-concurrent_2.12.svg "Sonatype Releases"
[Badge-SonatypeSnapshots]: https://img.shields.io/nexus/s/https/oss.sonatype.org/dev.zio/zio-concurrent_2.12.svg "Sonatype Snapshots"
[Badge-Discord]: https://img.shields.io/discord/629491597070827530?logo=discord "chat on discord"
[Badge-Circle]: https://circleci.com/gh/zio/zio-concurrent.svg?style=svg "circleci"
[Link-Circle]: https://circleci.com/gh/zio/zio-concurrent "circleci"
[Link-SonatypeReleases]: https://oss.sonatype.org/content/repositories/releases/dev/zio/zio-concurrent_2.12/ "Sonatype Releases"
[Link-SonatypeSnapshots]: https://oss.sonatype.org/content/repositories/snapshots/dev/zio/zio-concurrent_2.12/ "Sonatype Snapshots"
[Link-Discord]: https://discord.gg/2ccFBr4 "Discord"

