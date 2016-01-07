package org.http4s.client.blaze

import org.http4s.Request
import org.http4s.Uri.{Scheme, Authority}

import scalaz.concurrent.Task

/** type that is responsible for the client lifecycle
  *
  * The [[ConnectionManager]] is a general wrapper around a [[ConnectionBuilder]]
  * that can pool resources in order to conserve resources such as socket connections,
  * CPU time, SSL handshakes, etc. Because It can contain significant resources it
  * must have a mechanism to free resources associated with it.
  */
trait ConnectionManager {
  /** Shutdown this client, closing any open connections and freeing resources */
  def shutdown(): Task[Unit]

  /** Get a connection for the provided request key. */
  def getClient(requestKey: RequestKey): Task[BlazeClientStage]
  
  /** Release the connection with the given request key.
    *
    * @param keepAlive true if the connection may be kept alive to serve subsequent requests.
    *                  Implementations must close the connection if this is false, but may
    *                  keep it open if it is true.
    */
  def releaseClient(requestKey: RequestKey, stage: BlazeClientStage, keepAlive: Boolean): Unit
}

object ConnectionManager {
  /** Create a [[ConnectionManager]] that creates new connections on each request
    *
    * @param builder generator of new connections
    * */
  def basic(builder: ConnectionBuilder): ConnectionManager =
    new BasicManager(builder)

  /** Create a [[ConnectionManager]] that will attempt to recycle connections
    *
    * @param builder generator of new connections
    * @param maxTotal max total connections
    */
  def pool(builder: ConnectionBuilder, maxTotal: Int): ConnectionManager =
    new PoolManager(builder, maxTotal)
}
