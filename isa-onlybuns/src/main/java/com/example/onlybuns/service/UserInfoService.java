package com.example.onlybuns.service;

import com.example.onlybuns.dtos.UserInfoDTO;
import com.example.onlybuns.model.Post;
import com.example.onlybuns.model.UserInfo;
import com.example.onlybuns.repository.FollowersRepository;
import com.example.onlybuns.repository.LikeRepository;
import com.example.onlybuns.repository.PostRepository;
import com.example.onlybuns.repository.UserInfoRepository;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.time.ZoneId;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserInfoService implements UserDetailsService {

    @Autowired
    private UserInfoRepository repository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private BloomFilter<String> bloomFilter;

    @PostConstruct
    public void initBloomFilter() {
        bloomFilter = BloomFilter.create(Funnels.unencodedCharsFunnel(), 10000, 0.01);
        List<String> allUsernames = repository.findAllUsernames(); // Napravi ovaj metod
        allUsernames.forEach(bloomFilter::put);
    }

    @Autowired
    private FollowersRepository followersRepository;
    @Autowired
    private LikeRepository likeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Account not activated. Please check your email.");
        }

        return new UserInfoDetails(user);
    }

    public String changePassword(Long userId, String oldPassword, String newPassword) {
        UserInfo user = getUserById(userId);
        if (user == null) {
            return "Korisnik ne postoji.";
        }

        // Provera da li je stara lozinka tačna
        if (!encoder.matches(oldPassword, user.getPassword())) {
            return "Old password incorrect!";
        }

        // Postavi novu lozinku
        user.setPassword(encoder.encode(newPassword));
        repository.save(user);

        return "Lozinka je uspešno promenjena.";
    }

    @Transactional
    public synchronized String addUser(UserInfo userInfo) {
        // 🌸 Provera korisničkog imena preko Bloom filtera
        if (bloomFilter != null && bloomFilter.mightContain(userInfo.getUsername())) {
            UserInfo existing = getUserByUsername(userInfo.getUsername());
            if (existing != null) {
                return "User with that username already exists!";
            }
        }

        // Provera emaila
        UserInfo user = getUserByEmail(userInfo.getEmail());
        if (user != null) {
            return "User with that email already exists!";
        }

        // 🌙 Simulacija konflikta u registraciji za testiranje
        try {
            Thread.sleep(500); // koristiš samo za testiranje konkurentnog pristupa
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 🔒 Encode password
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));

        // 🔑 Generate activation token
        String activationToken = UUID.randomUUID().toString();
        userInfo.setActivationToken(activationToken);
        userInfo.setActive(false);

        // 💾 Sačuvaj korisnika
        repository.save(userInfo);

        // 🔁 Ažuriraj bloom filter
        if (bloomFilter != null) {
            bloomFilter.put(userInfo.getUsername());
        }

        // 📧 Pošalji mejl za aktivaciju
        sendActivationEmail(userInfo.getEmail(), activationToken);

        return "Registration successful! Please check your email for the activation link.";
    }
    private void sendActivationEmail(String email, String activationToken) {
        String activationLink = "http://localhost:8080/auth/activate?token=" + activationToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Account Activation");
        message.setText("Click the following link to activate your account: " + activationLink);
        message.setFrom(fromEmail);
        mailSender.send(message);
    }

    @Transactional
    public String activateUser(String token) {
        UserInfo user = repository.findByActivationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid activation token"));

        user.setActive(true);
        user.setActivationToken(null); // Clear token after activation
        repository.save(user);

        return "Account activated successfully!";
    }

    public UserInfo getUserByEmail(String email) {
        return repository.findByEmail(email).orElse(null);
    }

    public UserInfo getUserByUsername(String username){
        return repository.findByUsername(username).orElse(null);
    }

    public UserInfo getUserById(Long userId) {
        return repository.findById(userId).orElse(null);
    }
    public Page<UserInfoDTO> getFilteredUsers(
            String name,
            String email,
            Integer minPosts,
            Integer maxPosts,
            String sortBy,
            String sortOrder,
            int page,
            int size) {

        // Determine Sort property
        String sortProperty = sortBy.equalsIgnoreCase("posts") ? "postCount" : sortBy;

        // Create Sort object
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortProperty);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Create Specification
        Specification<UserInfo> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (email != null && !email.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }

            if (minPosts != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("postCount"), minPosts));
            }

            if (maxPosts != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("postCount"), maxPosts));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // Fetch data
        Page<UserInfo> usersPage = repository.findAll(specification, pageable);

        // Map to DTO
        return usersPage.map(UserInfoDTO::new);
    }

    public void updateLastLogin(UserInfo userInfo) {
        LocalDateTime now = LocalDateTime.now();
        repository.updateLastLoginByUserId(now, userInfo.getId());

    }

    @Scheduled(cron = "10 51 9 * * ?")
    public void checkInactiveUsersAndNotify() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<UserInfo> inactiveUsers = repository
                .findByLastLoginBeforeAndIsNotifiedFalse(sevenDaysAgo);

        inactiveUsers.forEach(user -> {
            //UserStatistics stats = statisticsService.getUserStatistics(user.getId());
            sendNotificationEmail(user);
            repository.markAsNotified(user.getId());
        });
    }

    private void sendNotificationEmail(UserInfo user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("We haven't seen each other 7 days!");
        message.setText(generateEmailContent(user));

        mailSender.send(message);
    }

    private String generateEmailContent(UserInfo user) {
        return String.format(
                "Dear %s,\n\nYou haven't logged in in previous 7 days.\n\n" +
                        "Here are some new statistics:\n" +
                        "- New followers: %d\n" +
                        "- New likes: %d\n" +
                        //"- Nove objave koje ste propustili: %d\n\n" +
                        "Come back and check out what's new!\n\n" +
                        "We wish you all the best,\nYour team!",
                user.getUsername(),
                calculateNewFollowers(user.getId(), user.getLastLogin()),
                calculateNewLikes(user.getId(), user.getLastLogin())
                //stats.getNewPosts()
        );
    }

    private Long calculateNewFollowers(Long userId, LocalDateTime lastLogin) {
        ZonedDateTime zonedLastLogin = lastLogin.atZone(ZoneId.systemDefault());
        return followersRepository.countNewFollowersForUserSinceLastLogin(userId, zonedLastLogin);
    }

    private Long calculateNewLikes(Long userId, LocalDateTime lastLogin){
        List<Post> userPosts = postRepository.findByUserId(userId);
        List<Long> postIds = userPosts.stream()
                .map(Post::getId)
                .collect(Collectors.toList());
        return likeRepository.countTotalNewLikesForPostsSinceLastLogin(postIds, lastLogin);

    }
    public List<UserInfoDTO> getAllUsersExcept(String currentUsername) {
        List<UserInfo> users = repository.findAll();
        return users.stream()
                .filter(u -> !u.getUsername().equals(currentUsername))
                .map(user -> new UserInfoDTO(user.getId(), user.getUsername(), user.getEmail()))
                .toList();
    }

}

