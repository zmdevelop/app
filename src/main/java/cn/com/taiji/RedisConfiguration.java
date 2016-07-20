package cn.com.taiji;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

@Configuration
public class RedisConfiguration {

	@Bean
	public JedisCluster getRedisCluster(){
		Set<HostAndPort> redisNodes = new HashSet<HostAndPort>(); 
		ResourceBundle resource = ResourceBundle.getBundle("redis");
		Enumeration<String> nodes = resource.getKeys();
		while(nodes.hasMoreElements())
		{
			String key = nodes.nextElement();
			String ipAndPort = resource.getString(key);
			String ip = ipAndPort.split(":")[0];
			int port = Integer.valueOf(ipAndPort.split(":")[1]);
			redisNodes.add(new HostAndPort(ip,port));
			System.out.println(ip + " " +port);
		}
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(100);  
		config.setMaxIdle(5); 
		config.setMaxWaitMillis(1000 * 100);  
		config.setTestOnBorrow(true);  
		JedisCluster jc = new JedisCluster(redisNodes,5000,redisNodes.size(),config);
		return jc;
	}
}
