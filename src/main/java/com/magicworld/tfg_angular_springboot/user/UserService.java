package com.magicworld.tfg_angular_springboot.user;

import com.magicworld.tfg_angular_springboot.exceptions.EmailAlreadyExistsException;
import com.magicworld.tfg_angular_springboot.exceptions.InvalidOperationException;
import com.magicworld.tfg_angular_springboot.purchase.PurchaseRepository;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLineRepository;
import com.magicworld.tfg_angular_springboot.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseLineRepository purchaseLineRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    @Transactional
    public void setCurrentUser(User user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                user.getAuthorities().toString());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword(),
                        Collections.singletonList(authority));
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Transactional
    public User updateProfile(User user, UpdateProfileRequest request) {
        if (!user.getEmail().equals(request.getEmail()) &&
            userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
                throw new InvalidOperationException("error.password.pattern");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUserWithRelatedData(User user) {
        reviewRepository.deleteAll(
            reviewRepository.findAll().stream()
                .filter(r -> r.getPurchase().getBuyer().getId().equals(user.getId()))
                .toList()
        );

        purchaseRepository.findByBuyerId(user.getId()).forEach(purchase -> {
            purchaseLineRepository.deleteAll(
                purchaseLineRepository.findByPurchaseId(purchase.getId())
            );
        });
        purchaseRepository.deleteAll(purchaseRepository.findByBuyerId(user.getId()));
        userRepository.delete(user);
    }
}
