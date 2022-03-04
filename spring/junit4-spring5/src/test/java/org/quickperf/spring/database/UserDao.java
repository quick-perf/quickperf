/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2022 the original author or authors.
 */
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
