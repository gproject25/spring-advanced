package org.example.expert.data;

import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;

public class UserFixture {
	public static User createUser(){
		return new User("user@test.com", "Password1234", UserRole.USER);
	}

	public static User createUser2(){
		return new User("user2@test.com", "Password1234", UserRole.USER);
	}

	public static User createUserAdminRole() {
		return new User("admin@test.com", "Password1234", UserRole.ADMIN);
	}
}
