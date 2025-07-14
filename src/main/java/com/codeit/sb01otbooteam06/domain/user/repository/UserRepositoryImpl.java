package com.codeit.sb01otbooteam06.domain.user.repository;

import com.codeit.sb01otbooteam06.domain.user.entity.QUser;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<User> searchUsers(String cursor, String emailLike, String roleEqual, Boolean locked, String sortBy, String sortDirection, int limit) {
        QUser user = QUser.user;

        var query = queryFactory.selectFrom(user);

        if (cursor != null) {
            query.where(user.id.gt(UUID.fromString(cursor)));
        }

        if (emailLike != null) {
            query.where(user.email.containsIgnoreCase(emailLike));
        }

        if (roleEqual != null) {
            query.where(user.role.stringValue().eq(roleEqual));
        }

        if (locked != null) {
            query.where(user.locked.eq(locked));
        }

        if ("email".equals(sortBy)) {
            query.orderBy("DESCENDING".equals(sortDirection) ? user.email.desc() : user.email.asc());
        } else {
            query.orderBy("DESCENDING".equals(sortDirection) ? user.createdAt.desc() : user.createdAt.asc());
        }

        return query.limit(limit).fetch();
    }
}
