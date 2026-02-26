package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.port.output.persistence.DatabaseHealthCheckPort
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class DatabaseHealthCheckAdapter(
    private val dataSource: DataSource,
) : DatabaseHealthCheckPort {
    override fun isHealthy(): Boolean =
        try {
            dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("SELECT 1").use { rs ->
                        rs.next()
                    }
                }
            }
        } catch (e: Exception) {
            false
        }
}
