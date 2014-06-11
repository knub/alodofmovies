package lod2014group1.messaging

import com.rabbitmq.client._
import com.rabbitmq.client.ConnectionFactory
import com.typesafe.config.ConfigFactory

object ConnectionBuilder {
	private val conf = ConfigFactory.load()
	private val HOST_NAME = conf.getString("alodofmovies.hosts.server.host")
	private val VHOST = conf.getString("alodofmovies.hosts.server.vhost")
	private val USERNAME = conf.getString("alodofmovies.hosts.server.username")
	private val PASSWORD = conf.getString("alodofmovies.hosts.server.password")

	def newConnection(): Connection = {
		val factory = new ConnectionFactory()
		factory.setHost(HOST_NAME)
		factory.setVirtualHost(VHOST)
		factory.setUsername(USERNAME)
		factory.setPassword(PASSWORD)
		factory.newConnection
	}
}
