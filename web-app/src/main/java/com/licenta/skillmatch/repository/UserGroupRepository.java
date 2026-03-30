package com.licenta.skillmatch.repository;

import com.licenta.skillmatch.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long>
{
    UserGroup findByGroupName(String groupName);
}
