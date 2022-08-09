package com.croh.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.croh.service.UploadService;

@Controller
public class HomeController {
	
	@Autowired
	private UploadService upload;
	
	@GetMapping("/")
	public String home() {
		return "home";
	}
	
	@PostMapping("/cargar")
	public String carga( 
		@RequestParam("archivos") MultipartFile file, 
		@RequestParam("user") String user, 
		@RequestParam("password") String password, 
		RedirectAttributes ms) 
	throws IOException {
		
		System.out.println("newName");
        System.out.println(file.getOriginalFilename());
		String message = upload.readTxt(file, user, password);
		ms.addFlashAttribute("mensaje", message);
		return "redirect:/";
	}

	@PostMapping("/descargar")
	public String carga(RedirectAttributes ms) 
	throws IOException {
		System.out.println("descargar service");
		String message = upload.download();
		ms.addFlashAttribute("mensaje", message);
		return "redirect:/";
	}

}
