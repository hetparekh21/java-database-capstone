
package com. project.back_end.controllers;

import com. project.back_end.models.Admin;
import com. project.back_end.services.Service;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.path}admin")
public class AdminController {

	private final Service sharedService;

	@Autowired
	public AdminController(Service sharedService) {
		this.sharedService = sharedService;
	}

	/**
	 * POST /login
	 * Body: Admin JSON (username, password)
	 * Returns: { token: "..." } on success or 401 with message on failure
	 */
	@PostMapping("/login")

	public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody Admin admin) {
		Map<String, Object> resp = new HashMap<>();
		if (admin == null || admin.getUsername() == null || admin.getPassword() == null) {
			resp.put("message", "Missing credentials");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
		}

        // System.out.println("*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n");
        // System.out.println("Admin found: " + admin.getUsername());
        // System.out.println("*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n");

		String token = sharedService.validateAdmin(admin.getUsername(), admin.getPassword());
		if (token == null) {
			resp.put("message", "Invalid username or passsword");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
		}

		resp.put("token", token);
		return ResponseEntity.ok(resp);
	}

}

