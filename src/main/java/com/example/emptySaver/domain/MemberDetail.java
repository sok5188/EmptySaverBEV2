package com.example.emptySaver.domain;

import com.example.emptySaver.domain.entity.Member;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@ToString
@Slf4j
public class MemberDetail implements UserDetails {
    public Member getUser() {
        return user;
    }

    private Member user;
    public MemberDetail(Member user) {
        this.user = user;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole().toString();
            }
        });
        return collect;
    }


    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public int hashCode() {
        return this.getUsername().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MemberDetail){
            return this.getUsername().equals(((MemberDetail)obj).getUsername());
        }
        return false;
    }
}
