package telegrambot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Users {

	private static Set<User> users = new HashSet<User>();

	public static User getUser(String userName) {
		for (User user : users) {
			if (user.getUserName().equals(userName)) {
				return user;
			}
		}

		return null;
	}

	public synchronized static void addUser(User user) {
		
		if (!Users.contains(user)) {
			users.add(user);
		}
	}
	
	public static int size() {
		return users.size();
	}
	
	private static boolean contains(User user) {
		for(User u : users) {
			if(u.equals(user)) {
				return true;
			}
		}
		return false;
	}
	public static void main(String args[]) {
		User user1 = new User("user1");
		User user2 = new User("user2");
		User user3 = new User("user1");
		Users.addUser(user1);
		Users.addUser(user2);
		Users.addUser(user3);
		System.out.println(Users.getUser("user1").getUserName());
		System.out.println(Users.size());
	}

}
