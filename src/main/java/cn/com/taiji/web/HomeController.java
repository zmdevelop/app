package cn.com.taiji.web;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class HomeController {
	private static final Logger log = LoggerFactory
			.getLogger(HomeController.class);
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index(HttpSession sc) {
		log.info(" {} ", sc.getServletContext().getRealPath("/"));
		return "index";
	}

	@RequestMapping(value = "/login")
	public String loginPage(HttpServletRequest request, HttpServletResponse response) {
		response.setHeader("X-Frame-Options", "SAMEORIGIN");
		return "login";
	}


	// 20140603165000
	// @RequestParam(value="mydt") @DateTimeFormat(pattern = "yyyyMMdd") DateTime 
	@RequestMapping(value = "/some",  method = RequestMethod.POST)
	public String somePost(Model model,  @RequestParam(value="mydt") @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime mydt) {
		System.out.println("***************************************************");
		System.out.println(mydt);
		System.out.println("***************************************************");
		model.addAttribute("dt", LocalDateTime.now());
		model.addAttribute("d", new Date());
		return "some";
	}
}