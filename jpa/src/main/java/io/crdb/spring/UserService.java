package io.crdb.spring;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RetryTemplate retryTemplate;

    public UserService(UserRepository userRepository, RetryTemplate retryTemplate) {
        this.userRepository = userRepository;
        this.retryTemplate = retryTemplate;
    }

    @Transactional
    public Iterable<User> saveAll(List<User> users) {
        return retryTemplate.execute(context -> userRepository.saveAll(users));
    }

    @Transactional
    public Iterable<User> saveAll(Iterable<User> users) {
        return retryTemplate.execute(context -> userRepository.saveAll(users));
    }

    @Transactional
    public User save(User user) {
        return retryTemplate.execute(context -> userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Iterable<User> findAll(Iterable<UUID> ids) {
        return userRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public Optional<User> find(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean exists(UUID id) {
        return userRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return userRepository.count();
    }

    @Transactional
    public void deleteAll() {
        retryTemplate.execute((RetryCallback<Void, RuntimeException>) context -> {
            userRepository.deleteAll();
            return null;
        });
    }

    @Transactional
    public void deleteAll(Iterable<User> users) {
        retryTemplate.execute((RetryCallback<Void, RuntimeException>) context -> {
            userRepository.deleteAll(users);
            return null;
        });
    }

    @Transactional
    public void delete(UUID id) {
        retryTemplate.execute((RetryCallback<Void, RuntimeException>) context -> {
            userRepository.deleteById(id);
            return null;
        });
    }

    @Transactional
    public void delete(User user) {
        retryTemplate.execute((RetryCallback<Void, RuntimeException>) context -> {
            userRepository.delete(user);
            return null;
        });
    }

    // force retry methods

    @Transactional
    public void forceRetry(User user, int waitBefore, int waitAfter) throws InterruptedException {
        TimeUnit.SECONDS.sleep(waitBefore);
        userRepository.save(user);
        TimeUnit.SECONDS.sleep(waitAfter);
    }


    // custom methods

    @Transactional
    public int updateUsers() {
        return retryTemplate.execute(context -> userRepository.updateTimestamp(ZonedDateTime.now()));
    }


    @Transactional(readOnly = true)
    public Iterable<User> findUsersWithNullTimestamp() {
        return userRepository.findByUpdatedTimestampIsNull();
    }

}
