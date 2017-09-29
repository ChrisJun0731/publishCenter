package com.demo.publishCenter.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Administrator on 2017/9/28.
 */
@Controller
public class MyController {
	@RequestMapping("/index")
	public String index(){
		return "index";
	}
}
