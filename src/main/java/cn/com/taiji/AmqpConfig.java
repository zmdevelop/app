package cn.com.taiji;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.rabbitmq.client.Channel;

@Configuration
@EnableScheduling
public class AmqpConfig {

	final static String sample = "sample";

/**
 * 定义队列
 * @return
 */
	@Bean
	Queue queueUnicom() {
		return new Queue(sample, false);
	}
	
	/**
	 * 定义交换机
	 * @return
	 */
	@Bean
	TopicExchange exchangeUnicom() {
		return new TopicExchange(sample);
	}
	/**
	 * 交换机与队列绑定
	 * @param queue
	 * @param exchange
	 * @return
	 */
	@Bean
	Binding bindingQueueUnicom(Queue queue, TopicExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(sample);
	}

/**
 * 设置消息监听适配器
 * @param send
 * @return
 */
//	@Bean
//	MessageListenerAdapter listenerAdapter4(Sender send) {
//		return new MessageListenerAdapter(send, "foo");
//	}

/**
 * 设置一个接收消息的监听器.
 * @param connectionFactory
 * @param listenerAdapter
 * @return
 */
//	@Bean
//	SimpleMessageListenerContainer containerQueueUnicom(ConnectionFactory connectionFactory) {
//		SimpleMessageListenerContainer containe = new SimpleMessageListenerContainer();
//		containe.setConnectionFactory(connectionFactory);
//		containe.setQueueNames(sample);
//		containe.setMessageListener(new ChannelAwareMessageListener(){
//
//			@Override
//			public void onMessage(Message arg0, Channel arg1) throws Exception {
//				
//			}
//			
//		});
//		return containe;
//	}
}
