package org.influxdb.v08.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.v08.InfluxDB;
import org.influxdb.v08.dto.ContinuousQuery;
import org.influxdb.v08.dto.Database;
import org.influxdb.v08.dto.DatabaseConfiguration;
import org.influxdb.v08.dto.Pong;
import org.influxdb.v08.dto.Serie;
import org.influxdb.v08.dto.Server;
import org.influxdb.v08.dto.Shard;
import org.influxdb.v08.dto.ShardSpace;
import org.influxdb.v08.dto.Shards;
import org.influxdb.v08.dto.User;

import retrofit.RestAdapter;
import retrofit.client.Header;
import retrofit.client.OkClient;
import retrofit.client.Response;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

/**
 * Implementation of a InluxDB API.
 * 
 * @author stefan.majer [at] gmail.com
 * 
 */
public class InfluxDBImpl implements InfluxDB {
	private final String username;
	private final String password;
	private final RestAdapter restAdapter;
	private final InfluxDBService influxDBService;
	private final InetAddress host;
	private final static int UDP_MAX_MESSAGE_SIZE = 2048;

	/**
	 * Constructor which should only be used from the InfluxDBFactory.
	 * 
	 * @param url
	 *            the url where the influxdb is accessible.
	 * @param username
	 *            the user to connect.
	 * @param password
	 *            the password for this user.
	 */
	public InfluxDBImpl(final String url, final String username, final String password) {
		super();
		this.username = username;
		this.password = password;
		try {
			String hostPart = new URI(url).getHost();
			this.host = InetAddress.getByName(hostPart);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("The given URI is not valid " + e.getMessage());
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("The given URI is not valid " + e.getMessage());
		}
		OkHttpClient okHttpClient = new OkHttpClient();
		this.restAdapter = new RestAdapter.Builder()
				.setEndpoint(url)
				.setErrorHandler(new InfluxDBErrorHandler())
				.setClient(new OkClient(okHttpClient))
				.build();

		this.influxDBService = this.restAdapter.create(InfluxDBService.class);
	}

	@Override
	public InfluxDB setLogLevel(final LogLevel logLevel) {
		switch (logLevel) {
		case NONE:
			this.restAdapter.setLogLevel(retrofit.RestAdapter.LogLevel.NONE);
			break;
		case BASIC:
			this.restAdapter.setLogLevel(retrofit.RestAdapter.LogLevel.BASIC);
			break;
		case HEADERS:
			this.restAdapter.setLogLevel(retrofit.RestAdapter.LogLevel.HEADERS);
			break;
		case FULL:
			this.restAdapter.setLogLevel(retrofit.RestAdapter.LogLevel.FULL);
			break;
		default:
			break;
		}
		return this;
	}

	@Override
	public Pong ping() {
		Stopwatch watch = Stopwatch.createStarted();
		Pong pong = this.influxDBService.ping();
		pong.setResponseTime(watch.elapsed(TimeUnit.MILLISECONDS));
		return pong;
	}

	@Override
	public String version() {
		Response response = this.influxDBService.version();
		String version = "unknown";
		List<Header> headers = response.getHeaders();
		for (Header header : headers) {
			if (null != header.getName() && header.getName().equalsIgnoreCase("X-Influxdb-Version")) {
				version = header.getValue();
			}
		}
		return version;
	}

	@Override
	public void write(final String database, final TimeUnit precision, final Serie... series) {
		this.influxDBService.write(database, series, this.username, this.password, toTimePrecision(precision));
	}

	@Override
	public void writeUdp(final int port, final Serie... series) {
		Gson gson = new Gson();
		String data = gson.toJson(series);
		// see: https://github.com/influxdb/influxdb/blob/master/api/udp/api.go#L66
		Preconditions.checkArgument(data.length() < UDP_MAX_MESSAGE_SIZE, "The given data size: " + data.length()
				+ " is larger or equal to the allowed maximum:" + UDP_MAX_MESSAGE_SIZE);

		ByteBuffer buf = ByteBuffer.wrap(data.getBytes());

		try {
			DatagramChannel channel = DatagramChannel.open();
			channel.send(buf, new InetSocketAddress(this.host, port));
			buf.clear();
			channel.close();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public List<Serie> query(final String database, final String query, final TimeUnit precision) {
		return this.influxDBService.query(database, query, this.username, this.password, toTimePrecision(precision));
	}

	@Override
	public void createDatabase(final String name) {
		Database db = new Database(name);
		this.influxDBService.createDatabase(db, this.username, this.password);
	}

	@Override
	public void createDatabase(final DatabaseConfiguration config) {
		this.influxDBService.createDatabase(config.getName(), config, this.username, this.password);
	}

	@Override
	public void deleteDatabase(final String name) {
		this.influxDBService.deleteDatabase(name, this.username, this.password);
	}

	@Override
	public List<Database> describeDatabases() {
		return this.influxDBService.describeDatabases(this.username, this.password);
	}

	@Override
	public void createClusterAdmin(final String name, final String adminPassword) {
		User user = new User(name);
		user.setPassword(adminPassword);
		this.influxDBService.createClusterAdmin(user, this.username, this.password);
	}

	@Override
	public void deleteClusterAdmin(final String name) {
		this.influxDBService.deleteClusterAdmin(name, this.username, this.password);
	}

	@Override
	public List<User> describeClusterAdmins() {
		return this.influxDBService.describeClusterAdmins(this.username, this.password);
	}

	@Override
	public void updateClusterAdmin(final String name, final String adminPassword) {
		User user = new User(name);
		user.setPassword(adminPassword);
		this.influxDBService.updateClusterAdmin(user, name, this.username, this.password);
	}

	@Override
	public void createDatabaseUser(final String database, final String name, final String userPassword,
			final String... permissions) {
		User user = new User(name);
		user.setPassword(userPassword);
		user.setPermissions(permissions);
		this.influxDBService.createDatabaseUser(database, user, this.username, this.password);
	}

	@Override
	public void deleteDatabaseUser(final String database, final String name) {
		this.influxDBService.deleteDatabaseUser(database, name, this.username, this.password);
	}

	@Override
	public List<User> describeDatabaseUsers(final String database) {
		return this.influxDBService.describeDatabaseUsers(database, this.username, this.password);
	}

	@Override
	public void updateDatabaseUser(final String database, final String name, final String newPassword,
			final String... permissions) {
		User user = new User(name);
		user.setPassword(newPassword);
		user.setPermissions(permissions);
		this.influxDBService.updateDatabaseUser(database, user, name, this.username, this.password);
	}

	@Override
	public void alterDatabasePrivilege(final String database, final String name, final boolean isAdmin,
			final String... permissions) {
		User user = new User(name);
		user.setAdmin(isAdmin);
		user.setPermissions(permissions);
		this.influxDBService.updateDatabaseUser(database, user, name, this.username, this.password);
	}

	@Override
	public void authenticateDatabaseUser(final String database, final String user, final String userPassword) {
		this.influxDBService.authenticateDatabaseUser(database, user, userPassword);
	}

	@Override
	public List<ContinuousQuery> describeContinuousQueries(final String database) {
		return this.influxDBService.getContinuousQueries(database, this.username, this.password);
	}

	@Override
	public void deleteContinuousQuery(final String database, final int id) {
		this.influxDBService.deleteContinuousQuery(database, id, this.username, this.password);
	}

	@Override
	public void deleteSeries(final String database, final String serieName) {
		this.influxDBService.deleteSeries(database, serieName, this.username, this.password);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void forceRaftCompaction() {
		this.influxDBService.forceRaftCompaction(this.username, this.password);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> interfaces() {
		return this.influxDBService.interfaces(this.username, this.password);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean sync() {
		return this.influxDBService.sync(this.username, this.password);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Server> listServers() {
		return this.influxDBService.listServers(this.username, this.password);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeServers(final int id) {
		this.influxDBService.removeServers(id, this.username, this.password);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createShard(final Shard shard) {
		this.influxDBService.createShard(this.username, this.password, shard);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Shards getShards() {
		return this.influxDBService.getShards(this.username, this.password);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dropShard(final Shard shard) {
		this.influxDBService.dropShard(shard.getId(), this.username, this.password, shard.getShards().get(0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ShardSpace> getShardSpaces() {
		return this.influxDBService.getShardSpaces(this.username, this.password);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dropShardSpace(final String database, final String name) {
		this.influxDBService.dropShardSpace(database, name, this.username, this.password);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createShardSpace(final String database, final ShardSpace shardSpace) {
		this.influxDBService.createShardSpace(database, this.username, this.password, shardSpace);
	}

	private static String toTimePrecision(final TimeUnit t) {
		switch (t) {
		case SECONDS:
			return "s";
		case MILLISECONDS:
			return "ms";
		case MICROSECONDS:
			return "u";
		default:
			throw new IllegalArgumentException("time precision must be " + TimeUnit.SECONDS + ", "
					+ TimeUnit.MILLISECONDS + " or " + TimeUnit.MICROSECONDS);
		}
	}

}
