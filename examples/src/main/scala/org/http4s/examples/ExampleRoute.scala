package org.http4s

import scalaz.concurrent.Task
import scalaz.stream.Process
import scala.concurrent.Future
import org.http4s.dsl._

class ExampleRoute {
  import Status._
  import Writable._
  import BodyParser._

  val flatBigString = (0 until 1000).map{ i => s"This is string number $i" }.foldLeft(""){_ + _}

  import scala.concurrent.ExecutionContext.Implicits.global

  val MyVar = AttributeKey[Int]("myVar")

  def apply(): HttpService = {
    case Get -> Root / "ping" =>
      Ok("pong")

    case req @ Post -> Root / ("echo" | "echo2") =>
      Task.now(Response(body = req.body.map {
        case chunk: BodyChunk => chunk.slice(6, chunk.length)
        case chunk => chunk
      }))


    case req @ Post -> Root / "sum"  =>
      text(req) { s =>
        val sum = s.split('\n').map(_.toInt).sum
        Ok(sum)
      }

    case req @ Post -> Root / "shortsum"  =>
      text(req) { s =>
        val sum = s.split('\n').map(_.toInt).sum
        Ok(sum)
      }
/*
    case req @ Get -> Root / "attributes" =>
      val req2 = req.updated(MyVar, 55)
      Ok("Hello" + req(MyVar) +  " and " + req2(MyVar) + ".\n")

    case req @ Post -> Root / "trailer" =>
      trailer(t => Ok(t.headers.length))

    case req @ Post -> Root / "body-and-trailer" =>
      for {
        body <- text(req.charset)
        trailer <- trailer
      } yield Ok(s"$body\n${trailer.headers("Hi").value}")
*/

    case Get -> Root / "html" =>
      Ok(
        <html><body>
          <div id="main">
            <h2>Hello world!</h2><br/>
            <h1>This is H1</h1>
          </div>
        </body></html>
      )

/*
    case req @ Get -> Root / "stream" =>
      Ok(Concurrent.unicast[ByteString]({
        channel =>
          new Thread {
            override def run() {
              for (i <- 1 to 10) {
                channel.push(ByteString("%d\n".format(i), req.charset.value))
                Thread.sleep(1000)
              }
              channel.eofAndEnd()
            }
          }.start()

      }))

    case Get -> Root / "bigstring" =>
      Ok(body = (0 until 1000).map(i => BodyChunk(s"This is string number $i")))
*/

    case Get -> Root / "future" =>
      Ok(Future("Hello from the future!"))

    case req @ Get -> Root / "bigstring2" =>
      val body = Process.range(0, 1000).map(i => BodyChunk(s"This is string number $i"))
      Task.now {
        Response(body = body)
      }

    /*
  case req @ Get -> Root / "bigstring3" =>
    Done{
      Ok(flatBigString)
    }

<<<<<<< HEAD
  case Get -> Root / "contentChange" =>
    Ok("<h2>This will have an html content type!</h2>", MediaTypes.`text/html`)
=======
    case Get -> Root / "contentChange" =>
      Ok("<h2>This will have an html content type!</h2>", MediaType.`text/html`)
>>>>>>> develop

    case req @ Get -> Root / "challenge" =>
<<<<<<< HEAD
      req.body |> (await1[Chunk] flatMap {
        case bits: BodyChunk if (bits.decodeString(req.prelude.charset)).startsWith("Go") =>
          Process.emit(Response(body = emit(bits) then req.body))
        case bits: BodyChunk if (bits.decodeString(req.prelude.charset)).startsWith("NoGo") =>
          Process.emit(Response(ResponsePrelude(status = Status.BadRequest), body = Process.emit(BodyChunk("Booo!"))))
=======
      Iteratee.head[Chunk].map {
        case Some(bits: BodyChunk) if (bits.decodeString(req.charset)).startsWith("Go") =>
          Ok(Enumeratee.heading(Enumerator(bits: Chunk)))
        case Some(bits: BodyChunk) if (bits.decodeString(req.charset)).startsWith("NoGo") =>
          BadRequest("Booo!")
>>>>>>> develop
        case _ =>
          Process.emit(Response(ResponsePrelude(status = Status.BadRequest), body = Process.emit(BodyChunk("no data"))))
      })

    case req @ Root :/ "root-element-name" =>
      req.body |> takeBytes(1024 * 1024) |>
        (processes.fromSemigroup[Chunk].map { chunks =>
          val in = chunks.asInputStream
          val source = new InputSource(in)
          source.setEncoding(req.prelude.charset.value)
          val elem = XML.loadXML(source, XML.parser)
          Response(body = emit(BodyChunk(elem.label)))
        }).liftR |>
        processes.lift(_.fold(identity _, identity _))
*/
    case req @ Get -> Root / "fail" =>
      sys.error("FAIL")

    case req =>
      println("Got request that didn't match: " + req.prelude.pathInfo)
      Task.now(Response(body = Process.emit(s"Didn't find match: ${req.prelude.pathInfo}").map(s => BodyChunk(s.getBytes))))
  }
}