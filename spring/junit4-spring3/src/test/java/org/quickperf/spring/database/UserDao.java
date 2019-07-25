package org.quickperf.spring.database;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDao {

	@Autowired
	private SessionFactory sessionFactory;

	public void save(User user) {
		Session session = sessionFactory.getCurrentSession();
		session.persist(user);
	}

	public User findByEmail(String email) {
		Session session = sessionFactory.getCurrentSession();
		String emailParam = "email";
		String hqlQuery = " FROM " + User.class.getCanonicalName() + " user"
				        + " WHERE user.email =" + ":" + emailParam;
		Query query = session.createQuery(hqlQuery);
		query.setParameter(emailParam, email);
		List results = query.list();
		return (User) results.get(0);
	}

}
