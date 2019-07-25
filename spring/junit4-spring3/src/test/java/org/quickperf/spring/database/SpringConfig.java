package org.quickperf.spring.database;

import org.apache.commons.dbcp.BasicDataSource;
import org.quickperf.spring.sql.QuickPerfProxyBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan(basePackageClasses = UserDao.class)
public class SpringConfig {

    @Bean
    public AnnotationSessionFactoryBean sessionFactory(DataSource dataSource) {
        AnnotationSessionFactoryBean  sessionFactory = new AnnotationSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        String packageName = this.getClass().getPackage().getName();
        sessionFactory.setPackagesToScan(new String[]{packageName});
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("test");
        dataSource.setPassword("test");
        return dataSource;
    }

    private Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        hibernateProperties.setProperty("hibernate.dialect"
                                      , "org.hibernate.dialect.H2Dialect");
        return hibernateProperties;
    }

    @Bean
    public QuickPerfProxyBeanPostProcessor dataSourceBeanPostProcessor() {
        return new QuickPerfProxyBeanPostProcessor();
    }

    @Bean
    public PlatformTransactionManager hibernateTransactionManager(LocalSessionFactoryBean sessionFactory) {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory.getObject());
        return transactionManager;
    }

}
