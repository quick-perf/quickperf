package org.quickperf.spring.database;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
		Query<User> query = session.createQuery(hqlQuery, User.class);
		query.setParameter(emailParam, email);
		return query.getSingleResult();
	}

}
