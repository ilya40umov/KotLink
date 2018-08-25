package org.kotlink.config

import org.jetbrains.exposed.spring.SpringTransactionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
class ExposedConfig {

    @Bean
    fun transactionManager(dataSource: DataSource) = SpringTransactionManager(dataSource)
}