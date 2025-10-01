```
2025-09-27T15:35:03.081+05:30 ERROR 6724 --- [ecom-application] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.DataException: Unable to open JDBC Connection for DDL execution [FATAL: invalid value for parameter "TimeZone": "Asia/Calcutta"] [n/a]
2025-09-27T15:35:03.081+05:30  WARN 6724 --- [ecom-application] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.DataException: Unable to open JDBC Connection for DDL execution [FATAL: invalid value for parameter "TimeZone": "Asia/Calcutta"] [n/a]
2025-09-27T15:35:03.084+05:30  INFO 6724 --- [ecom-application] [           main] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
2025-09-27T15:35:03.095+05:30  INFO 6724 --- [ecom-application] [           main] .s.b.a.l.ConditionEvaluationReportLogger : 

Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2025-09-27T15:35:03.115+05:30 ERROR 6724 --- [ecom-application] [           main] o.s.boot.SpringApplication               : Application run failed

org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.DataException: Unable to open JDBC Connection for DDL execution [FATAL: invalid value for parameter "TimeZone": "Asia/Calcutta"] [n/a]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1826) ~[spring-beans-6.2.11.jar:6.2.11]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:607) ~[spring-beans-6.2.11.jar:6.2.11]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:529) ~[spring-beans-6.2.11.jar:6.2.11]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:339) ~[spring-beans-6.2.11.jar:6.2.11]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:373) ~[spring-beans-6.2.11.jar:6.2.11]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:337) ~[spring-beans-6.2.11.jar:6.2.11]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:207) ~[spring-beans-6.2.11.jar:6.2.11]
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:970) ~[spring-context-6.2.11.jar:6.2.11]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:627) ~[spring-context-6.2.11.jar:6.2.11]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146) ~[spring-boot-3.5.6.jar:3.5.6]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:752) ~[spring-boot-3.5.6.jar:3.5.6]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:439) ~[spring-boot-3.5.6.jar:3.5.6]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:318) ~[spring-boot-3.5.6.jar:3.5.6]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1361) ~[spring-boot-3.5.6.jar:3.5.6]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1350) ~[spring-boot-3.5.6.jar:3.5.6]
	at com.app_ecom.EcomApplication.main(EcomApplication.java:10) ~[classes/:na]
Caused by: jakarta.persistence.PersistenceException: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.DataException: Unable to open JDBC Connection for DDL execution [FATAL: invalid value for parameter "TimeZone": "Asia/Calcutta"] [n/a]
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.buildNativeEntityManagerFactory(AbstractEntityManagerFactoryBean.java:431) ~[spring-orm-6.2.11.jar:6.2.11]
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.afterPropertiesSet(AbstractEntityManagerFactoryBean.java:400) ~[spring-orm-6.2.11.jar:6.2.11]
	at org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.afterPropertiesSet(LocalContainerEntityManagerFactoryBean.java:366) ~[spring-orm-6.2.11.jar:6.2.11]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.invokeInitMethods(AbstractAutowireCapableBeanFactory.java:1873) ~[spring-beans-6.2.11.jar:6.2.11]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1822) ~[spring-beans-6.2.11.jar:6.2.11]
	... 15 common frames omitted
Caused by: org.hibernate.exception.DataException: Unable to open JDBC Connection for DDL execution [FATAL: invalid value for parameter "TimeZone": "Asia/Calcutta"] [n/a]
	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:103) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:108) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:94) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl.getIsolatedConnection(DdlTransactionIsolatorNonJtaImpl.java:74) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl.getIsolatedConnection(DdlTransactionIsolatorNonJtaImpl.java:39) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.tool.schema.internal.exec.ImprovedExtractionContextImpl.getJdbcConnection(ImprovedExtractionContextImpl.java:63) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.tool.schema.extract.spi.ExtractionContext.getQueryResults(ExtractionContext.java:43) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.tool.schema.extract.internal.SequenceInformationExtractorLegacyImpl.extractMetadata(SequenceInformationExtractorLegacyImpl.java:39) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.tool.schema.extract.internal.DatabaseInformationImpl.initializeSequences(DatabaseInformationImpl.java:66) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.tool.schema.extract.internal.DatabaseInformationImpl.<init>(DatabaseInformationImpl.java:60) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.tool.schema.internal.Helper.buildDatabaseInformation(Helper.java:185) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.tool.schema.internal.AbstractSchemaMigrator.doMigration(AbstractSchemaMigrator.java:93) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator.performDatabaseAction(SchemaManagementToolCoordinator.java:280) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator.lambda$process$5(SchemaManagementToolCoordinator.java:144) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at java.base/java.util.HashMap.forEach(HashMap.java:1429) ~[na:na]
	at org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator.process(SchemaManagementToolCoordinator.java:141) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.boot.internal.SessionFactoryObserverForSchemaExport.sessionFactoryCreated(SessionFactoryObserverForSchemaExport.java:37) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.internal.SessionFactoryObserverChain.sessionFactoryCreated(SessionFactoryObserverChain.java:35) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.internal.SessionFactoryImpl.<init>(SessionFactoryImpl.java:324) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.boot.internal.SessionFactoryBuilderImpl.build(SessionFactoryBuilderImpl.java:463) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl.build(EntityManagerFactoryBuilderImpl.java:1517) ~[hibernate-core-6.6.29.Final.jar:6.6.29.Final]
	at org.springframework.orm.jpa.vendor.SpringHibernateJpaPersistenceProvider.createContainerEntityManagerFactory(SpringHibernateJpaPersistenceProvider.java:66) ~[spring-orm-6.2.11.jar:6.2.11]
	at org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.createNativeEntityManagerFactory(LocalContainerEntityManagerFactoryBean.java:390) ~[spring-orm-6.2.11.jar:6.2.11]
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.buildNativeEntityManagerFactory(AbstractEntityManagerFactoryBean.java:419) ~[spring-orm-6.2.11.jar:6.2.11]
	... 19 common frames omitted
Caused by: org.postgresql.util.PSQLException: FATAL: invalid value for parameter "TimeZone": "Asia/Calcutta"
	at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2734) ~[postgresql-42.7.7.jar:42.7.7]
	at org.postgresql.core.v3.QueryExecutorImpl.readStartupMessages(QueryExecutorImpl.java:2859) ~[postgresql-42.7.7.jar:42.7.7]
	at org.postgresql.core.v3.QueryExecutorImpl.<init>(QueryExecutorImpl.java:178) ~[postgresql-42.7.7.jar:42.7.7]
	at org.postgresql.core.v3.Co
```
----

Perfect 👌 thanks for sharing the screenshot.
I see you’re running your Spring Boot app in **IntelliJ IDEA** and currently looking at the **Run/Debug Configurations** window.

Here’s what you need to do:

---

### ✅ Add JVM Option (`-Duser.timezone=Asia/Kolkata`)

1. In the **Run/Debug Configurations** window (the one you showed).
2. At the top, click on **Modify options** (near top-right).
3. From the dropdown, select **Add VM options**.
   → A new **VM options** field will appear just below the "Program arguments".
4. In that **VM options** field, paste:

```
-Duser.timezone=Asia/Kolkata
```

5. Click **Apply** → **OK**.
6. Now **Run** your application again.

---

### ⚡ What This Does

This sets the **JVM default timezone** to `Asia/Kolkata`, so Hibernate and JDBC will use it when connecting to PostgreSQL.
This avoids the `FATAL: invalid value for parameter "TimeZone": "Asia/Calcutta"` error.

---

👉 Do you want me to also show you how to **verify inside the app** (e.g., log the JVM timezone at startup) so you can confirm it’s correctly applied?
