package com.example.quest.security.service;

import com.example.quest.model.entity.User;
import com.example.quest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//4.
//해당클래스가 서비스라는것을 알리는 어노테이션
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    //예외발생시 rollback처리 자동수행.
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //값이 없는 경우, Optional.orElseThrow()를 통해 명시적으로 예외를 던짐
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 아이디로 사용자를 찾을 수 없습니다.: " + username));

        return UserDetailsImpl.build(user);
    }

}
