import com.example.internhub_be.domain.Function;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.domain.RolePermission;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.JwtAuthResponse;
import com.example.internhub_be.payload.LoginRequest;
import com.example.internhub_be.payload.PermissionResponseDTO;
import com.example.internhub_be.repository.FunctionRepository;
import com.example.internhub_be.repository.RolePermissionRepository;
import com.example.internhub_be.repository.UserRepository;
import com.example.internhub_be.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final FunctionRepository functionRepository;


    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          UserRepository userRepository,
                          RolePermissionRepository rolePermissionRepository,
                          FunctionRepository functionRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.functionRepository = functionRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new JwtAuthResponse(token));
    }

    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionResponseDTO>> getUserPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", username));

        List<RolePermission> rolePermissions = rolePermissionRepository.findById_RoleId(user.getRole().getId());

        List<PermissionResponseDTO> permissionDTOs = rolePermissions.stream().map(rp -> {
            Function function = functionRepository.findById(rp.getId().getFunctionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Function", "id", rp.getId().getFunctionId().toString()));

            return new PermissionResponseDTO(
                    function.getCode(),
                    function.getName(),
                    rp.getCanAccess(),
                    rp.getCanCreate(),
                    rp.getCanEdit(),
                    rp.getCanDelete()
            );
        }).collect(Collectors.toList());

        return new ResponseEntity<>(permissionDTOs, HttpStatus.OK);
    }
}
