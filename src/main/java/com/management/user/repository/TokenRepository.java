package com.management.user.repository;

import com.management.user.entity.Tokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Tokens, Integer> {

    @Query(value = """
      select t from Tokens t inner join Users u\s
      on t.userId = u.id\s
      where u.id = :id\s
      """)
    Optional<Tokens> findTokenByUserId(Integer id);

    Optional<Tokens> findTokenByRefreshToken(String refreshToken);

    void deleteByUserId(Integer id);
}
