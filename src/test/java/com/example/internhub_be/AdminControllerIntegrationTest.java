package com.example.internhub_be;

import com.example.internhub_be.domain.Department;
import com.example.internhub_be.domain.Role;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.payload.LoginRequest;
import com.example.internhub_be.payload.JwtAuthResponse;
import com.example.internhub_be.repository.DepartmentRepository;
import com.example.internhub_be.repository.RoleRepository;
import com.example.internhub_be.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String NON_ADMIN_EMAIL = "user@internhub.com";
    private final String NON_ADMIN_PASSWORD = "password";
    private final String NON_ADMIN_ROLE = "INTERN";
    private final String DEFAULT_DEPARTMENT = "IT Department";


    @BeforeEach
    void setup() {
        // Ensure default department exists
        Optional<Department> departmentOptional = departmentRepository.findByName(DEFAULT_DEPARTMENT);
        if (departmentOptional.isEmpty()) {
            Department itDepartment = new Department();
            itDepartment.setName(DEFAULT_DEPARTMENT);
            itDepartment.setDescription("Information Technology Department");
            departmentRepository.save(itDepartment);
        }

        // Ensure non-admin role exists
        Optional<Role> roleOptional = roleRepository.findByName(NON_ADMIN_ROLE);
        if (roleOptional.isEmpty()) {
            roleRepository.save(new Role(null, NON_ADMIN_ROLE));
        }

        // Create non-admin user if not exists
        if (userRepository.findByEmail(NON_ADMIN_EMAIL).isEmpty()) {
            User nonAdmin = new User();
            nonAdmin.setName("Test User");
            nonAdmin.setEmail(NON_ADMIN_EMAIL);
            nonAdmin.setPassword(passwordEncoder.encode(NON_ADMIN_PASSWORD));
            roleRepository.findByName(NON_ADMIN_ROLE).ifPresent(nonAdmin::setRole);
            departmentRepository.findByName(DEFAULT_DEPARTMENT).ifPresent(nonAdmin::setDepartment);
            nonAdmin.setIsActive(true);
            userRepository.save(nonAdmin);
        }
    }

    @AfterEach
    void teardown() {
        // Clean up the non-admin user created for tests
        userRepository.findByEmail(NON_ADMIN_EMAIL).ifPresent(userRepository::delete);
    }

    private String obtainAdminToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@internhub.com");
        loginRequest.setPassword("admin");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        JwtAuthResponse authResponse = objectMapper.readValue(responseString, JwtAuthResponse.class);
        return authResponse.getAccessToken();
    }

    private String obtainUserToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        JwtAuthResponse authResponse = objectMapper.readValue(responseString, JwtAuthResponse.class);
        return authResponse.getAccessToken();
    }

    @Test
    void testGetAllRoles_Success_PublicAccess() throws Exception {
        // Không cần token, bất kỳ ai cũng có thể truy cập
        mockMvc.perform(get("/api/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].name").isString());
    }
}
