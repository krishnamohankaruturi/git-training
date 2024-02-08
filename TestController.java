package com.ess.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {
	
	//@Autowired
	//TestService testService;
	JSONObject json = new JSONObject();
	private static final Logger LOGGER = LogManager.getLogger(TestController.class.getName());
	/**
	 * @PathVariable Example
	 * suppose you want to call an api by providing a value say 
	 * http://localhost:8080/ESS/api/testMethod1/123
	 * here "123" is the value provided by you
	 * */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/testMethod1/{msg}", method = RequestMethod.GET)
	public @ResponseBody String testMethod1(@PathVariable String msg) {
		json.put("msg", msg);
		return json.toString();
	}
	
	/**
	 * @RequestParam Example
	 * suppose you want to call an api by providing a value say 
	 * http://localhost:8080/ESS/api/testMethod2?msg=123
	 * here "123" is the value provided by you
	 * */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/testMethod2", method = RequestMethod.GET)
	public @ResponseBody String testMethod2(@RequestParam(value="msg", required=true) String msg) {
		json.put("msg", msg);
		return json.toString();
	}
	
	@RequestMapping(value="/testMethod3", method = RequestMethod.GET)
	public @ResponseBody String testMethod3() {
		LOGGER.debug("Debug Message Logged !!! for testMethod3");
        LOGGER.info("Info Message Logged !!! for testMethod3");
        LOGGER.error("Error Message Logged !!! for testMethod3", new NullPointerException("NullError"));
		//json.put("msg", testService.getMessage("hiii"));
		return json.toString();
	}
}