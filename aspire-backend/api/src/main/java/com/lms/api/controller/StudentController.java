package com.lms.api.controller;

import static org.springframework.http.ResponseEntity.ok;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lms.api.model.Student;
import com.lms.api.repository.StudentRepository;
//import com.lms.api.repository.UserRepository;
import com.lms.api.security.jwt.JwtTokenProvider;
import com.lms.api.exception.StudentNotFoundException;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/student")
public class StudentController {
	
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@Autowired
	StudentRepository students;

	private final StudentRepository SRepo;

	StudentController (StudentRepository repository) {
		this.SRepo = repository;
	}

	// API to get all students
	@GetMapping("/all")
	public List<Student> list() {
		return SRepo.findAll();
	}

	// API to get student by ID
	@GetMapping("/get/{id}")
	public Student get(@PathVariable String id) {
		return SRepo.findById(id)
				.orElseThrow(() -> new StudentNotFoundException(id));
	}

	// API to create a student
	@PostMapping("/add")
	Student create(@RequestBody Student newStudent) {
		newStudent.setRole("Student");
		newStudent.setApproved("Pending");
		newStudent.setActive(true);
		return SRepo.save(newStudent);
	}

	// API to update a student
	@PutMapping("/update/{id}")
	public Student update(@PathVariable String id, @RequestBody Student student) {
		return SRepo.findById(id)
				.map(s -> {
					s.setPid(student.getPid());
					s.setFname(student.getFname());
					s.setLname(student.getLname());
					s.setDob(student.getDob());
					s.setGender(student.getGender());
					s.setPhone(student.getPhone());
					s.setEmail(student.getEmail());
					s.setPassword(student.getPassword());
					s.setApproved(student.getApproved());
					s.setActive(student.getActive());
					s.setRole(student.getRole());
					return SRepo.save(s);
				})
				.orElseGet(() -> {
					student.setId(id);
					return SRepo.save(student);
				});
	}

	// API to delete a student
	@DeleteMapping("/delete/{id}")
	public void delete(@PathVariable String id) {
		SRepo.deleteById(id);
	}
	
	
	@SuppressWarnings("rawtypes")
	@PostMapping("/login")
	public ResponseEntity login(@RequestBody AuthBody data) {
		try {
			String username = data.getEmail();
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));
			String token = jwtTokenProvider.createToken(username, this.students.findByEmail(username).getRoles());
			Map<Object, Object> model = new HashMap<>();
			model.put("username", username);
			model.put("token", token);
			return ok(model);
		} catch (AuthenticationException e) {
			throw new BadCredentialsException("Invalid email/password supplied");
		}
	}
	
}