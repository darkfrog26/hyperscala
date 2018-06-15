package io.youi.client

import scala.concurrent.duration.FiniteDuration

object ClientPlatform {
  def createClient(defaultRetries: Int = HttpClient.retries,
                   defaultRetryDelay: FiniteDuration = HttpClient.retryDelay,
                   connectionPool: ConnectionPool = HttpClient.connectionPool): HttpClient = {
    JVMHttpClient(defaultRetries = defaultRetries, defaultRetryDelay = defaultRetryDelay, connectionPool = connectionPool)
  }

  def createPool(maxIdleConnections: Int = ConnectionPool.maxIdleConnections,
                 keepAlive: FiniteDuration = ConnectionPool.keepAlive): ConnectionPool = {
    JVMConnectionPool(maxIdleConnections, keepAlive)
  }
}