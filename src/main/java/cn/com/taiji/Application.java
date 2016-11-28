package cn.com.taiji;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import cn.com.taiji.security.TaijiAuthenticationProvider;
import cn.com.taiji.security.TaijiFailureHandler;
import cn.com.taiji.security.TaijiFilterSecurityInterceptor;
import cn.com.taiji.security.TaijiSecurityMetadataSource;
import cn.com.taiji.security.TaijiSuccessHandler;
import cn.com.taiji.security.TaijiUserDetailServiceImpl;
import cn.com.taiji.security.TaijiUsernamePasswordAuthenticationFilter;
import cn.com.taiji.sys.interceptor.DMGridDataFormatInterceptor;
import cn.com.taiji.sys.json.SysModule;
import cn.com.taiji.sys.service.MenuService;
import cn.com.taiji.sys.service.RoleService;
import cn.com.taiji.sys.service.UserService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.joda.JodaModule;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableGlobalMethodSecurity(securedEnabled = true)
// @EnableMongoRepositories(value= {"cn.com.taiji.document"})
// @EnableJpaRepositories(value = {"cn.com.taiji.repository"})
@AutoConfigureAfter(JacksonAutoConfiguration.class)
public class Application extends WebMvcConfigurerAdapter {

	@Bean
	public FilterRegistrationBean encodeRegistrationBean() {
		CharacterEncodingFilter encodeFilter = new CharacterEncodingFilter();
		encodeFilter.setEncoding("UTF-8");
		encodeFilter.setForceEncoding(true);
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(encodeFilter);
		registrationBean.setOrder(Ordered.LOWEST_PRECEDENCE);
		return registrationBean;
	}

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(Application.class);
		app.setShowBanner(false);
		app.run(args);
	}
	
	@Bean
	public ObjectMapper jacksonObjectMapper() {
		return new ObjectMapper().registerModule(new JodaModule())
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
				.configure(SerializationFeature.INDENT_OUTPUT, true)
				.setDateFormat(new ISO8601DateFormat())
				.registerModule(new SysModule());
//				.registerModule(new PortalModule())
//				.registerModule(new CmsModule());// @TODO add more Module
	}
//	@PostConstruct
//	public void init() {
//		// @formatter:off
//		jacksonObjectMapper
//				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
//				.configure(SerializationFeature.INDENT_OUTPUT, true)
//				.setDateFormat(new ISO8601DateFormat())
//				.registerModule(jacksonJodaModule) 
//				.registerModule(new SysModule())
//				.registerModule(new PortalModule())
//				.registerModule(new CmsModule());// @TODO add more Module
//		// @formatter:on
//	}
//
//	@Autowired
//	ObjectMapper jacksonObjectMapper;
//
//	@Autowired
//	JodaModule jacksonJodaModule;

	// @Bean
	// @Primary
	// public ObjectMapper jacksonObjectMapper() {
//		// @formatter:off
//		return new ObjectMapper()
//				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
//				.configure(SerializationFeature.INDENT_OUTPUT, true)
//				.setDateFormat(new ISO8601DateFormat())
//				.registerModule(new JodaModule()) // add more Module
//				.registerModule(new SysModule());
//		// @formatter:on
	// }

	@Bean
	public MappingJackson2JsonView mappingJackson2JsonView() {
		MappingJackson2JsonView v = new org.springframework.web.servlet.view.json.MappingJackson2JsonView();
		v.setObjectMapper(jacksonObjectMapper() );
		v.setPrettyPrint(true);
		return v;
	}

	protected class MappingJackson2JsonpView extends MappingJackson2JsonView {
		public static final String DEFAULT_CONTENT_TYPE = "application/javascript";

		@Override
		public String getContentType() {
			return DEFAULT_CONTENT_TYPE;
		}

		@Override
		public void render(Map<String, ?> model, HttpServletRequest request,
				HttpServletResponse response) throws Exception {
			Map<String, String[]> params = request.getParameterMap();
			if (params.containsKey("callback")) {
				response.getOutputStream().write(
						new String(params.get("callback")[0] + "(").getBytes());
				super.render(model, request, response);
				response.getOutputStream().write(new String(");").getBytes());
				response.setContentType(DEFAULT_CONTENT_TYPE);
			} else {
				super.render(model, request, response);
			}
		}
	}

	@Bean
	public MappingJackson2JsonpView mappingJackson2JsonpView() {
		MappingJackson2JsonpView v = new MappingJackson2JsonpView();
		v.setObjectMapper(jacksonObjectMapper() );
		v.setPrettyPrint(false);
		return v;
	}

	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;
	@Autowired
	FreeMarkerViewResolver freeMarkerViewResolver;
	
	/*添加multipart配置 2014-11-13 SunJingyan Start*/
	@Autowired
	private MultipartProperties multipartProperties = new MultipartProperties();
	
	@Bean
	@ConditionalOnMissingBean
	public MultipartConfigElement multipartConfigElement() {
	             this.multipartProperties.setMaxFileSize("-1");
	             this.multipartProperties.setMaxRequestSize("-1");
	             return this.multipartProperties.createMultipartConfig();
	}
	
	@Bean
	@ConditionalOnMissingBean
	public StandardServletMultipartResolver multipartResolver() {
	return new StandardServletMultipartResolver();
	}
	/*添加multipart配置 2014-11-13 SunJingyan End*/

	// @Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) 
	{
		configurer.favorParameter(true).ignoreAcceptHeader(false)
				.defaultContentType(MediaType.TEXT_HTML)
				.mediaType("json", MediaType.APPLICATION_JSON)
				.mediaType("jsonp", MediaType.valueOf("application/javascript"));
	}
	/*@Autowired
	InternalResourceViewResolver jspViewResolver;*/
//	@Autowired  
//    protected freemarker.template.Configuration configuration; 
	@Bean
	public ViewResolver contentNegotiatingViewResolver(ContentNegotiationManager manager) 
	{
//		configuration.setDateFormat("yyyy/MM/dd");  
//        configuration.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");  
		List<ViewResolver> resolvers = new ArrayList<ViewResolver>();
		//jspViewResolver.setPrefix("classpath:/templates/jsp/");
		//jspViewResolver.setSuffix(".jsp");
		//jspViewResolver.setOrder(0);
		//resolvers.add(jspViewResolver);
		freeMarkerViewResolver.setOrder(1);
//		freeMarkerViewResolver.setExposeRequestAttributes(true);
//		freeMarkerViewResolver.setExposeSessionAttributes(true);
//		freeMarkerViewResolver.setExposeSpringMacroHelpers(true);
//		freeMarkerViewResolver.setCache(false);  
//		freeMarkerViewResolver.setPrefix("classpath:/templates/ftl/");
//		freeMarkerViewResolver.setSuffix(".ftl");
		resolvers.add(freeMarkerViewResolver);
		thymeleafViewResolver.setCache(false);
		resolvers.add(thymeleafViewResolver);
		ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
		resolver.setViewResolvers(resolvers);
		resolver.setContentNegotiationManager(manager);

		List<View> views = new ArrayList<View>();
		views.add(mappingJackson2JsonView());
		views.add(mappingJackson2JsonpView());
		resolver.setDefaultViews(views);
		return resolver;

	}

	// see
	// org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.ThymeleafDefaultConfiguration
	@Bean
	public Collection<IDialect> dialects() {
		Collection<IDialect> dialects = new HashSet<IDialect>();
		dialects
				.add(new org.thymeleaf.extras.springsecurity3.dialect.SpringSecurityDialect());
		return dialects;
	}

	// @Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/login").setViewName("login");
//		registry.addViewController("/sms/send").setViewName("/sms/send");
//		registry.addViewController("/autologin").setViewName("/auto_login");
	}

	@Order(Ordered.HIGHEST_PRECEDENCE) 
	@Configuration
	protected static class AuthenticationSecurity extends
	GlobalAuthenticationConfigurerAdapter {

		@Autowired
		private DataSource dataSource;

		@Bean
		public ShaPasswordEncoder passwordEncoder() {
			// return new BCryptPasswordEncoder();
			return new ShaPasswordEncoder(1);
		}
		//@Autowired
		//public    TaijiAuthenticationProvider customAuthenticationProvider;
		
		@Autowired
		public TaijiUserDetailServiceImpl userDetailsService;
		
		@Override
		public void init(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(userDetailsService);

		}
	}
	/**
     * 配置拦截器
     * @author lance
     * @param registry
     */
//    public void addInterceptors(InterceptorRegistry registry) {
//    	registry.addInterceptor(new DMGridDataFormatInterceptor()).addPathPatterns("/**");
//    }
	@Order(Ordered.LOWEST_PRECEDENCE - 8)
	protected static class ApplicationSecurity extends
	WebSecurityConfigurerAdapter {
		
		@Autowired
		private UserService userService;

		@Bean
		public TaijiUsernamePasswordAuthenticationFilter loginFilter()
				throws Exception {
			TaijiUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter = new TaijiUsernamePasswordAuthenticationFilter();
			customUsernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(customSuccessHandler());
			customUsernamePasswordAuthenticationFilter.setAuthenticationFailureHandler(customFailureHandler());
			customUsernamePasswordAuthenticationFilter.setAuthenticationManager(authenticationManagerBean());
			customUsernamePasswordAuthenticationFilter.setUserService(userService);
			customUsernamePasswordAuthenticationFilter.setFilterProcessesUrl("/j_spring_security_check");
	
			return customUsernamePasswordAuthenticationFilter;
		}



		@Bean
		public TaijiFailureHandler customFailureHandler() {
			TaijiFailureHandler customFailureHandler = new TaijiFailureHandler();
			customFailureHandler.setDefaultFailureUrl("/login?error");
			return customFailureHandler;
		}

		@Bean
		public TaijiSuccessHandler customSuccessHandler() {
			TaijiSuccessHandler customSuccessHandler = new TaijiSuccessHandler();
			customSuccessHandler.setDefaultTargetUrl("/sysGover/user-list");
			return customSuccessHandler;
		}

		@Bean
		@Override
		public AuthenticationManager authenticationManagerBean() throws Exception {
			List<AuthenticationProvider> authenticationProviderList = new ArrayList<AuthenticationProvider>();
			authenticationProviderList.add(customAuthenticationProvider());
			AuthenticationManager authenticationManager = new ProviderManager(authenticationProviderList);
			return authenticationManager;
		}
		@Autowired
		public TaijiUserDetailServiceImpl userDetailsService;
		@Bean
		private TaijiAuthenticationProvider customAuthenticationProvider() {
			TaijiAuthenticationProvider customAuthenticationProvider = new TaijiAuthenticationProvider();
			customAuthenticationProvider.setUserDetailsService(userDetailsService);
			return customAuthenticationProvider;
		}
		
		/*@Bean
		private cn.com.taiji.security.TaijiAccessDecisionManager accessDecisionManager(){
			cn.com.taiji.security.TaijiAccessDecisionManager accessDecisionManager=new cn.com.taiji.security.TaijiAccessDecisionManager();
		return accessDecisionManager;
		}*/
		
		@Autowired
		private MenuService menuService;
		@Autowired
		private RoleService roleService;
		@Bean
		private TaijiSecurityMetadataSource fisMetadataSource(){
			TaijiSecurityMetadataSource fisMetadataSource=new TaijiSecurityMetadataSource();
			fisMetadataSource.setMenuService(menuService);
			fisMetadataSource.setRoleService(roleService);
			return fisMetadataSource;
		}
		@Autowired
		private cn.com.taiji.security.TaijiAccessDecisionManager accessDecisionManager;
		@Bean
		public TaijiFilterSecurityInterceptor taijifiltersecurityinterceptor() throws Exception{
			TaijiFilterSecurityInterceptor taijifiltersecurityinterceptor=new TaijiFilterSecurityInterceptor();
			taijifiltersecurityinterceptor.setFisMetadataSource(fisMetadataSource());
			taijifiltersecurityinterceptor.setAccessDecisionManager(accessDecisionManager);
			taijifiltersecurityinterceptor.setAuthenticationManager(authenticationManagerBean());
			return taijifiltersecurityinterceptor;
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http.authorizeRequests()
			.filterSecurityInterceptorOncePerRequest(true)//过滤器的安全拦截器的每一次的要求
			.antMatchers("/login").permitAll()   // for login
			.antMatchers("/SysDicClass/*").permitAll()
			//.antMatchers("/sysGover/*").permitAll()
			.antMatchers("/j_spring_security_check").permitAll()
			
			.anyRequest().fullyAuthenticated() //.accessDecisionManager(accessDecisionManager)    // all others need login
			.and()
			.formLogin().loginPage("/login").failureUrl("/login?error") // login config
			.and()
			.logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")) //logout config
			.and()//.addFilte(access());
			.exceptionHandling().accessDeniedPage("/access");  // exception
			//http.csrf().disable().addFilterAfter(taijifiltersecurityinterceptor(), FilterSecurityInterceptor.class)
			//.addFilter(loginFilter()).rememberMe();
			// @formatter:on
			http.csrf().disable();
		}

	}
	
	

}